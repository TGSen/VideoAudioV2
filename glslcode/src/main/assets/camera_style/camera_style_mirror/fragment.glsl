precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
     if (uv.x <=0.5) {
            uv.x = uv.x;
      }else{
            uv.x = 1.0-uv.x;
      }
       uv.y = uv.y;
    gl_FragColor = texture2D(inputTexture, fract(uv));
}