#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D  U_Texture;//纹理贴图
varying vec2 V_textcoord;
varying vec4 V_poistion;
uniform vec4 FileterZoom;

uniform vec4 U_LeftEyePos;
uniform vec4 U_RightEyePos;
uniform vec4 U_ScaleNum;

//http://www.gson.org/thesis/warping-thesis.pdf
//根据当前点与中心点的距离
//计算缩放后点与中心点的距离
float fs(float r,float rmax){
    return (1.-pow(r/rmax-1.,2.)*U_ScaleNum.x)*r;
}


//cur = 当前要采样的点 eye=眼睛点 rmax = 最大作用半径
vec2 newCoord(vec2 coord,vec2 eye ,float rmax){
   vec2 newCoord = coord;
   //获得当前画的点和眼睛的距离
   float r = distance(coord,eye);
   //在rmax范围内 需要缩放
   if(r < rmax){
        // 获得缩放后的距离
        float fsr = fs(r,rmax);
        //fsr是个float 是距离eye的距离, 我们需要得到点的位置 是一个向量
        // 那么根据比例关系 可以得出
        // 新点与eye的向量差 / 老点与eye的差 = 新点与eye距离/老点与eye距离
        // (newCoord - eye) / (coord - eye ) = fsr/r
         // newCoord - eye =  fsr/r * (coord - eye)
        newCoord = fsr / r * (coord - eye) + eye;

   }
   return newCoord;
}
void main(){
    float rmax = distance(U_LeftEyePos.xy,U_RightEyePos.xy)/2.0;
     vec2 newCoord = newCoord(U_LeftEyePos.xy, V_textcoord,rmax);
     newCoord = newCoord(U_RightEyePos.xy, newCoord,rmax);
    //无滤镜
    gl_FragColor = texture2D(U_Texture, newCoord);


}

