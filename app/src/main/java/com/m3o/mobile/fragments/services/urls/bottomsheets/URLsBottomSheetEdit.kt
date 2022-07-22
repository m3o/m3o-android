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

class URLsBottomSheetEdit(
    private val destinationUrl: String,
    private val action: (destinationUrl: String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_urls_edit, container, false)
        view.apply {
            val inputView = findViewById<TextInputLayout>(R.id.input_field)
            val inputTextView = findViewById<TextInputEditText>(R.id.input_field_text)
            inputTextView.setText(destinationUrl)
            findViewById<MaterialButton>(R.id.save_button).setOnClickListener {
                val newDestinationUrl = inputTextView.text.toString().trim()
                if (Patterns.WEB_URL.matcher(newDestinationUrl).matches()) {
                    dismiss()
                    action(newDestinationUrl)
                } else {
                    inputView.error = "Invalid URL"
                }
            }
        }

        return view
    }

    companion object {
        const val TAG = "Edit Short URL"
    }
}
