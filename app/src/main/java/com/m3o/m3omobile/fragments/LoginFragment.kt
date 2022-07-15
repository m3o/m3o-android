package com.m3o.m3omobile.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.m3o.m3omobile.R
import com.m3o.m3omobile.activities.MainActivity
import com.m3o.m3omobile.api.AccountService
import com.m3o.m3omobile.api.LoginService
import com.m3o.m3omobile.api.Networking
import com.m3o.m3omobile.databinding.FragmentLoginBinding
import com.m3o.m3omobile.utils.EMAIL
import com.m3o.m3omobile.utils.SHARED_PREFERENCE
import com.m3o.m3omobile.utils.Safe
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {
            binding.submitButton.isEnabled = false
            binding.apiKeyButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.mailInputText.text.toString()
            val password = binding.passwordInputText.text.toString()
            lifecycleScope.launch {
                try {
                    val accessToken = LoginService.login(email, password).token.accessToken
                    Log.d("M3O Mobile", "Log in complete")
                    myContext.getSharedPreferences(SHARED_PREFERENCE, MODE_PRIVATE)
                        .edit()
                        .putString(EMAIL, email)
                        .apply()
                    Log.d("M3O Mobile", "Email stored")
                    Safe.encryptAndStoreAccessToken(myContext, accessToken)
                    Log.d("M3O Mobile", "Access token stored")
                    Networking.initializeAuth(accessToken)
                    val userId = AccountService.read(email, password).customer.id
                    Safe.encryptAndStoreUserId(myContext, userId)
                    Log.d("M3O Mobile", "User Id stored")
                    val apiKey = LoginService.createKey().apiKey
                    Safe.encryptAndStoreApiKey(myContext, apiKey)
                    Log.d("M3O Mobile", "API key stored")
                    startActivity(Intent(myContext, MainActivity::class.java))
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    binding.submitButton.isEnabled = true
                    binding.apiKeyButton.isEnabled = true
                    MaterialAlertDialogBuilder(myContext)
                        .setTitle("Error")
                        .setMessage(Html.fromHtml("<b>Exception Message</b>:<br/>${e.message}"))
                        .show()
                }
            }
        }

        binding.apiKeyButton.setOnClickListener {
            @SuppressLint("InflateParams")
            val inputField = layoutInflater.inflate(R.layout.dialog_view_api_key, null)
                .findViewById<TextInputLayout>(R.id.input_field)

            @SuppressLint("InflateParams")
            val inputTextField = inputField.findViewById<TextInputEditText>(R.id.input_field_text)

            MaterialAlertDialogBuilder(myContext)
                .setTitle("Enter your API key")
                .setView(inputField)
                .setPositiveButton(android.R.string.ok, null)
                .create().apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val apiKey = inputTextField.text.toString()

                            if (Regex("[a-zA-Z0-9]+").matches(apiKey) && apiKey.length <= 64) {
                                Safe.encryptAndStoreApiKey(myContext, apiKey)
                                dismiss()
                                Log.d("M3O Mobile", "API key stored")
                                startActivity(Intent(myContext, MainActivity::class.java))
                            } else {
                                inputField.error = "Invalid API key format"
                            }
                        }
                    }
                    show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
