package com.m3o.mobile.fragments.services.qrcodes

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.QrCodesService
import com.m3o.mobile.databinding.FragmentServiceQrCodesBinding
import com.m3o.mobile.utils.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

class QRCodesFragment : Fragment() {
    private var _binding: FragmentServiceQrCodesBinding? = null
    private lateinit var myContext: Context
    private var type = -1
    private var filled = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceQrCodesBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setImageDrawable(getServiceIcon(myContext, getSvg()))

        binding.typeInputText.setOnItemClickListener { _, _, position, _ ->
            hideKeyboard()
            type = position
            binding.apply {
                when (type) {
                    0 -> {
                        input1.hint = "* Text"
                        input1Text.inputType = InputType.TYPE_CLASS_TEXT
                        setInputsVisibility(true)
                    }
                    1 -> {
                        input1.hint = "* URL"
                        input1Text.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                        setInputsVisibility(true)
                    }
                    2 -> {
                        input1.hint = "* Address"
                        input2.hint = "CC"
                        input3.hint = "BCC"
                        input4.hint = "Subject"
                        input5.hint = "Body"
                        input1Text.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        input2Text.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        setInputsVisibility(true, true, true, true, true)
                    }
                    3 -> {
                        input1.hint = "* Telephone Number"
                        input1Text.inputType = InputType.TYPE_CLASS_PHONE
                        setInputsVisibility(true)
                    }
                    4 -> {
                        input1.hint = "* Telephone Number"
                        input2.hint = "Message"
                        input1Text.inputType = InputType.TYPE_CLASS_PHONE
                        input2Text.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        setInputsVisibility(true, true)
                    }
                    5 -> {
                        input1.hint = "* Package name (e.g. com.test.android)"
                        input1Text.inputType = InputType.TYPE_CLASS_TEXT
                        setInputsVisibility(true)
                    }
                }
                generateButton.visibility = View.VISIBLE
            }
        }

        binding.generateButton.setOnClickListener {
            hideKeyboard()
            binding.apply {
                if (regexMatch()) {
                    progressBar.visibility = View.VISIBLE
                    logD("QR Code content: $content")
                    filled = false
                    binding.imageView.setImageDrawable(getServiceIcon(myContext, getSvg()))

                    val content = formatContent(
                        input1Text.toTrimmedString(),
                        input2Text.toTrimmedString(),
                        input3Text.toTrimmedString(),
                        input4Text.toTrimmedString(),
                        input5Text.toTrimmedString()
                    )

                    if (!M3O.isInitialized()) {
                        M3O.initialize(Safe.getAndDecryptApiKey(myContext))
                    }
                    fetchQrCode(content)

                    input1.error = null
                    input2.error = null
                    input3.error = null
                    input4.error = null
                    input5.error = null
                    input1Text.setText("")
                    input2Text.setText("")
                    input3Text.setText("")
                    input4Text.setText("")
                    input5Text.setText("")
                } else {
                    handleFailedRegex()
                }
            }
        }

