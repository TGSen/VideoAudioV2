#extension GL_OES_EGL_image_external : require
#ifdef GL_ES
precision mediump float;
#endif


uniform samplerExternalOES  U_Texture;//纹理贴图
varying vec2 V_textcoord;
varying vec4 V_color;
varying vec4 V_Normal;
varying vec4 V_WorldPos;
uniform vec4 U_MultipleFilter;
varying vec4 FileterZoom;
void main(){
//这个是第一版本，测试成功后修改V2 版本了

    //增加一些滤镜效果
 //美白
 float lightRatio = 0.25; //亮度
 float contrastRatio = 0.26;//对比度

 //美白亮度增加
 float light = 1.0*lightRatio;
 //对比度也增加
 float contrast = 1.0+contrastRatio;
  vec4 colorBase = texture2D(U_Texture, V_textcoord.xy);
  //混合之后的效果
  float colorR = colorBase.r;
  float colorG = colorBase.g;
  float colorB = colorBase.b;
  float colorA = colorBase.a;
   //美白,对rgb 处理
   float tempColorR =colorR+light;
   float tempColorG =colorG+light;
   float tempColorB =colorB+light;

   //颜色在0-1 之间
   if(tempColorR > 1.0){
        colorR = 1.0;
   }else if(tempColorR < 0.0){
        colorR = 0.0;
   }else{
        colorR = tempColorR;
   }

   if(tempColorG > 1.0){
           colorG = 1.0;
      }else if(tempColorG < 0.0){
           colorG = 0.0;
      }else{
           colorG = tempColorG;
      }

      if(tempColorB > 1.0){
              colorB = 1.0;
         }else if(tempColorB < 0.0){
              colorB = 0.0;
         }else{
              colorB = tempColorB;
         }

   //对比度处理 ，让白的更白点，黑更黑点，在中间点0.5就作为对比
   //(colorR-0.5)*contrast 这个是变化量，在加回原来的
   tempColorR = (colorR-0.5)*contrast+0.5;
   tempColorG = (colorG-0.5)*contrast+0.5;
   tempColorB = (colorB-0.5)*contrast+0.5;

     if(tempColorR > 1.0){
          colorR = 1.0;
     }else if(tempColorR < 0.0){
          colorR = 0.0;
     }else{
          colorR = tempColorR;
     }

     if(tempColorG > 1.0){
             colorG = 1.0;
        }else if(tempColorG < 0.0){
             colorG = 0.0;
        }else{
             colorG = tempColorG;
        }

        if(tempColorB > 1.0){
                colorB = 1.0;
           }else if(tempColorB < 0.0){
                colorB = 0.0;
           }else{
                colorB = tempColorB;
        }
   gl_FragColor = vec4(colorR, colorG, colorB, colorA);

}