package com.m3o.mobile.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class VectorDrawableCreator {

    private static final byte[][] BIN_XML_STRINGS = {
        "height".getBytes(),
        "width".getBytes(),
        "viewportWidth".getBytes(),
        "viewportHeight".getBytes(),
        "fillColor".getBytes(),
        "pathData".getBytes(),
        "strokeColor".getBytes(),
        "strokeWidth".getBytes(),
        "translateX".getBytes(),
        "translateY".getBytes(),
        "android".getBytes(),
        "http://schemas.android.com/apk/res/android".getBytes(),
        "path".getBytes(),
        "vector".getBytes(),
        "group".getBytes()
    };

    private static final int[] BIN_XML_ATTRS = {
        android.R.attr.height,
        android.R.attr.width,
        android.R.attr.viewportWidth,
        android.R.attr.viewportHeight,
        android.R.attr.fillColor,
        android.R.attr.pathData,
        android.R.attr.strokeColor,
        android.R.attr.strokeWidth
    };

    private static final short CHUNK_TYPE_XML = 0x0003;
    private static final short CHUNK_TYPE_STR_POOL = 0x0001;
    private static final short CHUNK_TYPE_START_TAG = 0x0102;
    private static final short CHUNK_TYPE_NS_START_TAG = 0x0100;
    private static final short CHUNK_TYPE_END_TAG = 0x0103;
    private static final short CHUNK_TYPE_NS_END_TAG = 0x0101;
    private static final short CHUNK_TYPE_RES_MAP = 0x0180;

    private static final short VALUE_TYPE_DIMENSION = 0x0500;
    private static final short VALUE_TYPE_STRING = 0x0300;
    private static final short VALUE_TYPE_COLOR_F = 0x1F00;
    private static final short VALUE_TYPE_COLOR_D = 0x1D00;
    private static final short VALUE_TYPE_FLOAT = 0x0400;


    /**
     * Create a vector drawable from a list of paths and colors
     * @param width drawable width
     * @param height drawable height
     * @param viewportWidth vector image width
     * @param viewportHeight vector image height
     * @param paths list of path data and colors
     * @return the vector drawable or null it couldn't be created.
     */
    public static Drawable getVectorDrawable(
        @NonNull Context context,
        int width,
        int height,
        float viewportWidth,
        float viewportHeight,
        boolean stroke,
        float translateX,
        float translateY,
        List<PathData> paths
    ) {
        byte[] binXml = createBinaryDrawableXml(
            width,
            height,
            viewportWidth,
            viewportHeight,
            stroke,
            translateX,
            translateY,
            paths
        );

        try {
            // Get the binary XML parser (XmlBlock.Parser) and use it to create the drawable
            // This is the equivalent of what AssetManager#getXml() does
            @SuppressLint("PrivateApi")
            Class<?> xmlBlock = Class.forName("android.content.res.XmlBlock");
            Constructor xmlBlockConstr = xmlBlock.getConstructor(byte[].class);
            Method xmlParserNew = xmlBlock.getDeclaredMethod("newParser");

            xmlBlockConstr.setAccessible(true);
            xmlParserNew.setAccessible(true);
            XmlPullParser parser = (XmlPullParser) xmlParserNew.invoke(
                    xmlBlockConstr.newInstance((Object) binXml));
            // TODO: fix that check for Android < API 24
            if (true) {
                return Drawable.createFromXml(context.getResources(), parser);
            } else {
                // Before API 24, vector drawables aren't rendered correctly without compat lib
                final AttributeSet attrs = Xml.asAttributeSet(parser);
                int type = parser.next();
                while (type != XmlPullParser.START_TAG) {
                    type = parser.next();
                }
                return VectorDrawableCompat.createFromXmlInner(context.getResources(), parser, attrs, null);
            }
        } catch (Exception e) {
            Log.e(VectorDrawableCreator.class.getSimpleName(), "Vector creation failed", e);
        }
        return null;
    }

    private static byte[] createBinaryDrawableXml(
        int width,
        int height,
        float viewportWidth,
        float viewportHeight,
        boolean stroke,
        float translateX,
        float translateY,
        List<PathData> paths
    ) {
        List<byte[]> stringPool = new ArrayList<>(Arrays.asList(BIN_XML_STRINGS));
        stringPool.add(12, paths.get(0).data);

        ByteBuffer bb = ByteBuffer.allocate(8192);  // Capacity might have to be greater.
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int posBefore;

        // ==== XML chunk ====
        // https://justanapplication.wordpress.com/2011/09/22/android-internals-binary-xml-part-two-the-xml-chunk/
        bb.putShort(CHUNK_TYPE_XML);  // Type
        bb.putShort((short) 8);  // Header size
        int xmlSizePos = bb.position();
        bb.position(bb.position() + 4);

        // ==== String pool chunk ====
        // https://justanapplication.wordpress.com/2011/09/15/android-internals-resources-part-four-the-stringpool-chunk/
        int spStartPos = bb.position();
        bb.putShort(CHUNK_TYPE_STR_POOL);  // Type
        bb.putShort((short) 28);  // Header size
        int spSizePos = bb.position();
        bb.position(bb.position() + 4);
        bb.putInt(stringPool.size());  // String count
        bb.putInt(0);  // Style count
        bb.putInt(1 << 8);  // Flags set: encoding is UTF-8
        int spStringsStartPos = bb.position();
        bb.position(bb.position() + 4);
        bb.putInt(0);  // Styles start

        // String offsets
        int offset = 0;
        for (byte[] str : stringPool) {
            bb.putInt(offset);
            offset += str.length + (str.length > 127 ? 5 : 3);
        }

        posBefore = bb.position();
        bb.putInt(spStringsStartPos, bb.position() - spStartPos);
        bb.position(posBefore);

        // String pool
        for (byte[] str : stringPool) {
            if (str.length > 127) {
                byte high = (byte) ((str.length & 0xFF00 | 0x8000) >>> 8);
                byte low = (byte) (str.length & 0xFF);
                bb.put(high);
                bb.put(low);
                bb.put(high);
                bb.put(low);
            } else {
                byte len = (byte) str.length;
                bb.put(len);
                bb.put(len);
            }
            bb.put(str);
            bb.put((byte) 0);
        }

        if (bb.position() % 4 != 0) {
            // Padding to align on 32-bit
            bb.put(new byte[4 - (bb.position() % 4)]);
        }

        // Write string pool chunk size
        posBefore = bb.position();
        bb.putInt(spSizePos, bb.position() - spStartPos);
        bb.position(posBefore);

        // ==== Resource map chunk ====
        // https://justanapplication.wordpress.com/2011/09/23/android-internals-binary-xml-part-four-the-xml-resource-map-chunk/
        bb.putShort(CHUNK_TYPE_RES_MAP);  // Type
        bb.putShort((short) 8);  // Header size
        bb.putInt(8 + BIN_XML_ATTRS.length * 4);  // Chunk size
        for (int attr : BIN_XML_ATTRS) {
            bb.putInt(attr);
        }

        // ==== Vector start tag ====
        putNsStartTag(bb, 11);

        int vstStartPos = bb.position();
        int vstSizePos = putStartTag(bb, 14, 4, 1);

        // Attributes

        // android:height="24dp", value type: dimension (dp)
        putAttribute(bb, 0, -1, VALUE_TYPE_DIMENSION, (height << 8) + 1);

        // android:width="24dp", value type: dimension (dp)
        putAttribute(bb, 1, -1, VALUE_TYPE_DIMENSION, (width << 8) + 1);



        // android:viewportWidth="24", value type: float
        putAttribute(bb, 2, -1, VALUE_TYPE_FLOAT, Float.floatToRawIntBits(viewportWidth));

        // android:viewportHeight="24", value type: float
        putAttribute(bb, 3, -1, VALUE_TYPE_FLOAT, Float.floatToRawIntBits(viewportHeight));

        // Write vector start tag chunk size
        posBefore = bb.position();
        bb.putInt(vstSizePos, bb.position() - vstStartPos);
        bb.position(posBefore);

        // ==== Group start tag ====
        int gstStartPos = bb.position();
        int gstSizePos = putStartTag(bb, 15, 2, 6);

        putAttribute(bb, 8, -1, VALUE_TYPE_FLOAT, Float.floatToRawIntBits(translateX));
        putAttribute(bb, 9, -1, VALUE_TYPE_FLOAT, Float.floatToRawIntBits(translateY));

        posBefore = bb.position();
        bb.putInt(gstSizePos, bb.position() - gstStartPos);
        bb.position(posBefore);

        for (int i = 0; i < paths.size(); i++) {
            // ==== Path start tag ====
            int pstStartPos = bb.position();
            int pstSizePos = putStartTag(bb, 13, stroke ? 4 : 2, 7);


            // android:fillColor="#aarrggbb", value type: #rgb.
            putAttribute(bb, 4, -1, VALUE_TYPE_COLOR_F, paths.get(i).color);

            // android:pathData="...", value type: string
            // TODO: handle multiple paths
            putAttribute(bb, 5, 12, VALUE_TYPE_STRING, 12);

            if(stroke) {

                // android:strokeWidth="...", value type: #rgb
                putAttribute(bb, 6, -1, VALUE_TYPE_COLOR_D,  paths.get(i).strokeColor);

                // android:strokeWidth="...", value type: float
                putAttribute(bb, 7, -1, VALUE_TYPE_FLOAT, Float.floatToRawIntBits(2));
            }

            // Write path start tag chunk size
            posBefore = bb.position();
            bb.putInt(pstSizePos, bb.position() - pstStartPos);
            bb.position(posBefore);

            // ==== Path end tag ====
            putEndTag(bb, 13, 7);
        }

        // ==== group end tag ====
        putEndTag(bb, 15, 6);

        // ==== Vector end tag ====
        putEndTag(bb, 14, 1);

        putNsEndTag(bb, 11);

        // Write XML chunk size
        posBefore = bb.position();
        bb.putInt(xmlSizePos, bb.position());
        bb.position(posBefore);

        // Return binary XML byte array
        byte[] binXml = new byte[bb.position()];
        bb.rewind();
        bb.get(binXml);

        try {
            Log.d("XML", new String(binXml, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return binXml;
    }

    private static int putStartTag(
        ByteBuffer bb,
        int name,
        int attributeCount,
        int lineNumber
    ) {
        // https://justanapplication.wordpress.com/2011/09/25/android-internals-binary-xml-part-six-the-xml-start-element-chunk/
        bb.putShort(CHUNK_TYPE_START_TAG);
        bb.putShort((short) 16);  // Header size
        int sizePos = bb.position();
        bb.putInt(0); // Size, to be set later
        bb.putInt(lineNumber);  // Line number: None
        bb.putInt(-1);  // Comment: None

        bb.putInt(-1);  // Namespace: None
        bb.putInt(name);
        bb.putShort((short) 0x14);  // Attributes start offset
        bb.putShort((short) 0x14);  // Attributes size
        bb.putShort((short) attributeCount);  // Attribute count
        bb.putShort((short) 0);  // ID attr: none
        bb.putShort((short) 0);  // Class attr: none
        bb.putShort((short) 0);  // Style attr: none

        return sizePos;
    }

    private static void putNsStartTag(ByteBuffer bb, int name) {
        // https://justanapplication.wordpress.com/2011/09/25/android-internals-binary-xml-part-six-the-xml-start-element-chunk/
        bb.putShort(CHUNK_TYPE_NS_START_TAG);
        bb.putShort((short) 16);  // Header size
        bb.putInt(24); // Size, to be set later
        bb.putInt(1);  // Line number: None
        bb.putInt(-1);  // Comment: None
        bb.putInt(10);  // Namespace: None
        bb.putInt(name);
    }

    private static void putEndTag(
        ByteBuffer bb,
        int name,
        int lineNumber
    ) {
        // https://justanapplication.wordpress.com/2011/09/26/android-internals-binary-xml-part-seven-the-xml-end-element-chunk/
        bb.putShort(CHUNK_TYPE_END_TAG);
        bb.putShort((short) 16);  // Header size
        bb.putInt(24);  // Chunk size
        bb.putInt(lineNumber);  // Line number: none
        bb.putInt(-1);  // Comment: none
        bb.putInt(-1);  // Namespace: none
        bb.putInt(name);  // Name: vector
    }

    private static void putNsEndTag(ByteBuffer bb, int name) {
        // https://justanapplication.wordpress.com/2011/09/26/android-internals-binary-xml-part-seven-the-xml-end-element-chunk/
        bb.putShort(CHUNK_TYPE_NS_END_TAG);
        bb.putShort((short) 16);  // Header size
        bb.putInt(24);  // Chunk size
        bb.putInt(1);  // Line number: none
        bb.putInt(-1);  // Comment: none
        bb.putInt(10);  // Namespace: none
        bb.putInt(name);  // Name: vector
    }

    private static void putAttribute(
        ByteBuffer bb,
        int name,
        int rawValue,
        short valueType,
        int valueData
    ) {
        // https://justanapplication.wordpress.com/2011/09/19/android-internals-resources-part-eight-resource-entries-and-values/#struct_Res_value
        bb.putInt(11);  // Namespace index in string pool (always the android namespace)
        bb.putInt(name);
        bb.putInt(rawValue);
        bb.putShort((short) 0x08);  // Value size
        bb.putShort(valueType);
        bb.putInt(valueData);
    }

    public static class PathData {

        public byte[] data;
        public int color;
        public int strokeColor;

        public PathData(
            byte[] data,
            int color,
            int strokeColor
        ) {
            this.data = data;
            this.color = color;
            this.strokeColor = strokeColor;
        }

        public PathData(
            String data,
            int color,
            int strokeColor
        ) {
            this(data.getBytes(StandardCharsets.UTF_8), color, strokeColor);
        }
    }
}