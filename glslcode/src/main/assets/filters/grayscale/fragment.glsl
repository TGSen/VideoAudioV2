precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//rgb 的分量
const highp vec3 colorWeight  = vec3(0.2125, 0.7154, 0.0721);
void main()
{
   vec4 colorBase = texture2D(inputTexture, textureCoordinate);
    float lightColor = dot(colorBase.rgb, colorWeight);
    gl_FragColor = vec4(vec3(lightColor), colorBase.a);
}
