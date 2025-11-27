package com.example.video_downloader_xxx.ui.fragment.library

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.video_downloader_xxx.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.teh.software.tehads.base.BaseFragment
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class LibraryFragment : BaseFragment<FragmentLibraryBinding>() {
    private val library: LibraryViewModel by activityViewModel()
    private val adapter by lazy { ViewPagerAdapter(this) }
    private var mediator: TabLayoutMediator? = null

    override fun initView() {
        binding?.let { b ->
            b.viewPager.adapter = adapter
            mediator = TabLayoutMediator(b.tabLayout, b.viewPager) { tab, pos ->
                tab.text = if (pos == 0) "Progress" else "Download"
            }.apply { attach() }
        }

    }

    override fun initData() {
    }

    override fun initListener() {
        library.goToDownloadTab
            .onEach {
                binding?.viewPager?.currentItem = 1
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        val navBackStackEntry = findNavController().currentBackStackEntry

        navBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("open_progress")
            ?.observe(viewLifecycleOwner) { open ->
                if (open == true) {
                    binding?.viewPager?.currentItem = 0
                    navBackStackEntry.savedStateHandle.remove<Boolean>("open_progress")
                }
            }
    }

    override fun reloadAds() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
    }

    override fun getViewBinding(): FragmentLibraryBinding =
        FragmentLibraryBinding.inflate(layoutInflater)
}