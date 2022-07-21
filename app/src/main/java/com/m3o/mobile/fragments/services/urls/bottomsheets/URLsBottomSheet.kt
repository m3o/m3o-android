package com.m3o.mobile.fragments.services.urls.bottomsheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.m3o.mobile.R
import com.m3o.mobile.utils.openUrl

class URLsBottomSheet(
    private val shortURL: String,
    private val longURL: String,
    private val onDelete: (shortURL: String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_urls, container, false)
        view.apply {
            findViewById<LinearLayout>(R.id.open_short).setOnClickListener {
                openUrl(shortURL)
            }
            findViewById<LinearLayout>(R.id.open_long).setOnClickListener {
                openUrl(longURL)
            }
            findViewById<LinearLayout>(R.id.share_short).setOnClickListener {
                openShareChooser(shortURL)
            }
            findViewById<LinearLayout>(R.id.share_long).setOnClickListener {
                openShareChooser(longURL)
            }
            findViewById<LinearLayout>(R.id.delete).setOnClickListener {
                dismiss()
                onDelete(shortURL)
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