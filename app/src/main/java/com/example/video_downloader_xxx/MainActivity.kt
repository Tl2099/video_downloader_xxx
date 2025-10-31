package com.example.video_downloader_xxx

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.video_downloader_xxx.databinding.ActivityMainBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val sharedVM: SharedViewModel by viewModel()

    override fun initView() {
        setupBottomNav()
        handleKeyboardVisibility()
    }

    private fun setupBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

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