#ifdef GL_SL
percistion mediump float;
#endif
attribute vec4 poistion;
attribute vec4 textcoord;
varying vec2 V_textcoord;
void main(){
    V_textcoord = textcoord.xy;
    gl_Position = poistion;
}