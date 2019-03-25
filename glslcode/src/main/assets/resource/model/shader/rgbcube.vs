attribute vec4 poistion;
uniform mat4 ModelMatrix;
uniform mat4 ViewMatrix;
uniform mat4 ProjectionMatrix;
out vec4 V_color;
void main(){
    V_color = vec4(poistion.x+0.5,poistion.y+0.5,poistion.z+0.5,1.0);
    gl_Position = ProjectionMatrix*ModelMatrix*ViewMatrix*poistion;
}
