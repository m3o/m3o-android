package com.m3o.mobile.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal const val ACCESS_TOKEN = "access_token"
internal const val API_KEY = "key"
internal const val EMAIL = "email"
internal const val LOG_APP_ID = "M3O-Mobile"
internal const val REFRESH_TOKEN = "refresh_token"
internal const val SHARED_PREFERENCE = "Safe"
internal const val SKIP_REFRESH = "skip_refresh"
internal const val USER_ID = "user_id"

internal fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

internal fun Fragment.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, length)
}

internal fun Context.storeToClipboard(label: String, text: String) {
    val clip = ClipData.newPlainText(label, text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
        .setPrimaryClip(clip)
}

internal fun Fragment.storeToClipboard(label: String, text: String) {
    requireContext().storeToClipboard(label, text)
}

internal fun Context.showDialog(title: String, message: CharSequence) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .show()
}

internal fun Fragment.showDialog(title: String, message: CharSequence) {
    requireContext().showDialog(title, message)
}

internal fun Fragment.showErrorDialog(message: String?) {
    showDialog(
        "An Error Occured",
        @Suppress("DEPRECATION")
        Html.fromHtml("<b>Exception Message</b>:<br/>$message")
    )
}

internal fun Fragment.hideKeyboard() {
    val activity = requireActivity()
    val imm = activity.getSystemService(
        AppCompatActivity.INPUT_METHOD_SERVICE
    ) as InputMethodManager
    var view = activity.currentFocus
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

internal fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

internal fun Fragment.openUrl(url: String) {
    requireContext().openUrl(url)
}

internal fun logD(message: String) = Log.d(LOG_APP_ID, message)

internal fun logE(message: String) = Log.e(LOG_APP_ID, message)

internal fun getServiceIcon(
    context: Context,
    svgPath: String
) = VectorDrawableCreator.getVectorDrawable(
    context,
    100,
    100,
    24f,
    24f,
    true,
    0f,
    0f,
    listOf(VectorDrawableCreator.PathData(
        svgPath,
        Color.TRANSPARENT,
        Color.parseColor("#F687B3") // pink-400
    ))
)

internal object Safe {
    private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ENCRYPTION_ALGORITHM = "AES"
    private const val HASH_ALGORITHM = "SHA-256"
    private const val IV_LENGTH = 16

    internal fun storeKey(
        context: Context,
        label: String,
        content: String
    ) {
        context.getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
            .edit()
            .putString(label, content)
            .apply()
    }

    internal fun getKey(context: Context, label: String): String {
        return context.getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
            .getString(label, "")!!
    }

    internal fun encryptAndStoreAccessToken(context: Context, accessToken: String) {
        encryptAndStore(context, ACCESS_TOKEN, accessToken)
    }

    internal fun encryptAndStoreUserId(context: Context, userId: String) {
        encryptAndStore(context, USER_ID, userId)
    }

    internal fun encryptAndStoreApiKey(context: Context, apiKey: String) {
        encryptAndStore(context, API_KEY, apiKey)
    }

    private fun encryptAndStore(
        context: Context,
        preference: String,
        value: String
    ) {
        if (value != "") {
            try {
                val secretKey = generateKey(Secrets.getCipherPassword())
                val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(IV_LENGTH)))
                val encryptedValue = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
                val encryptedApiKey = Base64.encodeToString(encryptedValue, Base64.DEFAULT)
                storeKey(context, preference, encryptedApiKey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            storeKey(context, preference, "")
        }
    }

    internal fun getAndDecryptAccessToken(context: Context) = getAndDecrypt(context, ACCESS_TOKEN)

    internal fun getAndDecryptUserId(context: Context) = getAndDecrypt(context, USER_ID)

    internal fun getAndDecryptApiKey(context: Context) = getAndDecrypt(context, API_KEY)

    private fun getAndDecrypt(context: Context, preference: String): String {
        val spf = context.getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
        val encryptedValue = spf.getString(preference, "")!!
        if (encryptedValue.isEmpty()) return ""
        val secretKey = generateKey(Secrets.getCipherPassword())
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(IV_LENGTH)))
        val decryptedBase64Value = Base64.decode(encryptedValue, Base64.DEFAULT)
        val decryptedValue = cipher.doFinal(decryptedBase64Value)
        return String(decryptedValue, Charsets.UTF_8)
    }

    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = password.toByteArray(Charsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, ENCRYPTION_ALGORITHM)
    }
}
