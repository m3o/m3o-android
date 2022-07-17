package com.m3o.mobile.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.mobile.activities.StartActivity
import com.m3o.mobile.api.AccountService
import com.m3o.mobile.api.Networking
import com.m3o.mobile.databinding.FragmentAccountBinding
import com.m3o.mobile.utils.*
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private lateinit var myContext: Context
    private lateinit var apiCardClickListener: View.OnClickListener
    private var timerCounter = 10

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = myContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
            .getString(EMAIL, "")!!

        if (email.isNotEmpty()) {
            binding.emailView.text = email
            binding.balanceCard.visibility = View.VISIBLE
            binding.emailCard.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    Networking.initializeAuth(Safe.getAndDecryptAccessToken(myContext))
                    val balance = AccountService.balance(Safe.getAndDecryptUserId(myContext))
                    binding.balanceView.text = (balance.currentBalance.toFloat() / 1000000).toString()
                    binding.progressBar.visibility = View.INVISIBLE
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.INVISIBLE
                    MaterialAlertDialogBuilder(myContext)
                        .setTitle("Error")
                        .setMessage(Html.fromHtml("<b>Exception Message</b>:<br/>${e.message}"))
                        .show()
                }
            }
        }

        val apiCardClickShownListener = View.OnClickListener {
            timerCounter = 0
            resetApiCard()
        }

        apiCardClickListener = View.OnClickListener {
            MaterialAlertDialogBuilder(myContext)
                .setTitle("Copy/Show API Key")
                .setMessage("Do you really want to copy/show your API key?")
                .setPositiveButton("Show") { _, _ ->
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                    setApiCardClickListener(apiCardClickShownListener)
                    binding.keyViewHidden.visibility = View.INVISIBLE
                    binding.keyView.visibility = View.VISIBLE
                    binding.keyView.text = Safe.getAndDecryptApiKey(myContext)

                    val timerHandler = Handler(Looper.getMainLooper())
                    timerCounter = 10
                    object: Runnable {
                        override fun run() {
                            if (_binding != null) {
                                if (timerCounter < 10) {
                                    binding.timerView.text = "0:0$timerCounter"
                                } else {
                                    binding.timerView.text = "0:$timerCounter"
                                }

                                timerCounter--
                                if (timerCounter >= 0) {
                                    timerHandler.postDelayed(this, 1000)
                                } else {
                                    resetApiCard()
                                }
                            }
                        }
                    }.apply {
                        timerHandler.postDelayed(this, 0)
                    }
                }
                .setNeutralButton("Copy") { _, _ ->
                    storeToClipboard("M3O API Key", Safe.getAndDecryptApiKey(myContext))
                    showToast("Copied to clipboard")
                }
                .show()
        }

        binding.apiKeyCard.setOnClickListener(apiCardClickListener)

        binding.logoutButton.setOnClickListener {
            myContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .edit()
                .putString(EMAIL, "")
                .apply()
            Safe.encryptAndStoreAccessToken(myContext, "")
            Safe.encryptAndStoreUserId(myContext, "")
            Safe.encryptAndStoreApiKey(myContext, "")
            requireActivity().finish()
            startActivity(Intent(myContext, StartActivity::class.java))
        }
    }

    private fun setApiCardClickListener(clickListener: View.OnClickListener? = apiCardClickListener) {
        binding.apiKeyCard.setOnClickListener(clickListener)
    }

    private fun resetApiCard() {
        if (_binding != null) {
            binding.keyViewHidden.visibility = View.VISIBLE
            binding.keyView.visibility = View.INVISIBLE
            binding.timerView.text = ""
            binding.keyView.text = ""
            setApiCardClickListener()
        }
    }

    override fun onStop() {
        super.onStop()
        resetApiCard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
