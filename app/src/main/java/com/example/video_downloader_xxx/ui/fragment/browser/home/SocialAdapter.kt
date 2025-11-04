package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.data.model.Social
import com.example.video_downloader_xxx.databinding.ItemSocialWebBinding
import com.example.video_downloader_xxx.util.glideLoad

class SocialAdapter(
    private var socials: List<Social>,
    private val onItemClick: (Social) -> Unit
) : RecyclerView.Adapter<SocialAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSocialWebBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Social) {
            binding.apply {
                imgIcon.glideLoad(item.iconUrl)
                tvName.text = item.title
            }
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(ItemSocialWebBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(socials[position])
    }

    override fun getItemCount(): Int = socials.take(7).size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Social>) {
        socials = newList
        notifyDataSetChanged()
    }

}