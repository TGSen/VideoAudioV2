precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

const vec2 TexSize = vec2(400.0, 400.0);
const vec2 mosaicSize = vec2(4.0, 4.0);

void main()
{
    vec2 intXY = vec2(textureCoordinate.x*TexSize.x, textureCoordinate.y*TexSize.y);
    vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x)*mosaicSize.x, floor(intXY.y/mosaicSize.y)*mosaicSize.y);
    vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);
    vec4 color = texture2D(inputTexture, UVMosaic);
    gl_FragColor = color;
}