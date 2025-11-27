package com.example.video_downloader_xxx.ui.fragment.browser.how_to_download

import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentHowToDownloadBinding
import com.teh.software.tehads.base.BaseFragment

class HowToDownloadFragment : BaseFragment<FragmentHowToDownloadBinding>() {

    private val images = listOf(
        R.drawable.img_how_to_download_1,
        R.drawable.img_how_to_download_2,
        R.drawable.img_how_to_download_3,
        R.drawable.img_how_to_download_4
    )

    private val processImages = listOf(
        R.drawable.how_to_download_ic_progress_1,
        R.drawable.how_to_download_ic_progress_2,
        R.drawable.how_to_download_ic_progress_3,
        R.drawable.how_to_download_ic_progress_4
    )

    private val numberImages = listOf(
        R.drawable.how_to_download_ic_number_1,
        R.drawable.how_to_download_ic_number_2,
        R.drawable.how_to_download_ic_number_3
    )

    private val textTitle = listOf(
        R.string.how_to_download_btn_title_1,
        R.string.how_to_download_btn_title_2,
        R.string.how_to_download_btn_title_3,
    )

    private var index = 0

    override fun initView() {
        binding?.apply {
            imgMain.setImageResource(images[index])

            imgProgress.setImageResource(processImages[index])

            imgNumber.setImageResource(numberImages[index])

            tvTitle.text = getString(textTitle[index])

            btnBack.setOnClickListener {
                if (index > 0) {
                    index--
                    updateUI()
                }
            }

            btnNext.setOnClickListener {
                index++
                if (index < images.size) {
                    updateUI()
                } else {
                    findNavController().navigateUp()
                }
            }

            imgClose.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun updateUI() {
        binding?.apply {
            imgMain.setImageResource(images[index])
            imgProgress.setImageResource(processImages[index])

            if (index <= 2) {
                imgNumber.visibility = View.VISIBLE
                tvTitle.visibility = View.VISIBLE

                imgNumber.setImageResource(numberImages[index])
                tvTitle.text = getString(textTitle[index])

                btnNext.text = getString(R.string.btn_next)
            }

            if (index == 3) {
                imgNumber.visibility = View.GONE
                tvTitle.visibility = View.GONE
                btnNext.text = getString(R.string.btn_got_it)
            }

            btnBack.visibility = if (index > 0 && index < 3) View.VISIBLE else View.GONE
        }
    }


    override fun initData() {
    }

    override fun initListener() {
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentHowToDownloadBinding =
        FragmentHowToDownloadBinding.inflate(layoutInflater)
}