package com.m3o.mobile.fragments.services.urls.bottomsheets

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.m3o.mobile.R

class URLsBottomSheetNew(
    private val action: (destinationUrl: String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_urls_new, container, false)
        view.apply {
            val inputView = findViewById<TextInputLayout>(R.id.input_field)
            val inputTextView = findViewById<TextInputEditText>(R.id.input_field_text)
            findViewById<MaterialButton>(R.id.save_button).setOnClickListener {
                val destinationUrl = inputTextView.text.toString().trim()
                if (Patterns.WEB_URL.matcher(destinationUrl).matches()) {
                    dismiss()
                    action(destinationUrl)
                } else {
                    inputView.error = "Invalid URL"
                }
            }
        }

        return view
    }

    companion object {
        const val TAG = "Add Short URL"
    }
}
