package com.example.video_downloader_xxx.ui.fragment.library.complete

import android.content.Intent
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.DataExt.indexPos
import com.example.video_downloader_xxx.data.DataExt.listUrl
import com.example.video_downloader_xxx.databinding.FragmentCompleteBinding
import com.example.video_downloader_xxx.ui.PlayerActivity
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CompleteFragment : BaseFragment<FragmentCompleteBinding>() {
    private val library: LibraryViewModel by activityViewModels()
    private val adapter: VideoCompleteAdapter by lazy { VideoCompleteAdapter() }

    override fun initView() {
    }

    override fun initData() {
        binding?.recycleViewListVideoProgress?.adapter = adapter
        library.completedVideos
            .filterNotNull()
            .onEach {
                adapter.addData(it)
            }.launchIn(lifecycleScope)
    }

    override fun initListener() {
        adapter.onItemClick = { video ->
            video.localPath?.let { path ->
                DataExt.path = path
                //Log.i(TAG, "initListener: $path")
                if (listUrl.size == 1){
                    startActivity(Intent(requireContext(), PlayerActivity::class.java))
                }else{
                    //Check Size  list
                    indexPos++
                    if(indexPos <= listUrl.size){
                        listUrl[indexPos]
                    }else{
                        indexPos = 0
                    }
                }
            }
        }
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentCompleteBinding =
        FragmentCompleteBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "CompleteFragment"
    }
}