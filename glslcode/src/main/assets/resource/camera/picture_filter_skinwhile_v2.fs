#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D   U_Texture;//纹理贴图
varying vec2 V_textcoord;
varying vec4 V_color;
varying vec4 V_Normal;
varying vec4 V_WorldPos;
uniform vec4 U_MultipleFilter;
varying vec4 V_poistion;
uniform vec4 FileterZoom;

float checkFloat(float tempColorR){
    if(tempColorR > 1.0){
            tempColorR = 1.0;
       }else if(tempColorR < 0.0){
            tempColorR = 0.0;
       }
       return tempColorR;
}
void main(){
     vec4 colorBase = texture2D(U_Texture, V_textcoord.xy);

            //增加一些滤镜效果
         //美白
         float lightRatio = U_MultipleFilter.x; //亮度 取第一个数作为亮度
         float contrastRatio = U_MultipleFilter.y;//对比度 取第二个数作为对比

         //美白亮度增加 1.0是原来的值
         float light = 1.0*lightRatio;
         //对比度也增加，对比度可以是负数，那么在该项目中seekbar要取值时需要这样转化 -1 0 1
         float contrast = 1.0+contrastRatio;

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
           colorR = checkFloat(tempColorR);
           colorG = checkFloat(tempColorG);
           colorB = checkFloat(tempColorB);

           //对比度处理 ，让白的更白点，黑更黑点，在中间点0.5就作为对比
           tempColorR = (colorR-0.5)*contrast+0.5;
           tempColorG = (colorG-0.5)*contrast+0.5;
           tempColorB = (colorB-0.5)*contrast+0.5;
            //颜色在0-1 之间
           colorR = checkFloat(tempColorR);
           colorG = checkFloat(tempColorG);
           colorB = checkFloat(tempColorB);
           gl_FragColor = vec4(colorR, colorG, colorB, colorA);

}