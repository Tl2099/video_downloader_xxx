package com.example.video_downloader_xxx.ui.fragment.browser.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.WebHistory
import com.example.video_downloader_xxx.databinding.ItemRecentlyWebBinding
import com.example.video_downloader_xxx.util.glideLoad

class WebRecentlyAdapter : RecyclerView.Adapter<WebRecentlyAdapter.ViewHolder>() {
    private val listWebRecently: MutableList<WebHistory?> = mutableListOf()
    var onItemClick: ((WebHistory) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemRecentlyWebBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WebHistory?) {
            if (item == null) {
                binding.imgIcon.setImageResource(R.drawable.ic_plus)
                binding.tvName.text = ""
                binding.root.isClickable = false
            } else {

                binding.imgIcon.glideLoad(item.faviconUrl ?: "", R.drawable.web_history)
                binding.tvName.text = item.title

                binding.root.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ItemRecentlyWebBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listWebRecently[position])
    }

    override fun getItemCount(): Int = listWebRecently.size

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<WebHistory>) {
        listWebRecently.clear()
        if (list.isEmpty()) {
            repeat(7) { listWebRecently.add(null) }
        } else {
            listWebRecently.addAll(list.take(7))

            repeat(7 - list.size) {
                listWebRecently.add(null)
            }
        }
        notifyDataSetChanged()
    }

}