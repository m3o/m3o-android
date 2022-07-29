package com.m3o.mobile.fragments.services.carbon

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyb3rko.m3okotlin.data.CarbonResponse
import com.cyb3rko.m3okotlin.services.CarbonService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.m3o.mobile.R
import com.m3o.mobile.databinding.FragmentServiceCarbonBinding
import com.m3o.mobile.utils.*
import kotlinx.coroutines.launch

class CarbonFragment : Fragment() {
    private var _binding: FragmentServiceCarbonBinding? = null
    private lateinit var myContext: Context
    private var data: List<CarbonResponse.CarbonProject> = listOf()
    private var multiRequests = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceCarbonBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.offsetButton.setOnClickListener {
            binding.offsetButton.isEnabled = false
            binding.progressBar.show()
            binding.projectsView.hide()
            binding.recycler.hide()

            initializeM3O()
            if (multiRequests in 0..1) {
                singleOffset()
            } else {
                multiOffsetLauncher(multiRequests)
            }
        }

        binding.offsetButton.setOnLongClickListener {
            showOffsetAmountDialog()
            true
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(myContext)
            adapter = CarbonAdapter(data)
        }

        showCounter()
    }

    private suspend fun offset1Kg(skipResult: Boolean = false): Boolean {
        var successful = false
        try {
            val response = try { CarbonService.offset() } catch (_: Exception) {
                binding.progressBar.hide()
                binding.offsetButton.isEnabled = true
                return false
            }
            binding.konfettiView.start(ConfettiPresets.parade())
            increaseCounter(response.units)
            if (!skipResult) {
                data = response.projects
                binding.projectsView.show()
                binding.recycler.apply {
                    layoutManager = LinearLayoutManager(myContext)
                    adapter = CarbonAdapter(data)
                }
                binding.recycler.show()
            }
            successful = true
        } catch (e: Exception) {
            e.printStackTrace()
            logE("Purchasing 1 Kg of carbon offsets failed")
            showErrorDialog(message = e.message)
        }
        if (!skipResult) {
            binding.progressBar.hide()
            binding.offsetButton.isEnabled = true
        }
        return successful
    }

    private fun singleOffset() = lifecycleScope.launch {
        offset1Kg()
    }

    private fun multiOffsetLauncher(amount: Int) {
        if (amount > 0) {
            lifecycleScope.launch {
                binding.multiRequestView.text = "0/$multiRequests Offsets"
                multiOffset(amount)
            }
        }
    }

    private suspend fun multiOffset(amount: Int) {
        if (amount > 0) {
            val skipResult = amount != 1
            if (offset1Kg(skipResult)) {
                val finishedRequests = multiRequests - amount + 1
                binding.multiRequestView.text = "$finishedRequests/$multiRequests Offsets"
                multiOffset(amount - 1)
            } else {
                binding.multiRequestView.text = ""
                binding.progressBar.hide()
                binding.offsetButton.isEnabled = true
            }
        } else {
            binding.multiRequestView.text = ""
            binding.progressBar.hide()
            binding.offsetButton.isEnabled = true
        }
    }

    private fun getCounter() = Safe.getInt(myContext, CARBON_OFFSET)

    private fun increaseCounter(amount: Int) {
        var total = getCounter()
        total = total.plus(amount)
        Safe.storeInt(myContext, CARBON_OFFSET, total)
        showCounter(total)
    }

    private fun showCounter(count: Int? = null) {
        if (count != null) {
            binding.counter.text = "Total: $count kg CO2"
        } else {
            binding.counter.text = "Total: ${getCounter()} kg CO2"
        }
    }

    private fun showOffsetAmountDialog() {
        @SuppressLint("InflateParams")
        val inputField = layoutInflater.inflate(R.layout.dialog_view_carbon_offset, null)
            .findViewById<TextInputLayout>(R.id.input_field)

        @SuppressLint("InflateParams")
        val inputTextField = inputField.findViewById<TextInputEditText>(R.id.input_field_text)

        MaterialAlertDialogBuilder(myContext)
            .setTitle("Configure the offset amount")
            .setView(inputField)
            .setPositiveButton(android.R.string.ok, null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val amount = inputTextField.text.toString()

                        if (Regex("[0-9]+").matches(amount) && amount.toInt() > 0) {
                            multiRequests = amount.toInt()
                            binding.offsetButton.text = "Offset $multiRequests kg"
                            dismiss()
                        } else {
                            inputField.error = "Invalid amount"
                        }
                    }
                }
                show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
