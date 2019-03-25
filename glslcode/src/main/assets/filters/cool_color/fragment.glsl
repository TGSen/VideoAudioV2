precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//原理：冷调是原色点+蓝
const vec4 uChangeColor = vec4(0.0,0.1,0.1,0.0);
void main(){
    vec4 baseColor = texture2D(inputTexture,textureCoordinate);
    vec4 color=baseColor + uChangeColor;
    float r=max(min(color.r,1.0),0.0);
    float g=max(min(color.g,1.0),0.0);
    float b=max(min(color.b,1.0),0.0);
    float a=max(min(color.a,1.0),0.0);
    gl_FragColor=vec4(r,g,b,a);
}