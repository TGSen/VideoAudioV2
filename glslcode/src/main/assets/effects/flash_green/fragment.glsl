precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//rgb 的分量
const highp vec3 colorWeight  = vec3(0.2125, 0.7154, 0.0721);
//卡通效果：对照ios版本，应该需要先进行边缘检测，
void main() {
 vec4 curColor = texture2D(inputTexture,textureCoordinate);
            //1、去色（黑白化）
            float lightColor = dot(curColor.rgb, colorWeight);
            vec4 fanshe = vec4(vec3(lightColor),0.0);
            //2、获取该纹理附近的上下左右的纹理并求其去色，补色
            vec4 sample0,sample1,sample2,sample3;
            float h0,h1,h2,h3;
            float fstep=0.005;
            sample0=texture2D(inputTexture,vec2(textureCoordinate.x-fstep,textureCoordinate.y-fstep));
            sample1=texture2D(inputTexture,vec2(textureCoordinate.x+fstep,textureCoordinate.y-fstep));
            sample2=texture2D(inputTexture,vec2(textureCoordinate.x+fstep,textureCoordinate.y+fstep));
            sample3=texture2D(inputTexture,vec2(textureCoordinate.x-fstep,textureCoordinate.y+fstep));
            //这附近的4个纹理值同样得进行去色（黑白化）
            h0 = 0.299*sample0.x + 0.587*sample0.y + 0.114*sample0.z;
            h1 = 0.299*sample1.x + 0.587*sample1.y + 0.114*sample1.z;
            h2 = 0.299*sample2.x + 0.587*sample2.y + 0.114*sample2.z;
            h3 = 0.299*sample3.x + 0.587*sample3.y + 0.114*sample3.z;
            //反相，得到每个像素的补色
            sample0 = vec4(1.0-h0,1.0-h0,1.0-h0,0.0);
            sample1 = vec4(1.0-h1,1.0-h1,1.0-h1,0.0);
            sample2 = vec4(1.0-h2,1.0-h2,1.0-h2,0.0);
            sample3 = vec4(1.0-h3,1.0-h3,1.0-h3,0.0);
            //3、对反相颜色值进行均值模糊
            vec4 color=(sample0+sample1+sample2+sample3) / 4.0;
            //4、颜色减淡，将第1步中的像素和第3步得到的像素值进行计算
            vec3 reduceColor = fanshe.rgb+(fanshe.rgb*color.rgb)/(1.0-color.rgb);
            vec4 blendColor ;
              //需要混合颜色,可以根据灰色的程度设置不一样的绿色，后期调的时候
            if(reduceColor.r>0.3){
                //先变成白色
                blendColor = vec4(1.0,1.0,1.0,1.0);
                gl_FragColor = curColor;
            }else{
                //需要点绿色
                blendColor = vec4(0.0,1.0,0.0,1.0);
                gl_FragColor = blendColor;
            }
            //尝试各种混合，看看那个效果符合,这个不理想，还是上面的好点
           //  gl_FragColor = abs(curColor-endColor);
            //   gl_FragColor = abs( blendColor - curColor );
            // gl_FragColor = vec4(1.0) - ((vec4(1.0) -curColor ) * (vec4(1.0) -blendColor ));
           //  gl_FragColor = min( blendColor, curColor );;


}