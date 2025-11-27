package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.ItemVideoBinding
import com.example.video_downloader_xxx.util.glideLoadUseCache

class DownloadVideoWebAdapter() : RecyclerView.Adapter<DownloadVideoWebAdapter.ViewHolder>() {

    private val video: MutableList<VideoInfo> = mutableListOf()
    var onRenameClick: (VideoInfo) -> Unit = {}
    var onClick: (VideoInfo) -> Unit = {}
    var onToggleSelect: ((VideoInfo) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VideoInfo) {

            with(binding) {
                imgThumbnail.glideLoadUseCache(item.videoUrl ?: "", R.drawable.video_placeholder)
                tvTitle.text = item.title
                tvFileSize.text = item.fileSize
                tvTime.text = item.duration

                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = item.isSelected
                root.isSelected = item.isSelected

                imgThumbnail.setOnClickListener {
                    onClick.invoke(item)
                }

                root.setOnClickListener {
                    onToggleSelect?.invoke(item)
                    //DataExt.listUrl.add(item.videoUrl ?: "")
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onToggleSelect?.invoke(item.copy(isSelected = isChecked))
                }

                btnRename.setOnClickListener {
                    onRenameClick.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(video[position])
    }

    override fun getItemCount(): Int {
        return video.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<VideoInfo>) {
        video.clear()
        video.addAll(list)
        notifyDataSetChanged()
    }
}