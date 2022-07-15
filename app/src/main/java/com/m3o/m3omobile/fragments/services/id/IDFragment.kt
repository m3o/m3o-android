package com.m3o.m3omobile.fragments.services.id

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.IDService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.m3omobile.databinding.FragmentServiceIdBinding
import com.m3o.m3omobile.utils.Safe
import com.m3o.m3omobile.utils.showToast
import com.m3o.m3omobile.utils.storeToClipboard
import kotlinx.coroutines.launch

class IDFragment : Fragment() {
    private var _binding: FragmentServiceIdBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceIdBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroup.setOnCheckedChangeListener { _, i ->
            binding.resultButton.text = "Generate"
        }

        binding.resultButton.apply {
            setOnClickListener {
                fetchId(binding.radioGroup.checkedRadioButtonId)
            }
            setOnLongClickListener {
                if (text != "Generate") {
                    storeToClipboard("ID", text.toString())
                    showToast("Copied to clipboard")
                    true
                } else false
            }
        }
    }

    private fun fetchId(checkedButtonId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        if (!M3O.isInitialized()) {
            M3O.initialize(Safe.getAndDecryptApiKey(myContext))
        }
        lifecycleScope.launch {
            val idType = when (checkedButtonId) {
                binding.radio1.id -> {
                    IDType.UUID
                }
                binding.radio2.id -> {
                    IDType.ULID
                }
                binding.radio3.id -> {
                    IDType.KSUID
                }
                binding.radio4.id -> {
                    IDType.XID
                }
                binding.radio5.id -> {
                    IDType.NANOID
                }
                binding.radio6.id -> {
                    IDType.ShortID
                }
                binding.radio7.id -> {
                    IDType.Snowflake
                }
                binding.radio8.id -> {
                    IDType.Bigflake
                }
                else -> null
            }

            if (idType != null) {
                try {
                    val idResponse = IDService.generate(idType.typeName)
                    if (idType.typeName == idResponse.type) {
                        binding.resultButton.text = idResponse.id
                    } else {
                        showError()
                    }
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(myContext)
                        .setTitle("Error")
                        .setMessage(Html.fromHtml("<b>Exception Message</b>:<br/>${e.message}"))
                        .show()
                }
                binding.progressBar.visibility = View.INVISIBLE
            } else {
                binding.progressBar.visibility = View.INVISIBLE
                showError()
            }
        }
    }

    private fun showError() {
        MaterialAlertDialogBuilder(myContext)
            .setTitle("Error")
            .setMessage("ID generation failed")
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class IDType(val typeName: String) {
        UUID("uuid"),
        ULID("ulid"),
        KSUID("ksuid"),
        XID("xid"),
        NANOID("nanoid"),
        ShortID("shortid"),
        Snowflake("snowflake"),
        Bigflake("bigflake")
    }
}
