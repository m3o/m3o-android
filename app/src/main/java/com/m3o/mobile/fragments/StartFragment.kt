package com.m3o.mobile.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m3o.mobile.R
import com.m3o.mobile.databinding.FragmentStartBinding
import com.m3o.mobile.utils.logE
import com.m3o.mobile.utils.openUrl
import com.m3o.mobile.utils.showDialog
import com.mukesh.tamperdetector.Installer
import com.mukesh.tamperdetector.guardDebugger
import com.mukesh.tamperdetector.verifyInstaller

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        myContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            openUrl("https://m3o.com/register")
        }

        val verifiedInstaller = myContext.verifyInstaller(Installer.GOOGLE_PLAY_STORE)
        if (verifiedInstaller != null && verifiedInstaller) {
            guardDebugger({
                showDebuggerError()
            }, {
                enableLoginButton()
            })
        } else {
            showWrongInstallerError()
        }
    }

    private fun enableLoginButton() {
        binding.loginButton.isEnabled = true
        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.LoginFragment)
        }
    }

    private fun showWrongInstallerError() {
        logE("Insecure App Access: Wrong installer found, disallowing app usage.")
        showDialog(
            "Insecure App Access",
            "The app version you are using is not downloaded from the Google Play Store.\n\n" +
                    "Uninstall the app from your device and install the app from the Google Play " +
                    "Store.",
            { openUrl(getString(R.string.about_play_store_link)) },
            "Download"
        )
    }

    private fun showDebuggerError() {
        logE("Insecure App Access: Debugger found, disallowing app usage.")
        showDialog(
            "Insecure App Access",
            "An app debugger was found.\nDeactive it and restart the app."
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
