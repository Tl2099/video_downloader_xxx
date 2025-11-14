package com.example.video_downloader_xxx.ui.fragment.library.progress

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.ItemVideoProgressBinding
import com.example.video_downloader_xxx.util.glideLoad

class VideoLoadingAdapter() : RecyclerView.Adapter<VideoLoadingAdapter.ViewHolder>() {
    private val video: MutableList<VideoInfo> = mutableListOf()
    var onCloseClick: (VideoInfo) -> Unit = {}

    inner class ViewHolder(private val binding: ItemVideoProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoInfo) {
            with(binding) {
                imgThumbnail.glideLoad(imgThumbnail, R.drawable.video_placeholder)
                tvTitle.text = item.title
                progressBar.setProgress(item.progress.toInt(), true)
                tvTime.text = item.fileSize
                tvTime.text = item.duration

                btnClose.setOnClickListener {
                    onCloseClick.invoke(item)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<VideoInfo>) {
        video.clear()
        video.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ItemVideoProgressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(video[position])
    }

    override fun getItemCount(): Int = video.size
}