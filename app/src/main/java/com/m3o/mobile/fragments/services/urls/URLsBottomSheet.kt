package com.m3o.mobile.fragments.services.urls

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m3o.mobile.R

class URLsBottomSheet(
    private val shortURL: String,
    private val longURL: String
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_urls, container, false)
        view.apply {
            findViewById<LinearLayout>(R.id.open_short).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shortURL))
                startActivity(intent)
            }
            findViewById<LinearLayout>(R.id.open_long).setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(longURL))
                startActivity(intent)
            }
            findViewById<LinearLayout>(R.id.share_short).setOnClickListener {
                openShareChooser(shortURL)
            }
            findViewById<LinearLayout>(R.id.share_long).setOnClickListener {
                openShareChooser(longURL)
            }
        }
        return view
    }

    private fun openShareChooser(url: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }

    companion object {
        const val TAG = "Short URLs Actions"
    }
}
