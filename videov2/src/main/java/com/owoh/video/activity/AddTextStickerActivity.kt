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
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AddTextStickerActivity : AppCompatActivity(), View.OnClickListener {


    private var imagePath: String? = null
    private var colorAdapter: ColorAdapter? = null
    private var colorItems: ArrayList<ColorItem> = arrayListOf<ColorItem>()

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
        var text = bundle.getSerializable(KEY_TEXT) as EventTextStickerChange
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val drawable = BitmapDrawable(bitmap)
        var textSticker = TextSticker(this@AddTextStickerActivity)
        textSticker.drawable = drawable
        var texts = text?.text
        if (TextUtils.isEmpty(texts) || texts.equals(" ")) {
            texts = " "
        } else {
            binding.editText.setText(texts)
        }
        textSticker.text = texts
        textSticker.resizeText()

        textSticker.isShow = true
        binding.stickerView.isLocked = true
        binding.stickerView.addSticker(textSticker)
        text?.color?.let {
            textSticker.setTextColor(Color.parseColor(it))
            binding.stickerView.updateSticker()
            for ((index, values) in colorItems.withIndex()) {
                if (it == values.color) {
                    colorAdapter?.setSeleted(index)
                    binding.recyclerview.smoothScrollToPosition(index)
                }
            }
        }


    }

    private fun initView() {
        binding.apply {
            //   var decoration = GridDecoration(8,9)
            submit.setOnClickListener(this@AddTextStickerActivity)
            btCloseImag.setOnClickListener(this@AddTextStickerActivity)
            ColorData.createData()
            ColorData.getData(colorItems)
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
                    binding.stickerView?.stickers?.let {
                        if (it.size <= 0) return
                        var textSticker = it[0] as TextSticker
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
                        EventBus.getDefault().post(EventTextStickerChange(binding.editText.text.toString(), it))
                    }
                }else{
                    EventBus.getDefault().post(EventTextStickerChange(" ", "#FFFFFF"))
                }
                finish()
            }
        }
    }

    companion object {
        private const val KEY_IMAGE_PATH = "path"
        private const val KEY_TEXT = "text"
        private const val BUNDLE_PATH = "bundle"
        var EXECUTOR: Executor = Executors.newCachedThreadPool()
        fun gotoThis(context: Context, path: String, text: EventTextStickerChange?) {
            val intent = Intent(context, AddTextStickerActivity::class.java)
            val bundle = Bundle()
            bundle.putString(KEY_IMAGE_PATH, path)
            if (text == null) {
                var etc = EventTextStickerChange(" ", "#FFFFFF")
                bundle.putSerializable(KEY_TEXT, etc)
            } else {
                bundle.putSerializable(KEY_TEXT, text)
            }

            intent.putExtra(BUNDLE_PATH, bundle)
            context.startActivity(intent)
        }
    }

}