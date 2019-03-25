precision mediump float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;

void main() 
{
    highp vec2 uv = textureCoordinate;
    vec4 color;
    if (uv.y >= 0.0 && uv.y <= 0.33) { // 上层
        vec2 coordinate = vec2(uv.x, uv.y + 0.33);
        color = texture2D(inputTexture, coordinate);
    } else if (uv.y > 0.33 && uv.y <= 0.67) {   // 中间层
        color = texture2D(inputTexture, uv);
    } else {    // 下层
        vec2 coordinate = vec2(uv.x, uv.y - 0.33);
        color = texture2D(inputTexture, coordinate);
    }
    gl_FragColor = color;
}