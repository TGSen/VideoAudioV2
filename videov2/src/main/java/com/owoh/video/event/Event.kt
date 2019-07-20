package com.owoh.video.event

import java.io.Serializable

class EventTextStickerChange(text: String, color: String):Serializable {
     var text: String? =null
     var color: String? =null

     init {
         this.text = text
          this.color = color
     }

}

class EventCloseFragment{

}