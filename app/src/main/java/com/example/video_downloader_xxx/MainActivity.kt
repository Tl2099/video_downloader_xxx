package com.example.video_downloader_xxx

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.video_downloader_xxx.databinding.ActivityMainBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val sharedVM: SharedViewModel by viewModel()
    private val downloadViewModel: SharedViewModel by viewModel()
    override fun initView() {
        setupBottomNav()
        handleKeyboardVisibility()
    }

    @SuppressLint("RestrictedApi")
    private fun setupBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        binding.bottomNav.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId, null, navOptions)
            true
        }

        binding.bottomNav.itemIconSize
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun handleKeyboardVisibility() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.bottomNav.visibility = if (imeVisible) View.GONE else View.VISIBLE
            insets
        }
    }


    override fun initData() {
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

}