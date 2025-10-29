package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.ItemVideoBinding
import com.example.video_downloader_xxx.util.glideLoad

class DownloadUrlVideoAdapter(
    private val video: MutableList<VideoInfo> = mutableListOf(),
    private val onDownload: ((VideoInfo) -> Unit)? = null
) : RecyclerView.Adapter<DownloadUrlVideoAdapter.ViewHolder>() {

    var onClickVideo: ((VideoInfo) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoInfo) {

            with(binding) {
                imgThumbnail.glideLoad(imgThumbnail, R.drawable.video_placeholder)
                tvTitle.text = item.title
                tvFileSize.text = item.fileSize
                tvTime.text = item.duration
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadUrlVideoAdapter.ViewHolder {
        return ViewHolder(
            ItemVideoBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: DownloadUrlVideoAdapter.ViewHolder,
        position: Int
    ) {
        holder.setIsRecyclable(true)
        holder.bind(video[position])
    }

    override fun getItemCount(): Int {
        return video.size
    }
}