package com.m3o.mobile.fragments.services.urls

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.cyb3rko.m3okotlin.data.URLsListResponse
import com.google.android.material.card.MaterialCardView
import com.m3o.mobile.R
import com.m3o.mobile.fragments.services.urls.bottomsheets.URLsBottomSheet
import java.text.SimpleDateFormat

class URLsAdapter(
    private val fragmentManager: FragmentManager,
    private val data: List<URLsListResponse.URL>
) : RecyclerView.Adapter<URLsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_urls, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]

        val slug = entry.shortURL.split("/").last()
        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val cutValue = entry.created.dropLastWhile { it != '.' }
        val date = dateFormat.parse(cutValue.dropLast(1))

        var created = ""
        if (date != null) {
            @SuppressLint("SimpleDateFormat")
            val dateFormat2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Z'")
            created = dateFormat2.format(date)
        }

        holder.apply {
            slugView.text = slug
            destinationView.text = entry.destinationURL
            counterView.text = "Clicks: ${entry.hitCount}"
            dateView.text = created

            root.setOnClickListener {
                val bottomSheet = URLsBottomSheet(entry.shortURL, entry.destinationURL)
                bottomSheet.show(fragmentManager, URLsBottomSheet.TAG)
            }
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: MaterialCardView = view.findViewById(R.id.card)
        val slugView: TextView = view.findViewById(R.id.slug_view)
        val destinationView: TextView = view.findViewById(R.id.destination_view)
        val counterView: TextView = view.findViewById(R.id.counter_view)
        val dateView: TextView = view.findViewById(R.id.date_view)
    }
}
