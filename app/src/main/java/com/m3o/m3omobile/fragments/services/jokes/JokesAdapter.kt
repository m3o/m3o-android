package com.m3o.m3omobile.fragments.services.jokes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyb3rko.m3okotlin.data.JokesResponse
import com.google.android.material.card.MaterialCardView
import com.m3o.m3omobile.R

class JokesAdapter(
    private val data: List<JokesResponse.Joke>,
    private val onClick: (joke: String) -> Unit
) : RecyclerView.Adapter<JokesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_jokes, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]

        holder.apply {
            if (entry.title != "") {
                titleView.text = entry.title
            } else {
                titleView.visibility = View.GONE
            }
            bodyView.text = entry.body
            sourceView.text = entry.source

            root.setOnClickListener {
                onClick(entry.body)
            }
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: MaterialCardView = view.findViewById(R.id.card)
        val titleView: TextView = view.findViewById(R.id.title_view)
        val bodyView: TextView = view.findViewById(R.id.body_view)
        val sourceView: TextView = view.findViewById(R.id.source_view)
    }
}
