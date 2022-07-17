package com.m3o.mobile.fragments.services

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.m3o.mobile.R
import com.m3o.mobile.utils.getServiceIcon

class ServicesAdapter(
    private val context: Context,
    private val data: List<ServicesViewModel>,
    val onClick: (service: String) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]

        holder.nameView.text = entry.service
        val serviceId = entry.service.lowercase().replace(" ", "_")
        holder.image.setImageDrawable(getServiceIcon(context, getSvgByServiceId(serviceId)))
        holder.root.setOnClickListener { onClick(serviceId) }
    }

    override fun getItemCount() = data.size

    private fun getSvgByServiceId(serviceId: String): String {
        val packageName: String = context.packageName
        val resId = context.resources.getIdentifier(serviceId, "string", packageName)
        return context.getString(resId)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: CardView = view.findViewById(R.id.card)
        val image: ImageView = view.findViewById(R.id.service_image)
        val nameView: TextView = view.findViewById(R.id.service_name_view)
    }
}
