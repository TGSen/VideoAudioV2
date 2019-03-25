#version 300 es
#ifdef GL_SL
percistion mediump float;
#endif
in vec4 poistion;
in vec4 textcoord;
out vec2 V_Texcoord;
void main(){
    V_Texcoord = textcoord.xy;
    gl_Position = poistion;
}