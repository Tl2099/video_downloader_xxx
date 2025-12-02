package com.example.video_downloader_xxx.ui.fragment.library.complete

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.ItemVideoCompleteBinding
import com.example.video_downloader_xxx.util.formatTimestamp
import com.example.video_downloader_xxx.util.glideLoadUseCache
import com.example.video_downloader_xxx.util.loadVideoThumbnail

class VideoCompleteAdapter() : RecyclerView.Adapter<VideoCompleteAdapter.ViewHolder>() {

    private val video: MutableList<VideoInfo> = mutableListOf()
    var onItemClick: ((VideoInfo) -> Unit)? = null
    var onMoreClick: ((VideoInfo) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemVideoCompleteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoInfo) {
            with(binding) {
                val formatted = item.downloadedAt?.let { formatTimestamp(it) } ?: "N/A"
                imgThumbnail.loadVideoThumbnail(
                    videoUrl = item.videoUrl,
                    fallbackThumbnail = item.thumbnail,
                    placeholder = R.drawable.video_placeholder
                )
                tvTitle.text = item.title
                tvTime.text = item.duration
                tvFileSize.text = item.fileSize
                tvTimeDownload.text = formatted

                root.setOnClickListener {
                    onItemClick?.invoke(item)
                }

                btnMore.setOnClickListener {
                    onMoreClick?.invoke(item)
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
        ItemVideoCompleteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(video[position])
    }

    override fun getItemCount(): Int = video.size
}