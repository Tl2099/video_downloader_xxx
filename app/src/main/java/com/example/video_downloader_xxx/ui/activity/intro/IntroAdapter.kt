package com.example.video_downloader_xxx.ui.activity.intro

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.video_downloader_xxx.data.model.Social
import com.example.video_downloader_xxx.databinding.FragmentIntroBinding
import com.example.video_downloader_xxx.ui.fragment.browser.intro.IntroFragment
import com.example.video_downloader_xxx.util.glideLoad

class IntroAdapter(
    private var pages: List<IntroPage>,
    private val onNextClick: (IntroPage) -> Unit
) : RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {


    inner class IntroViewHolder(private val binding: FragmentIntroBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IntroPage) {
            binding.apply {
                content.glideLoad(item.imageRes)
                contentTitle.text = item.title
                contentDescription.text = item.description
                imgProgress.glideLoad(item.imageProgress)

                btnNext.setOnClickListener {
                    onNextClick.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IntroViewHolder = IntroViewHolder(
        FragmentIntroBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: IntroViewHolder,
        position: Int
    ) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<IntroPage>) {
        pages = newList
        notifyDataSetChanged()
    }

}
