package com.m3o.mobile.fragments.services.urls

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyb3rko.m3okotlin.services.UrlService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.mobile.databinding.FragmentServiceUrlsBinding
import com.m3o.mobile.fragments.services.urls.bottomsheets.URLsBottomSheetEdit
import com.m3o.mobile.fragments.services.urls.bottomsheets.URLsBottomSheetNew
import com.m3o.mobile.utils.*
import kotlinx.coroutines.launch

class URLsFragment : Fragment() {
    private var _binding: FragmentServiceUrlsBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceUrlsBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeM3O()
        binding.progressBar.show()
        lifecycleScope.launch {
            fetchData()
        }

        binding.fab.setOnClickListener {
            val bottomSheet = URLsBottomSheetNew {
                lifecycleScope.launch {
                    binding.progressBar.show()
                    UrlService.shorten(it)
                    fetchData()
                }
            }
            bottomSheet.show(parentFragmentManager, URLsBottomSheetNew.TAG)
        }
    }

    private suspend fun fetchData() {
        try {
            binding.animationView.hide()
            binding.emptyTextView.hide()
            val data = try { UrlService.list().urlPairs } catch (_: Exception) {
                binding.progressBar.hide()
                return
            }
            if (data.isNotEmpty()) {
                binding.recycler.apply {
                    layoutManager = LinearLayoutManager(myContext)
                    adapter = URLsAdapter(
                        (myContext as AppCompatActivity).supportFragmentManager,
                        data,
                        onEdit = { oldDestinationUrl, id ->
                            val bottomSheet = URLsBottomSheetEdit(oldDestinationUrl) {
                                if (oldDestinationUrl != it) {
                                    lifecycleScope.launch {
                                        binding.progressBar.show()
                                        UrlService.update(it, id)
                                        fetchData()
                                    }
                                }
                            }
                            bottomSheet.show(parentFragmentManager, URLsBottomSheetEdit.TAG)
                        },
                        onDelete = {
                            MaterialAlertDialogBuilder(myContext)
                                .setTitle("Delete short URL?")
                                .setMessage("Are you sure you want to delete this short URL?\n\n$it")
                                .setPositiveButton("Yes") { _, _ ->
                                    lifecycleScope.launch {
                                        binding.progressBar.show()
                                        UrlService.delete(shortUrl = it)
                                        fetchData()
                                    }
                                }
                                .show()
                        }
                    )
                    show()
                }
            } else {
                binding.recycler.visibility = View.GONE
                binding.animationView.show()
                binding.emptyTextView.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logE("Fetching and showing URLs failed")
            showErrorDialog(message = e.message)
        }
        binding.progressBar.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
