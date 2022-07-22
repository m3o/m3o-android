package com.m3o.mobile.fragments.services.gifs

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cyb3rko.m3okotlin.data.GifsResponse
import com.m3o.mobile.R

class GIFsAdapter(
    private val context: Context,
    private val data: List<GifsResponse.Gif>,
    val onClick: (gif: GifDrawable, position: Int) -> Unit
) : RecyclerView.Adapter<GIFsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_gifs, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = data[position]

        val request = Glide.with(context)
            .asGif()
            .load(entry.images.original.url)

        request.into(holder.image)
        request.into(object: CustomTarget<GifDrawable>() {
            override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                holder.root.setOnClickListener {
                    onClick(resource, holder.adapterPosition)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) { }
        })
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: CardView = view.findViewById(R.id.card)
        val image: ImageView = view.findViewById(R.id.image)
    }
}
