package com.m3o.m3omobile.fragments.services.jokes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cyb3rko.m3okotlin.M3O
import com.cyb3rko.m3okotlin.services.JokesService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m3o.m3omobile.databinding.FragmentServiceJokesBinding
import com.m3o.m3omobile.utils.Safe
import kotlinx.coroutines.launch

class JokesFragment : Fragment() {
    private var _binding: FragmentServiceJokesBinding? = null
    private lateinit var myContext: Context

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceJokesBinding.inflate(inflater, container, false)
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
            lifecycleScope.launch {
                val data = JokesService.random(10).jokes
                binding.recycler.apply {
                    layoutManager = LinearLayoutManager(myContext)
                    adapter = JokesAdapter(data) {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, it)
                            type = "text/plain"
                        }

                        startActivity(Intent.createChooser(shareIntent, null))
                    }
                }
                binding.progressBar.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.INVISIBLE
            MaterialAlertDialogBuilder(myContext)
                .setTitle("Error")
                .setMessage(Html.fromHtml("<b>Exception Message</b>:<br/>${e.message}"))
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
