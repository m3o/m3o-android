package com.m3o.mobile.fragments.services.bitcoin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.BitcoinService
import com.m3o.mobile.R
import com.m3o.mobile.databinding.FragmentServiceBitcoinBinding
import com.m3o.mobile.utils.Safe
import com.m3o.mobile.utils.logE
import com.m3o.mobile.utils.showErrorDialog
import kotlinx.coroutines.launch

class BitcoinFragment : Fragment() {
    private var _binding: FragmentServiceBitcoinBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBitcoinBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.progressBar.visibility = View.VISIBLE
            if (!M3O.isInitialized()) {
                M3O.initialize(Safe.getAndDecryptApiKey(myContext))
            }
            fetchData()

            binding.refreshLayout.apply {
                setProgressBackgroundColorSchemeResource(R.color.refreshLayoutBackground)
                setColorSchemeResources(R.color.refreshLayoutArrow)
                setOnRefreshListener {
                    binding.progressBar.visibility = View.VISIBLE
                    isRefreshing = false
                    fetchData()
                }
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.INVISIBLE
            showErrorDialog(e.message)
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            try {
                val data = BitcoinService.price().price
                @SuppressLint("SetTextI18n")
                binding.priceView.text = "$data $"
            } catch (e: Exception) {
                e.printStackTrace()
                logE("Fetching Bitcoin price failed")
                showErrorDialog(e.message)
            }
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
