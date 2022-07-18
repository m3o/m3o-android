package com.m3o.mobile.fragments.services.gifs

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.GIFsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.mobile.databinding.FragmentServiceGifsBinding
import com.m3o.mobile.utils.Safe
import com.m3o.mobile.utils.hideKeyboard
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class GIFsFragment : Fragment() {

    private var _binding: FragmentServiceGifsBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceGifsBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.apply {
            layoutManager = GridLayoutManager(myContext, 2)
        }

        binding.gifSearchInputText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard()
                binding.progressBar.visibility = View.VISIBLE
                if (!M3O.isInitialized()) {
                    M3O.initialize(Safe.getAndDecryptApiKey(myContext))
                }

                val search = binding.gifSearchInputText.text.toString()
                lifecycleScope.launch {
                    try {
                        val data = GIFsService.search(search, 20).data
                        if (data != null) {
                            binding.recycler.adapter = GIFsAdapter(myContext, data) { gif, _ ->
//                            val file = cacheAvatar(gif)
//                            val uri = FileProvider.getUriForFile(
//                                myContext,
//                                "${myContext.applicationContext.packageName}.provider",
//                                file
//                            )
//                            val shareIntent = Intent().apply {
//                                action = Intent.ACTION_SEND
//                                putExtra(Intent.EXTRA_STREAM, uri)
//                                type = "image/gif"
//                            }
//
//                            val chooser = Intent.createChooser(shareIntent, null)
//                            @SuppressLint("QueryPermissionsNeeded")
//                            val resInfoList = myContext.packageManager
//                                .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
//                            resInfoList.forEach {
//                                val packageName = it.activityInfo.packageName
//                                myContext.grantUriPermission(
//                                    packageName,
//                                    uri,
//                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
//                                )
//                            }
//                            startActivity(chooser)
                            }
                        }
                        binding.progressBar.visibility = View.INVISIBLE
                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.INVISIBLE
                        MaterialAlertDialogBuilder(myContext)
                            .setTitle("Error")
                            .setMessage(Html.fromHtml("<b>Exception Message</b>:<br/>${e.message}"))
                            .show()
                    }
                }

                return@setOnKeyListener true
            }
            false
        }
    }

    private fun cacheAvatar(gif: GifDrawable): File {
        val dir = File(myContext.cacheDir, "/gifs/")
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                dir.mkdirs()
            }
        }
        val file = File(dir, "sharingGIF.gif")
        try {
            file.createNewFile()
            val bytes = ByteArray(gif.buffer.remaining())
            gif.buffer.get(bytes)
            val out = FileOutputStream(file)
            out.write(bytes)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(myContext, "Sharing GIF failed", Toast.LENGTH_SHORT).show()
        }
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
