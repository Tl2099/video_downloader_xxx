package com.example.video_downloader_xxx.ui.fragment.browser.history

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.WebHistory
import com.example.video_downloader_xxx.databinding.ItemWebHistoryBinding
import com.example.video_downloader_xxx.util.glideLoad
import androidx.core.graphics.drawable.toDrawable

class WebsiteHistoryAdapter : RecyclerView.Adapter<WebsiteHistoryAdapter.ViewHolder>() {
    private val listWebHistory: MutableList<WebHistory> = mutableListOf()
    var onMoreClick: (() -> Unit)? = null
    var onShareClick: ((WebHistory) -> Unit)? = null
    var onDeleteClick: ((WebHistory) -> Unit)? = null
    var onCopyLinkClick: ((WebHistory) -> Unit)? = null
    var onItemClick: ((WebHistory) -> Unit)? = null

    inner class ViewHolder(val binding: ItemWebHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WebHistory) {
            with(binding) {
                tvTitle.text = item.title
                imgIcon.glideLoad(item.faviconUrl ?: "", R.drawable.web_history)
                tvWebUrl.text = item.url

                btnMore.setOnClickListener { v ->
                    val wrapper = ContextThemeWrapper(v.context, R.style.MyPopupMenuStyle)
                    val popup = PopupMenu(wrapper, v)
                    popup.menuInflater.inflate(R.menu.menu_history_item, popup.menu)

                    popup.setOnMenuItemClickListener { menu ->
                        when (menu.itemId) {
                            R.id.action_share -> onShareClick?.invoke(item)
                            R.id.action_delete -> onDeleteClick?.invoke(item)
                            R.id.action_copy -> onCopyLinkClick?.invoke(item)
                        }
                        true
                    }

                    popup.show()
                }

                root.setOnClickListener {
                    onItemClick?.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ItemWebHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listWebHistory[position])
    }

    override fun getItemCount(): Int = listWebHistory.size

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<WebHistory>) {
        listWebHistory.clear()
        listWebHistory.addAll(list)
        notifyDataSetChanged()
    }
}
