#extension GL_OES_EGL_image_external : require
#ifdef GL_ES
precision mediump float;
#endif
uniform samplerExternalOES  U_Texture;//纹理贴图
varying vec2 V_textcoord;
varying vec4 V_poistion;
uniform vec4 FileterZoom;
void main(){
    //增加一些滤镜效果
    // gl_FragColor =color;
    //gl_FragColor = vec4((1.0 - gl_FragColor.rgb), gl_FragColor.w);
        //gl_FragColor = V_color;
  //gl_FragColor = V_color*texture2D(U_Texture, V_textcoord);
    //无滤镜
    gl_FragColor = texture2D(U_Texture, V_textcoord);


}