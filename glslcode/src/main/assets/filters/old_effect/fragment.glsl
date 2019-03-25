precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//老照片的效果
void main(){
     vec4 baseColor = texture2D(inputTexture,textureCoordinate);
     float r = 0.393*baseColor.r+0.769*baseColor.g+0.189*baseColor.b;
     //这个根据ios 效果偏红点+0.1
     r = max(min((r+0.1),1.0),0.0);
     float g = 0.349*baseColor.r+0.686*baseColor.g+0.168*baseColor.b;
     float b = 0.272*baseColor.r+0.534*baseColor.g+0.131*baseColor.b;
     gl_FragColor=vec4(r,g,b,baseColor.a);

}