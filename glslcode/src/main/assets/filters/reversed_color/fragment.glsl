precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;


void main()
{
    vec4 colorBase = texture2D(inputTexture, textureCoordinate);
     //反相的效果 算法原型 255-本身的rgb,glsl 是1.0
    gl_FragColor = vec4((vec3(1.0)-colorBase.rgb),colorBase.a);
}