package com.m3o.mobile.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.m3o.mobile.R
import com.m3o.mobile.activities.MainActivity
import com.m3o.mobile.api.AccountService
import com.m3o.mobile.api.LoginService
import com.m3o.mobile.api.Networking
import com.m3o.mobile.databinding.FragmentLoginBinding
import com.m3o.mobile.utils.*
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
            binding.passwordInput.error = null
            val email = binding.mailInputText.text.toString()
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.mailInput.error = "Invalid email address"
                return@setOnClickListener
            }
            binding.mailInput.error = null

            val password = binding.passwordInputText.text.toString()
            if (password.isEmpty()) {
                binding.passwordInput.error = "Empty password"
                return@setOnClickListener
            }

            binding.submitButton.isEnabled = false
            binding.apiKeyButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val token = LoginService.login(email, password).token
                    val accessToken = token.accessToken
                    val refreshToken = token.refreshToken
                    println(refreshToken)
                    println(accessToken)
                    Log.d("M3O Mobile", "Log in complete")

                    Safe.storeKey(myContext, EMAIL, email)
                    Log.d("M3O Mobile", "Email stored")

                    Safe.encryptAndStoreAccessToken(myContext, accessToken)
                    Safe.storeKey(myContext, REFRESH_TOKEN, refreshToken)
                    Log.d("M3O Mobile", "Access and refresh tokens stored")

                    Networking.initializeAuth(accessToken)
                    val userId = AccountService.read(email, password).customer.id
                    Safe.encryptAndStoreUserId(myContext, userId)
                    Log.d("M3O Mobile", "User Id stored")

                    val apiKey = LoginService.createKey().apiKey
                    Safe.encryptAndStoreApiKey(myContext, apiKey)
                    Log.d("M3O Mobile", "API key stored")

                    val intent = Intent(myContext, MainActivity::class.java).apply {
                        putExtra(SKIP_REFRESH, true)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.submitButton.isEnabled = true
                    binding.apiKeyButton.isEnabled = true
                    showErrorDialog(e.message)
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
