package com.example.video_downloader_xxx.ui.activity

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.ActivityMainBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val sharedVM: SharedViewModel by viewModel()
    private val downloadViewModel: SharedViewModel by viewModel()
    private val library: LibraryViewModel by viewModel()
    lateinit var browserNavHost: NavHostFragment
    lateinit var libraryNavHost: NavHostFragment
    override fun initView() {
        setupNavHosts()
        setupBottomNav()
        handleKeyboardVisibility()
    }

    @SuppressLint("RestrictedApi")
    private fun setupNavHosts() {
        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        browserNavHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_browser) as NavHostFragment

        libraryNavHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_library) as NavHostFragment

//        val navController = navHostFragment.navController
//
//        binding.bottomNav.itemIconSize
//        binding.bottomNav.setupWithNavController(navController)
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_browser ->{
                    showBrowserTab()
                    true
                }
                R.id.navigation_library -> {
                    showLibraryTab()
                    true
                }
                else -> false
            }
        }
    }

    fun openProgressScreen() {
        binding.bottomNav.selectedItemId = R.id.navigation_library
        val controller = libraryNavHost.navController
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.libraryFragment, true)
            .setLaunchSingleTop(false)
            .build()
        controller.navigate(R.id.libraryFragment, null, options)
        controller.currentBackStackEntry?.savedStateHandle?.set("open_progress", true)
        //controller.navigate(R.id.libraryFragment)
        library.openProgressTab.tryEmit(Unit)
    }

    private fun showBrowserTab() {
        binding.navHostBrowser.visibility = View.VISIBLE
        binding.navHostLibrary.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .show(browserNavHost)
            .hide(libraryNavHost)
            .commit()
    }

    private fun showLibraryTab() {
        binding.navHostLibrary.visibility = View.VISIBLE
        binding.navHostBrowser.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .show(libraryNavHost)
            .hide(browserNavHost)
            .commit()
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