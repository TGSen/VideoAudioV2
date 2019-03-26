precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//径向模糊效果
void main() {
    vec4 clraverge = vec4(0.0, 0.0, 0.0, 0.0);
    //range 作为参数来传入
    float range = 6.0, count = 0.0, x1, y1;
    vec2 cpos = vec2(0.5, 0.5);
    for (float j = 1.0; j <= range; j += 1.0){
        // 横坐标为圆心坐标时，计算k分母为0
        if (cpos.x - textureCoordinate.x == 0.0){
            x1 = textureCoordinate.x;
            //y 方向上一定范围内的采样（0 - 1/10的差值）
            y1 = textureCoordinate.y + (cpos.y - textureCoordinate.y) * j / (10.0 * range);
        }
        else {
            // 直线的斜率
            float k = (cpos.y - textureCoordinate.y) / (cpos.x - textureCoordinate.x);
            // x 方向
            x1 = textureCoordinate.x + (cpos.x - textureCoordinate.x) * j / (10.0 * range);
            // k 方向
            y1 = cpos.y - (cpos.x - x1) * k;
            // 如果超出指定范围则跳过
            if (x1 < 0.0 || y1 < 0.0 || x1 > 1.0 || y1 > 1.0){
                continue;
            }
        }
        clraverge += texture2D(inputTexture, vec2(x1, y1));
        count += 1.0;
    }
    // 取径向范围内的平均值
    clraverge /= count;
    gl_FragColor = clraverge;
}