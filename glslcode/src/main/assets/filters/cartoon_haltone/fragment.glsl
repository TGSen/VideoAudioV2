precision mediump float;

uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
const highp float threshold =0.9;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

const highp float fractionalWidthOfPixel =  0.01;
const highp float aspectRatio =2.0;



void main()
{
    //边缘检测，采用的是gpuimage 的方式
    float texelWidth = 0.0005;
    float texelHeight =0.0005;
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
    vec4 edgeColor = vec4(vec3(mag), 1.0);

    //然后使用半色调

    highp vec2 sampleDivisor = vec2(fractionalWidthOfPixel, fractionalWidthOfPixel / aspectRatio);
    highp vec2 samplePos = textureCoordinate - mod(textureCoordinate, sampleDivisor) + 0.5 * sampleDivisor;
    highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
    highp vec2 adjustedSamplePos = vec2(samplePos.x, (samplePos.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
    highp float distanceFromSamplePoint = distance(adjustedSamplePos, textureCoordinateToUse);

    lowp vec3 sampledColor = texture2D(inputTexture, samplePos).rgb;
    highp float dotScaling = 1.0 - dot(sampledColor, W);
    vec4 dotSampledColor;
    if (dotScaling>0.8){
        lowp float checkForPresenceWithinDot = 1.0 - step(distanceFromSamplePoint, (fractionalWidthOfPixel * 0.5) * dotScaling);
        dotSampledColor =vec4(vec3(checkForPresenceWithinDot), 1.0);
    } else {
        dotSampledColor= vec4(1.0);
    }
    if (dotSampledColor.r<0.2){
        dotSampledColor = vec4(0.8, 0.0, 0.0, 0.8);
    }
    gl_FragColor = edgeColor*dotSampledColor;


}