        binding.imageView.setOnClickListener {
            if (filled) {
                binding.progressBar.visibility = View.VISIBLE
                val bitmap = binding.imageView.drawable.toBitmap()
                saveImage(bitmap)
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setInputsVisibility(
        input1Visible: Boolean,
        input2Visible: Boolean = false,
        input3Visible: Boolean = false,
        input4Visible: Boolean = false,
        input5Visible: Boolean = false
    ) {
        val visible = View.VISIBLE
        val gone = View.GONE
        binding.apply {
            input1.visibility = if (input1Visible) visible else gone
            input2.visibility = if (input2Visible) visible else gone
            input3.visibility = if (input3Visible) visible else gone
            input4.visibility = if (input4Visible) visible else gone
            input5.visibility = if (input5Visible) visible else gone
            input1.error = null
            input2.error = null
            input3.error = null
            input4.error = null
            input5.error = null
        }
    }

    private fun regexMatch(): Boolean {
        binding.apply {
            return when (type) {
                0 -> input1Text.toTrimmedString().isNotBlank()
                1 -> Patterns.WEB_URL.matcher(input1Text.toTrimmedString()).matches()
                2 -> Patterns.EMAIL_ADDRESS.matcher(input1Text.toTrimmedString()).matches()
                3 -> Patterns.PHONE.matcher(input1Text.toTrimmedString()).matches()
                4 -> Patterns.PHONE.matcher(input1Text.toTrimmedString()).matches()
                5 -> {
                    val regex = Regex("^([A-Za-z][A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*\$")
                    regex.matches(input1Text.toTrimmedString())
                }
                else -> false
            }
        }
    }

    private fun handleFailedRegex() {
        binding.apply {
            when (type) {
                0 -> input1.error = "Empty input"
                1 -> input1.error = "Invalid URL"
                2 -> input1.error = "Invalid email address"
                3 -> input1.error = "Invalid phone number"
                4 -> input1.error = "Invalid phone number"
                5 -> input1.error = "Invalid package name"
            }
        }
    }

    private fun formatContent(vararg content: String): String {
        return when (type) {
            0 -> content[0]
            1 -> content[0]
            2 -> {
                var formatted = "mailto:${content[0]}"
                var addedSomething = false
                if (content[1].isNotBlank()) {
                    formatted += "?cc=${content[1]}"
                    addedSomething = true
                }
                if (content[2].isNotBlank()) {
                    val separator = if (addedSomething) "&" else "?"
                    formatted += "${separator}bcc=${content[2]}"
                    addedSomething = true
                }
                if (content[3].isNotBlank()) {
                    val separator = if (addedSomething) "&" else "?"
                    formatted += "${separator}subject=${content[3]}"
                    addedSomething = true
                }
                if (content[4].isNotBlank()) {
                    val separator = if (addedSomething) "&" else "?"
                    formatted += "${separator}body=${content[4]}"
                }

                formatted
            }
            3 -> {
                "tel:${content[0]}"
            }
            4 -> {
                var formatted = "smsto:${content[0]}"
                if (content[1].isNotBlank()) {
                    formatted += ":${content[1]}"
                }

                formatted
            }
            5 -> {
                "https://play.google.com/store/apps/details?id=${content[0]}"
            }
            else -> ""
        }
    }

    private fun fetchQrCode(content: String) {
        lifecycleScope.launch {
            try {
                val qrLink = QrCodesService.generate(content, 1024).qr
                logD("QR Code URL: $qrLink")
                storeToClipboard("QR Code URL", qrLink)
                showToast("Copied QR Code URL")
                filled = true
                Glide.with(myContext).load(qrLink)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            e?.printStackTrace()
                            logE("Loading QR link into ImageView failed")
                            if (e != null) {
                                showErrorDialog(e.message)
                            }
                            return true
                        }
                    })
                    .into(binding.imageView)
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                logE("Generating QR Code failed")
                showErrorDialog(e.message)
            }
        }
    }

    private fun saveImage(bitmap: Bitmap) {
        val fileName = "qr-code-${Random.nextInt(1, 999999)}.png"
        val out: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val path = File(Environment.DIRECTORY_PICTURES, "M3O").toString()
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, path)
            }
            val uri = myContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (uri != null) {
                out = myContext.contentResolver.openOutputStream(uri)
            } else {
                out = null
            }
        } else {
            val path = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString(),
                "M3O"
            )
            val file = File(path, fileName)
            out = FileOutputStream(file)
        }

        if (out != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
                showToast("QR code saved to gallery")
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = "Saving QR code failed"
                logE(errorMessage)
                showToast(errorMessage)
            }
        }
    }

    private fun getSvg(): String {
        val packageName: String = myContext.packageName
        val resId = myContext.resources.getIdentifier("qr_codes", "string", packageName)
        return myContext.getString(resId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
