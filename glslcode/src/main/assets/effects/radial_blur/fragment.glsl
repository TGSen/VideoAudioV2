precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//径向模糊效果，该效果来源于HLSL
void main() {
    //采样
    //奇怪这样写报错
//    float smaples[10];
//
//    smaples[9]= 0.08;
//    samples[6]=0.02;samples[7]=0.03;samples[8]=0.05;
//    samples[3]=-0.02;samples[4]=-0.01;samples[5]=0.01;
//    samples[0]=-0.08;samples[1]=-0.05;samples[2]=-0.03;
    //采样距离
    float fSampleDist = 1.0;
    //采样力度
    float fSampleStrength = 2.0;

    vec2 dir = vec2(0.5)- textureCoordinate;
    // calculate the distance to the center of the screen
    float dist = length(dir);
    // normalize the direction (reuse the distance)
    dir /= dist;
    // this is the original colour of this pixel
    // using only this would result in a nonblurred version

    vec4 baseColor = texture2D(inputTexture, textureCoordinate);
    vec4 blendColor = baseColor;
    // take 10 additional blur samples in the direction towards
    // the center of the screen
    //进行10次采样
//    for (int i = 0; i < 10; i++)
//    {
//        blendColor += texture2D(inputTexture, textureCoordinate + dir * samples[i] * fSampleDist);
//    }

    blendColor += texture2D(inputTexture, textureCoordinate + dir * (-0.05) * fSampleDist);
    blendColor += texture2D(inputTexture, textureCoordinate + dir * (-0.03) * fSampleDist);
    blendColor += texture2D(inputTexture, textureCoordinate + dir * (-0.01) * fSampleDist);
    blendColor += texture2D(inputTexture, textureCoordinate + dir * (0.01) * fSampleDist);
    blendColor += texture2D(inputTexture, textureCoordinate + dir * (0.03) * fSampleDist);
    blendColor += texture2D(inputTexture, textureCoordinate + dir * (0.05) * fSampleDist);

    // we have taken eleven samples
    blendColor /= 7.0;

    // weighten the blur effect with the distance to the
    // center of the screen ( further out is blurred more)
    // float t = saturate(dist * fSampleStrength);

    float t = clamp(dist * fSampleStrength, 0.0, 1.0);
    //Blend the original color with the averaged pixels
    //计算插值在HLSL ：lerp(baseColor, blendColor, t);lerp(x, y, s) 即 x + s(y - x)

    vec4 endColor = baseColor+t*(blendColor-baseColor);
    gl_FragColor = endColor;
}