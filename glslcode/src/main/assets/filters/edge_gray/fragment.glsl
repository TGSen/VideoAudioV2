precision mediump float;

uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
const highp float threshold =0.9;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
    //边缘检测，采用的是gpuimage 的方式
    float texelWidth = 0.001;
    float texelHeight =0.0008;
    vec2 widthStep = vec2(texelWidth, 0.0);
    vec2 heightStep = vec2(0.0, texelHeight);
    vec2 widthHeightStep = vec2(texelWidth, texelHeight);
    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);

    vec2 leftTextureCoordinate = textureCoordinate.xy - widthStep;
    vec2 rightTextureCoordinate = textureCoordinate.xy+ widthStep;

    vec2 topTextureCoordinate = textureCoordinate.xy - heightStep;
    vec2 topLeftTextureCoordinate = textureCoordinate.xy - widthHeightStep;
    vec2 topRightTextureCoordinate = textureCoordinate.xy+widthNegativeHeightStep;

    vec2 bottomTextureCoordinate = textureCoordinate.xy +heightStep;
    vec2 bottomLeftTextureCoordinate = textureCoordinate.xy - widthNegativeHeightStep;
    vec2 bottomRightTextureCoordinate = textureCoordinate.xy+ widthHeightStep;
    
    float bottomLeftIntensity = texture2D(inputTexture, bottomLeftTextureCoordinate).r;
    float topRightIntensity = texture2D(inputTexture, topRightTextureCoordinate).r;
    float topLeftIntensity = texture2D(inputTexture, topLeftTextureCoordinate).r;
    float bottomRightIntensity = texture2D(inputTexture, bottomRightTextureCoordinate).r;
    float leftIntensity = texture2D(inputTexture, leftTextureCoordinate).r;
    float rightIntensity = texture2D(inputTexture, rightTextureCoordinate).r;
    float bottomIntensity = texture2D(inputTexture, bottomTextureCoordinate).r;
    float topIntensity = texture2D(inputTexture, topTextureCoordinate).r;
    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity  +bottomRightIntensity;
    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;

    float mag = 1.0 - length(vec2(h, v));
    mag = step(threshold, mag);
    gl_FragColor = vec4(vec3(mag), 1.0);



}