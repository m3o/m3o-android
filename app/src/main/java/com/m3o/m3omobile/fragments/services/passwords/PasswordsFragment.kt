package com.m3o.m3omobile.fragments.services.passwords

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.m3o.m3omobile.databinding.FragmentServicePasswordsBinding
import com.m3o.m3omobile.utils.showToast
import com.m3o.m3omobile.utils.storeToClipboard
import java.security.SecureRandom

class PasswordsFragment : Fragment() {
    private var _binding: FragmentServicePasswordsBinding? = null
    private lateinit var myContext: Context

    private val lowercase = "abcdefghijklmnopqrstuvwxyz"
    private val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numbers = "0123456789"
    private val special = "!@#$%&*"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicePasswordsBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lengthSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                binding.resultButton.text = "Generate"

                val value = slider.value.toInt()
                binding.lengthView.text = "Length: $value"
            }
        })

        binding.resultButton.apply {
            setOnClickListener {
                text = generate(
                    binding.lengthSlider.value.toInt(),
                    binding.lowercaseSwitch.isChecked,
                    binding.uppercaseSwitch.isChecked,
                    binding.numbersSwitch.isChecked,
                    binding.specialSwitch.isChecked
                )
            }
            setOnLongClickListener {
                if (text != "Generate") {
                    storeToClipboard("Password", text.toString())
                    showToast("Copied to clipboard")
                    true
                } else false
            }
        }

        binding.apply {
            listOf(
                lowercaseSwitch,
                uppercaseSwitch,
                numbersSwitch,
                specialSwitch
            ).forEach {
                it.setOnClickListener {
                    binding.resultButton.text = "Generate"
                }
            }
        }
    }

    private fun generate(
        length: Int,
        useLowercase: Boolean,
        useUppercase: Boolean,
        useNumbers: Boolean,
        useSpecial: Boolean
    ): String {
        if (!useLowercase && !useUppercase && !useNumbers && !useSpecial) {
            return ""
        }
        var chars = ""
        if (useLowercase) chars += lowercase
        if (useUppercase) chars += uppercase
        if (useNumbers) chars += numbers
        if (useSpecial) chars += special

        var password = ""
        val random = SecureRandom.getInstance("SHA1PRNG")
        repeat(length) {
            password += chars[random.nextInt(chars.length)]
        }

        return if (evaluate(password, useLowercase, useUppercase, useNumbers, useSpecial)) {
            password
        } else {
            generate(length, useLowercase, useUppercase, useNumbers, useSpecial)
        }
    }

    private fun evaluate(
        password: String,
        useLowercase: Boolean,
        useUppercase: Boolean,
        useNumbers: Boolean,
        useSpecial: Boolean
    ): Boolean {
        if (useLowercase && !password.contains("[a-z]".toRegex())) return false
        if (useUppercase && !password.contains("[A-Z]".toRegex())) return false
        if (useNumbers && !password.contains("[0-9]".toRegex())) return false
        if (useSpecial && !password.contains("[$special]".toRegex())) return false
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
