package com.m3o.mobile.fragments.about

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.m3o.mobile.R
import com.m3o.mobile.utils.openUrl

class AboutAnimationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myContext = requireContext()
        val information = listOf(
            Triple("Empty box", "Hoai Le", "https://lottiefiles.com/629-empty-box")
        )
        val view = ScrollView(myContext)
        val linearLayout = LinearLayout(myContext)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPaddingRelative(50, 50, 50, 0)
        information.forEach {
            val textView = TextView(myContext)
            textView.textSize = 18f
            textView.setPaddingRelative(50, 50, 50, 50)
            val text = getString(R.string.about_animations_description, it.first, it.second)
            val spannableString = SpannableString(text)
            val clickableSpan = object: ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrl(it.third)
                }
            }
            spannableString.setSpan(
                clickableSpan,
                0,
                it.first.length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
            val card = MaterialCardView(myContext)
            card.setMargins(0, 0, 0, 20)
            card.addView(textView)
            linearLayout.addView(card)
        }
        view.addView(linearLayout)
        return view
    }

    private fun View.setMargins(start: Int, top: Int, end: Int, bottom: Int) {
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.MarginLayoutParams.MATCH_PARENT,
            ViewGroup.MarginLayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(start, top, end, bottom)
        this.layoutParams = layoutParams
        this.requestLayout()
    }
}
