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
varying vec4 V_poistion;
uniform vec4 FileterZoom;
void main(){
    //增加一些滤镜效果
    // gl_FragColor =color;
    //gl_FragColor = vec4((1.0 - gl_FragColor.rgb), gl_FragColor.w);
        //gl_FragColor = V_color;
  //gl_FragColor = V_color*texture2D(U_Texture, V_textcoord);
 // gl_FragColor = texture2D(U_Texture, V_textcoord.xy);

 //黑白滤镜
  vec4 colorBase = texture2D(U_Texture, V_textcoord.xy);
  if((FileterZoom.z==1.0 && V_poistion.y>=FileterZoom.y)||(FileterZoom.z==0.0 &&V_poistion.x<=FileterZoom.x)){
          //混合之后的效果
          float blendColorR = dot(colorBase.r,U_MultipleFilter.x);
          float blendColorG = dot(colorBase.g,U_MultipleFilter.y);
          float blendColorB = dot(colorBase.b,U_MultipleFilter.z);
          //float blendColorA = dot(colorBase.a,U_MultipleFilter.w);
           gl_FragColor = vec4(blendColorR, blendColorG, blendColorB, 1.0);
    } else{
           gl_FragColor = colorBase;
    }
}