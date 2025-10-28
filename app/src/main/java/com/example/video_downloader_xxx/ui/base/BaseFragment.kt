package com.example.video_downloader_xxx.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null
    val binding get() = _binding
    private var _isViewCreated = false
    val isFragmentRunning: Boolean
        get() {
            return isAdded && !isHidden && isResumed
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding != null) {
            _isViewCreated = true
            return binding?.root
        }
        _isViewCreated = false
        _binding = getViewBinding()
        return binding?.root
    }


    protected abstract fun initView()
    protected abstract fun initData()
    protected abstract fun initListener()

    /**
     * Auto load ads when fragment created and back from other fragment
     * **/
    protected abstract fun reloadAds()
    protected abstract fun getViewBinding(): VB

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private var timeReloadAds: Long = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (System.currentTimeMillis() - timeReloadAds > 1000) {
            reloadAds()
        }
        timeReloadAds = System.currentTimeMillis()
        if (_isViewCreated) return
        initView()
        initData()
        initListener()
    }
}