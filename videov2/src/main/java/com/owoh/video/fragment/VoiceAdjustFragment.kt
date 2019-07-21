package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.owoh.R

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/19 14:29
 */
class VoiceAdjustFragment : Fragment() {
    private var mActivity: Activity? = null

    private var mVoiceChangeListener: OnVoiceSeekBarChangeListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
    }

    private lateinit var binding: com.owoh.databinding.FragmentLayoutVoiceAdjustBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_layout_voice_adjust, container, false)

        return binding?.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }


    private fun initView() {
        binding.apply {
            title.text = getString(R.string.adjust_voice)
            seekBarVoice.max = 100
            seekBarBGM.max = 100
            seekBarVoice.progress = 50
            seekBarBGM.progress = 50
            seekBarBGM.isEnabled = false


            seekBarVoice.setOnSeekBarChangeListener(MSeekBarchangeListener(SEEKBAR_ORIGI))
            seekBarBGM.setOnSeekBarChangeListener(MSeekBarchangeListener(SEEKBAR_BGM))
        }

    }

    private inner class MSeekBarchangeListener(private val currentSeekBar: Int) : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (mVoiceChangeListener == null) return
            if (currentSeekBar == SEEKBAR_BGM) {
                mVoiceChangeListener!!.bgmVoiceChange(progress.toFloat() / seekBar.max.toFloat())
            } else if (currentSeekBar == SEEKBAR_ORIGI) {
                mVoiceChangeListener!!.origiVoiceChange(progress.toFloat() / seekBar.max.toFloat())
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }


    }

    fun setOnVoiceSeekBarChangeListener(listener: OnVoiceSeekBarChangeListener) {
        this.mVoiceChangeListener = listener
    }

    /**
     * 声音的控制器改变
     */
    interface OnVoiceSeekBarChangeListener {
        fun origiVoiceChange(progress: Float)

        fun bgmVoiceChange(progres: Float)
    }

    companion object {
        const val SEEKBAR_ORIGI = 0x001
        const val SEEKBAR_BGM = 0x002
        val instance: VoiceAdjustFragment
            get() = VoiceAdjustFragment()
    }


}
