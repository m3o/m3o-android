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
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.URLsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.mobile.databinding.FragmentServiceUrlsBinding
import com.m3o.mobile.fragments.services.urls.bottomsheets.URLsBottomSheetNew
import com.m3o.mobile.utils.Safe
import com.m3o.mobile.utils.logE
import com.m3o.mobile.utils.showErrorDialog
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

        if (!M3O.isInitialized()) {
            M3O.initialize(Safe.getAndDecryptApiKey(myContext))
        }
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            fetchData()
        }

        binding.fab.setOnClickListener {
            val bottomSheet = URLsBottomSheetNew {
                lifecycleScope.launch {
                    binding.progressBar.visibility = View.VISIBLE
                    URLsService.shorten(it)
                    fetchData()
                }
            }
            bottomSheet.show(parentFragmentManager, URLsBottomSheetNew.TAG)
        }
    }

    private suspend fun fetchData() {
        try {
            binding.animationView.visibility = View.GONE
            binding.emptyTextView.visibility = View.GONE
            val data = URLsService.list().urlPairs
            if (data.isNotEmpty()) {
                binding.recycler.apply {
                    layoutManager = LinearLayoutManager(myContext)
                    adapter = URLsAdapter(
                        (myContext as AppCompatActivity).supportFragmentManager,
                        data
                    ) {
                        MaterialAlertDialogBuilder(myContext)
                            .setTitle("Delete short URL?")
                            .setMessage("Are you sure you want to delete this short URL?\n\n$it")
                            .setPositiveButton("Yes") { _, _ ->
                                lifecycleScope.launch {
                                    binding.progressBar.visibility = View.VISIBLE
                                    URLsService.delete(it)
                                    fetchData()
                                }
                            }
                            .show()
                    }
                }
            } else {
                binding.animationView.visibility = View.VISIBLE
                binding.emptyTextView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logE("Fetching and showing URLs failed")
            showErrorDialog(e.message)
        }
        binding.progressBar.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
