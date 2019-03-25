attribute vec4 position;
attribute vec4 color;
attribute vec4 normal;
uniform mat4 ModelMatrix;
uniform mat4 ViewMatrix;
uniform mat4 ProjectionMatrix;
varying vec4 V_Color;
void main(){
    V_Color = color;
    gl_PointSize = 128.0;//设置点的大小
    vec4 pos= vec4(position.x+normal.x,position.y+normal.y,position.z+normal.z,1.0);
    gl_Position =  ProjectionMatrix*ViewMatrix*ModelMatrix*pos;

}