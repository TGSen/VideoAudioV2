#ifdef GL_ES
precision mediump float;
#endif
in vec4 V_Color;
void main(){
    gl_FragColor =V_Color;
}
