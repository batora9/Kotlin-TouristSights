package com.example.touristsights

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SightAdapter (
    private val context: Context,
    private var sights: MutableList<Sight>,
) : RecyclerView.Adapter<SightAdapter.ViewHolder>() {

    private var listener: ((Int) -> Unit)? = null
    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    // データを更新するための関数を追加
    fun updateSights(newSights: List<Sight>) {
        sights.clear()
        sights.addAll(newSights)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.image)
        val description: TextView = view.findViewById(R.id.description)
        val kind: TextView = view.findViewById(R.id.kind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = sights[position].name
        holder.description.text = sights[position].description
        holder.kind.text = sights[position].kind
        var imageResource = context.resources.getIdentifier(
            sights[position].imageName,
            "drawable",
            context.packageName
        )
        // 画像が見つからない場合のデフォルト画像を設定
        if (imageResource == 0) {
            imageResource = android.R.drawable.ic_menu_gallery
        }
        holder.image.setImageResource(imageResource)
        holder.itemView.setOnClickListener {
            listener?.invoke(position)
        }
    }

    override fun getItemCount(): Int = sights.size
}