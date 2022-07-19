package com.m3o.mobile.fragments.services.carbon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyb3rko.m3okotlin.data.CarbonResponse
import com.m3o.mobile.R

class CarbonAdapter(
    private val data: List<CarbonResponse.CarbonProject>
) : RecyclerView.Adapter<CarbonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_carbon_project, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]

        var percentages = "%.6f".format(entry.tonnes)
        percentages = percentages.dropLastWhile { it == '0' }

        holder.apply {
            nameView.text = entry.name
            percentageView.text = "${entry.percentage} %"
            tonnesView.text = "$percentages tonnes CO2"
        }
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.name_view)
        val percentageView: TextView = view.findViewById(R.id.percentage_view)
        val tonnesView: TextView = view.findViewById(R.id.tonnes_view)
    }
}
