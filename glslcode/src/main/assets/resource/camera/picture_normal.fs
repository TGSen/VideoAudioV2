#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D  U_Texture;//纹理贴图

varying vec2 V_textcoord;
varying vec4 V_poistion;
varying vec4 V_color;
uniform vec4 FileterZoom;
void main(){
    //无滤镜
    gl_FragColor = texture2D(U_Texture, V_textcoord);

    //  gl_FragColor = V_color;
}