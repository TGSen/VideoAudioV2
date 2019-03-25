#ifdef GL_ES
precision mediump float;
#endif
varying vec2 V_textcoord;
uniform sampler2D U_Texture;
void main(){
    vec4 color = vec4(0.0,0.0,0.0,0.0);
    int coreSize = 3;
    float core[3];
    core[0]= 0.0;core[1]= 1.0;core[2]= 2.0;
    float texelOffset = 1.0/200.0;
    float kernel[9];
    //权重因子
    //kernel[0]=1;kernel[1]=2;kernel[2]=1;
   // kernel[3]=2;kernel[4]=4;kernel[5]=2;
    //kernel[6]=1;kernel[7]=2;kernel[8]=1;

     kernel[0]=1.0;kernel[1]=2.0;kernel[2]=1.0;
     kernel[3]=2.0;kernel[4]=4.0;kernel[5]=2.0;
     kernel[6]=1.0;kernel[7]=2.0;kernel[8]=1.0;
    int index = 0;

    for(int y= 0;y<coreSize;y++){
        for(int x= 0;x<coreSize;x++){
            float xOffset =(-1.0+core[x])*texelOffset;
            float yOffset = (-1.0+core[y])*texelOffset;
            vec4 currentColor = texture2D(U_Texture,V_textcoord+vec2(xOffset,yOffset));
            color+=currentColor*kernel[index++];
        }
    }
    //这步很重要，如果不做这步的话，就容易产生曝光
    color/=16.0;
    gl_FragColor = color;
}