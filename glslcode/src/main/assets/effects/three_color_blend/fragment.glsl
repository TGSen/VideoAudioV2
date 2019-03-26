precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
const  float offset = 0.008;
//仿ios 红绿蓝 三个颜色+原来颜色混合
void main() {
    vec4 baseColor = texture2D(inputTexture, textureCoordinate);
    vec4 green = texture2D(inputTexture, vec2(textureCoordinate.x - offset, textureCoordinate.y - offset));
    vec4 red = texture2D(inputTexture, vec2(textureCoordinate.x + offset, textureCoordinate.y + offset));
    gl_FragColor = vec4(red.x, green.y, baseColor.z, baseColor.w);


}