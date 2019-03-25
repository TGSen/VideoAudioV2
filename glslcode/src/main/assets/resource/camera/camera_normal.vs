attribute vec4 poistion;
attribute vec4 color;
attribute vec4 normal;
attribute vec4 textcoord;
uniform mat4 ModelMatrix;
uniform mat4 ViewMatrix;
uniform mat4 ProjectionMatrix;
varying vec4 V_Normal;
varying vec4 V_color;
varying vec2 V_textcoord;
varying vec4 V_poistion;
void main(){
    V_color = color;
     V_textcoord = textcoord.xy;
    gl_Position = ProjectionMatrix*ModelMatrix*ViewMatrix*poistion;
     V_poistion =poistion;
}