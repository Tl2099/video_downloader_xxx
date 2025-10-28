package com.example.video_downloader_xxx.ui.base

//abstract class BaseSplashActivity<VB : ViewBinding>(@XmlRes val config: Int) : FragmentActivity() {
//    lateinit var binding: VB
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = viewBinding()
//        setContentView(binding.root)
//        NovaAds.getInstance().setOnAdInitializeListener {
//            NovaAds.getInstance().initializeSplash(this, config) {
//                onInitAds()
//            }
//        }
//
//        onInitViews()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        fullScreenCall()
//    }
//
//    private fun fullScreenCall() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.decorView.windowInsetsController?.let {
//                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//            }
//        } else {
//            val decorView = window.decorView
//            val uiOptions =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.STATUS_BAR_HIDDEN
//            decorView.systemUiVisibility = uiOptions
//        }
//    }
//
//
//    protected abstract fun onInitAds()
//    protected abstract fun onInitViews()
//
//    protected abstract fun viewBinding(): VB

//}