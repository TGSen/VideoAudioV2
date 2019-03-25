precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
    if (uv.y >= 0.5) {
        if (uv.x <= 0.5) {
                uv.x = uv.x * 2.0;
            } else {
                uv.x = (uv.x - 0.5) * 2.0;
            }
        uv.y = uv.y * 2.0;
    } else {
        uv.x = uv.x;
        uv.y = uv.y-0.5;
    }
    gl_FragColor = texture2D(inputTexture, fract(uv));
}