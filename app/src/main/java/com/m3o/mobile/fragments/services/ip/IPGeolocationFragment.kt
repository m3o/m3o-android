package com.m3o.mobile.fragments.services.ip

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyb3rko.m3okotlin.services.IpGeolocationService
import com.m3o.mobile.R
import com.m3o.mobile.databinding.FragmentServiceIpGeolocationBinding
import com.m3o.mobile.utils.hideKeyboard
import com.m3o.mobile.utils.initializeM3O
import com.m3o.mobile.utils.logE
import com.m3o.mobile.utils.showErrorDialog
import kotlinx.coroutines.launch

class IPGeolocationFragment : Fragment() {
    private var _binding: FragmentServiceIpGeolocationBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceIpGeolocationBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ipAddressInputText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard()
                binding.progressBar.show()
                initializeM3O()

                val ip = binding.ipAddressInputText.text.toString()
                lifecycleScope.launch {
                    try {
                        val data = try {
                            IpGeolocationService.lookup(ip)
                        } catch (_: Exception) {
                            binding.progressBar.hide()
                            return@launch
                        }
                        var output = mutableListOf<Pair<String, String>>()
                        if (data.asn != null) {
                            output.add("ASN" to data.asn.toString())
                        }
                        if (data.city != null) {
                            output.add("City" to data.city!!)
                        }
                        output.add("Country" to data.country)
                        output.add("Timezone" to data.timezone)
                        output.add("Continent" to data.continent)
                        output.add("Latitude" to data.latitude.toString())
                        output.add("Longitude" to data.longitude.toString())

                        output.forEach {
                            val infoView = layoutInflater.inflate(
                                R.layout.item_service_ip_info,
                                null
                            )
                            val spaceView = Space(myContext).apply {
                                layoutParams = ViewGroup.LayoutParams(0, 25)
                            }
                            infoView.findViewById<TextView>(R.id.name_view).text = it.first
                            infoView.findViewById<TextView>(R.id.value_view).text = it.second
                            binding.informationView.addView(infoView)
                            binding.informationView.addView(spaceView)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        logE("Looking up IP and showing results failed")
                        showErrorDialog(message = e.message)
                    }
                    binding.progressBar.hide()
                }
                return@setOnKeyListener true
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
