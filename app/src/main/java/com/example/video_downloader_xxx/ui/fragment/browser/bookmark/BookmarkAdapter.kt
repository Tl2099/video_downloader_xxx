package com.example.video_downloader_xxx.ui.fragment.browser.bookmark

import android.annotation.SuppressLint
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.Bookmark
import com.example.video_downloader_xxx.data.model.WebHistory
import com.example.video_downloader_xxx.databinding.ItemBookmarkBinding
import com.example.video_downloader_xxx.databinding.ItemWebHistoryBinding
import com.example.video_downloader_xxx.util.glideLoad

class BookmarkAdapter : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    private val listBookmark: MutableList<Bookmark> = mutableListOf()
    var onMoreClick: (() -> Unit)? = null
    var onShareClick: ((Bookmark) -> Unit)? = null
    var onDeleteClick: ((Bookmark) -> Unit)? = null
    var onCopyLinkClick: ((Bookmark) -> Unit)? = null
    var onItemClick: ((Bookmark) -> Unit)? = null

    inner class ViewHolder(val binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Bookmark) {
            with(binding) {
                tvTitle.text = item.title
                imgIcon.glideLoad(item.faviconBase64 ?: "", R.drawable.web_history)
                tvWebUrl.text = item.url

                btnMore.setOnClickListener { v ->
                    val wrapper = ContextThemeWrapper(v.context, R.style.HistoryPopupMenuStyle)
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
        ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listBookmark[position])
    }

    override fun getItemCount(): Int = listBookmark.size

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<Bookmark>) {
        listBookmark.clear()
        listBookmark.addAll(list)
        notifyDataSetChanged()
    }
}