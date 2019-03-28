precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
const float brightness =0.2;
//美白效果，來源于ios 版本的gpuimage GPUImageBrightnessFilter
//原理就是將rgb 都增加
void main()
{
    vec4 baseColor = texture2D(inputTexture, textureCoordinate);
    vec3 color = baseColor.rgb + vec3(brightness);
    //控制一下范围
    color.r = clamp(color.r,0.0,1.0);
    color.g = clamp(color.g,0.0,1.0);
    color.b = clamp(color.b,0.0,1.0);
    gl_FragColor = vec4(color, baseColor.w);
}