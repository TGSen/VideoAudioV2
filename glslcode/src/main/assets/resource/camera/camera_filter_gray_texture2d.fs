#ifdef GL_ES
precision mediump float;
#endif


uniform sampler2D  U_Texture;//纹理贴图
varying vec2 V_textcoord;
varying vec4 V_color;
varying vec4 V_Normal;
varying vec4 V_WorldPos;
uniform vec4 U_MultipleFilter;
uniform vec4 FileterZoom;
varying vec4 V_poistion;
void main(){
    vec4 colorBase = texture2D(U_Texture, V_textcoord.xy);
    float blendColor = (colorBase.r+colorBase.g+colorBase.b)/3.0;
    gl_FragColor = vec4(blendColor, blendColor, blendColor, 1.0);

}