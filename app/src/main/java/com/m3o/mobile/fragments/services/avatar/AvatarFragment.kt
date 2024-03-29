package com.m3o.mobile.fragments.services.avatar

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyb3rko.m3okotlin.services.AvatarService
import com.m3o.mobile.databinding.FragmentServiceAvatarBinding
import com.m3o.mobile.utils.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AvatarFragment : Fragment() {

    private var _binding: FragmentServiceAvatarBinding? = null
    private lateinit var myContext: Context
    private lateinit var avatar: Bitmap
    private var avatarFile: File? = null
    private var avatarName = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceAvatarBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.usernameInputText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard()
                binding.progressBar.show()
                binding.avatarView.setImageDrawable(getServiceIcon(myContext, getSvg()))
                binding.saveButton.hide()
                binding.shareButton.hide()

                initializeM3O()
                avatarName = binding.usernameInputText.text.toString().trim()
                val format = if (binding.jpegButton.isChecked) "jpeg" else "png"
                val gender = if (binding.maleButton.isChecked) "male" else "female"

                avatarFile = null

                lifecycleScope.launch {
                    try {
                        val data = try {
                            AvatarService.generate(
                                format,
                                gender,
                                false,
                                avatarName
                            )
                        } catch (_: Exception) {
                            binding.progressBar.hide()
                            return@launch
                        }

                        val base64 = data.base64.substring(data.base64.indexOf(","))
                        val decodedString = Base64.decode(base64, Base64.DEFAULT)
                        avatar = BitmapFactory.decodeByteArray(
                            decodedString,
                            0,
                            decodedString.size
                        )
                        binding.avatarView.setImageBitmap(avatar)
                        binding.saveButton.show()
                        binding.shareButton.show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        logE("Loading avatar failed")
                        showErrorDialog(message = e.message)
                    }
                    binding.progressBar.hide()
                }
                return@OnKeyListener true
            }
            false
        })

        binding.avatarView.setImageDrawable(getServiceIcon(myContext, getSvg()))

        binding.saveButton.setOnClickListener {
            if (this@AvatarFragment::avatar.isInitialized) {
                val imageType = if (binding.jpegButton.isChecked) {
                    Bitmap.CompressFormat.JPEG
                } else {
                    Bitmap.CompressFormat.PNG
                }
                saveAvatar(imageType)
            }
        }

        binding.shareButton.setOnClickListener {
            if (this@AvatarFragment::avatar.isInitialized) {
                val imageType = if (binding.jpegButton.isChecked) {
                    Bitmap.CompressFormat.JPEG
                } else {
                    Bitmap.CompressFormat.PNG
                }
                val imageType2 = if (binding.jpegButton.isChecked) {
                    "jpeg"
                } else {
                    "png"
                }
                if (avatarFile == null) {
                    avatarFile = cacheAvatar(imageType, imageType2)
                }
                val fileUri = FileProvider.getUriForFile(
                    myContext,
                    "${myContext.applicationContext.packageName}.provider",
                    avatarFile!!
                )
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_STREAM,
                        fileUri
                    )
                    type = "image/$imageType2"
                }

                val chooser = Intent.createChooser(shareIntent, null)
                @SuppressLint("QueryPermissionsNeeded")
                val resInfoList = myContext.packageManager
                    .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
                resInfoList.forEach {
                    val packageName = it.activityInfo.packageName
                    myContext.grantUriPermission(
                        packageName,
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                startActivity(chooser)
            }
        }
    }

    private fun cacheAvatar(format: Bitmap.CompressFormat, format2: String): File {
        val dir = File(myContext.cacheDir, "/images/")
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                dir.mkdirs()
            }
        }
        val file = File(dir, "avatar_$avatarName.$format2")
        try {
            file.createNewFile()
            val out = FileOutputStream(file)
            avatar.compress(format, 100, out)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Sharing avatar failed"
            logE(errorMessage)
            showToast(errorMessage)
        }
        return file
    }

    private fun saveAvatar(imageType: Bitmap.CompressFormat) {
        val fileName = "avatar_$avatarName.${imageType.name}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveAvatarSinceQ(fileName, imageType)
        } else {
            executeWithFileAccess("save the avatar") {
                saveAvatarBelowQ(fileName, imageType)
            }
        }
    }

    private fun saveAvatarSinceQ(fileName: String, format: Bitmap.CompressFormat) {
        val path = File(Environment.DIRECTORY_PICTURES, "M3O").toString()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/${format.name}")
            put(MediaStore.MediaColumns.RELATIVE_PATH, path)
        }
        val uri = myContext.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        try {
            val out = if (uri != null) {
                myContext.contentResolver.openOutputStream(uri)
            } else {
                null
            }
            if (out != null) {
                avatar.compress(format, 100, out)
                out.close()
                showToast("Avatar saved to gallery")
            } else throw Exception("Output stream is null, saving avatar not possible")
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Saving avatar failed"
            logE(errorMessage)
            showToast(errorMessage)
        }
    }

    private fun saveAvatarBelowQ(fileName: String, format: Bitmap.CompressFormat) {
        val path = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "M3O"
        )

        try {
            if (!path.exists()) path.mkdirs()
            val file = File(path, fileName)
            file.createNewFile()
            val out = FileOutputStream(file)
            avatar.compress(format, 100, out)
            out.close()
            showToast("Avatar saved to gallery")
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = "Saving avatar failed"
            logE(errorMessage)
            showToast(errorMessage)
        }
    }

    private fun getSvg(): String {
        val packageName: String = myContext.packageName
        val resId = myContext.resources.getIdentifier("avatar", "string", packageName)
        return myContext.getString(resId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
