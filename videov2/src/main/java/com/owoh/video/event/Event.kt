package com.owoh.video.event

import android.text.Editable

class EventTextStickerChange(text: String, color: String) {
     var text: String? =null
     var color: String? =null

     init {
         this.text = text
          this.color = color
     }

}