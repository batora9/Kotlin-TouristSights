package com.example.touristsights

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

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

        // 画像の読み込み処理を改善
        loadImage(holder.image, sights[position].imageName)

        holder.itemView.setOnClickListener {
            listener?.invoke(position)
        }
    }

    private fun loadImage(imageView: ImageView, imageName: String) {
        // まず、撮影した画像ファイルをチェック
        val picturesDir = File(context.getExternalFilesDir("Pictures"), imageName)

        if (picturesDir.exists()) {
            // 撮影した画像ファイルが存在する場合
            try {
                val bitmap = BitmapFactory.decodeFile(picturesDir.absolutePath)
                imageView.setImageBitmap(bitmap)
                return
            } catch (e: Exception) {
                // ファイル読み込みに失敗した場合、drawableリソースを試す
            }
        }

        // drawableリソースをチェック
        val imageResource = context.resources.getIdentifier(
            imageName.substringBeforeLast('.'), // 拡張子を除去
            "drawable",
            context.packageName
        )

        if (imageResource != 0) {
            imageView.setImageResource(imageResource)
        } else {
            // デフォルト画像を設定
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount(): Int = sights.size
}