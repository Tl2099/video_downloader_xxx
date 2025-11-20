package com.example.video_downloader_xxx.ui.fragment.library.progress

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.ItemVideoProgressBinding
import com.example.video_downloader_xxx.util.glideLoadUseCache

class VideoLoadingAdapter() : RecyclerView.Adapter<VideoLoadingAdapter.ViewHolder>() {
    private val video: MutableList<VideoInfo> = mutableListOf()
    var onCloseClick: (VideoInfo) -> Unit = {}
    var onAdjustClick: (VideoInfo) -> Unit = {}

    inner class ViewHolder(private val binding: ItemVideoProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: VideoInfo) {
            with(binding) {
                imgThumbnail.glideLoadUseCache(item.videoUrl ?: "", R.drawable.video_placeholder)
                tvTitle.text = item.title
                progressBar.setProgress(item.progress.toInt(), true)
                tvTime.text = item.duration
                tvPercent.text = "${item.progress}% / 100%"
                tvStatus.text = item.downloadStatus.toString()
                btnClose.setOnClickListener {
                    onCloseClick.invoke(item)
                }
                btnAdjust.setOnClickListener {
                    onAdjustClick.invoke(item)
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