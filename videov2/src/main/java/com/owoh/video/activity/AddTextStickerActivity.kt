package com.owoh.video.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.blankj.utilcode.util.ColorData
import com.blankj.utilcode.util.ColorItem
import com.cgfay.filterlibrary.utils.DensityUtils
import com.owoh.R
import com.owoh.video.adapter.ColorAdapter
import com.owoh.video.event.EventTextStickerChange
import com.owoh.video.widget.SpaceItemDecoration
import com.owoh.video.widget.sticker.Sticker
import com.owoh.video.widget.sticker.StickerView
import com.owoh.video.widget.sticker.TextSticker
import org.greenrobot.eventbus.EventBus


class AddTextStickerActivity : AppCompatActivity(), View.OnClickListener {


    private var imagePath: String? = null
    private var colorAdapter: ColorAdapter? = null

    private lateinit var binding: com.owoh.databinding.ActivityAddTextstickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_textsticker)

        initView()
        initData()
    }

    private fun initData() {
        val bundle = intent.getBundleExtra(BUNDLE_PATH)
        imagePath = bundle.getString(KEY_IMAGE_PATH)
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val drawable = BitmapDrawable(bitmap)
        var textSticker = TextSticker(this@AddTextStickerActivity)
        textSticker.drawable = drawable
        textSticker.text = " "
        textSticker.resizeText()
        textSticker.isShow = true
        binding.stickerView.isLocked = true
        binding.stickerView.addSticker(textSticker)
    }

    private fun initView() {
        binding.apply {
            //   var decoration = GridDecoration(8,9)
            submit.setOnClickListener(this@AddTextStickerActivity)
            btCloseImag.setOnClickListener(this@AddTextStickerActivity)
            var colorItems = arrayListOf<ColorItem>()
            ColorData.createData()
            ColorData.getData(colorItems)
            colorItems[0].isSeleted = true
            colorAdapter = ColorAdapter(this@AddTextStickerActivity, colorItems)
            binding?.recyclerview?.adapter = colorAdapter
            var layoutManager = LinearLayoutManager(this@AddTextStickerActivity)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            binding?.recyclerview?.layoutManager = layoutManager
            var db = DensityUtils.dp2px(this@AddTextStickerActivity, 8f)
            binding?.recyclerview?.addItemDecoration(SpaceItemDecoration(db, db))
            colorAdapter?.setOnColorSeletedLinstener(object : ColorAdapter.OnColorSeletedLinstener {
                override fun onSeleted(color: String, position: Int) {

                    //   EventBus.getDefault().post(colorData[position])
                    binding.stickerView?.stickers[0]?.let {
                        var textSticker = binding.stickerView?.stickers[0] as TextSticker
                        textSticker.setTextColor(Color.parseColor(color))
                        binding.stickerView.updateSticker()
                    }
                }


            })

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    binding.stickerView?.stickers[0]?.let {
                        var textSticker = binding.stickerView?.stickers[0] as TextSticker
                        var content = " "
                        if (!TextUtils.isEmpty(s)) {
                            content = s.toString()
                        }
                        textSticker.text = content
                        textSticker.resizeText()
                        binding.stickerView.updateSticker()
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })


            binding.stickerView.setBackgroundColor(Color.TRANSPARENT)
            binding.stickerView.isLocked = false
            binding.stickerView.isConstrained = true


            binding.stickerView.onStickerOperationListener = object : StickerView.OnStickerOperationListener {
                override fun onStickerAdded(sticker: Sticker) {
                }

                override fun onStickerClicked(sticker: Sticker) {
                    //stickerView.removeAllSticker();
                }

                override fun onStickerDeleted(sticker: Sticker) {
                }

                override fun onStickerDragFinished(sticker: Sticker) {
                }

                override fun onStickerTouchedDown(sticker: Sticker) {
                }

                override fun onStickerZoomFinished(sticker: Sticker) {
                }

                override fun onStickerFlipped(sticker: Sticker) {
                }

                override fun onStickerDoubleTapped(sticker: Sticker) {
                }

                override fun onStickerTouchedOutSide() {

                }
            }


        }


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btCloseImag ->
                finish()
            R.id.submit -> {
                if (!TextUtils.isEmpty(binding.editText.text)) {
                    colorAdapter?.getSeletedColor()?.let {
                        Log.e("Harrison","it:"+it)
                        EventBus.getDefault().post(EventTextStickerChange(binding.editText.text.toString(), it))
                    }

                }
                finish()
            }
        }
    }

    companion object {
        private const val KEY_IMAGE_PATH = "path"
        private const val BUNDLE_PATH = "bundle"
        fun gotoThis(context: Context, path: String) {
            val intent = Intent(context, AddTextStickerActivity::class.java)
            val bundle = Bundle()
            bundle.putString(KEY_IMAGE_PATH, path)
            intent.putExtra(BUNDLE_PATH, bundle)
            context.startActivity(intent)
        }
    }

}