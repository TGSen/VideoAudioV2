precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//rgb 的分量
const highp vec3 colorWeight  = vec3(0.2125, 0.7154, 0.0721);
//卡通效果：对照ios版本，应该需要先进行边缘检测，
void main() {
       	vec4 color=vec4(0.0);
    	int coreSize=3;
    	//float texelOffset=1.0/rand;
    	float texelOffset=0.005;
    	//
    	float kernel[9];

    	//Kirsch 算子
    	//kernel[6]=5.0;kernel[7]=-3.0;kernel[8]=-3.0;
       // kernel[3]=5.0;kernel[4]=0.0;kernel[5]=-3.0;
        //kernel[0]=5.0;kernel[1]=-3.0;kernel[2]=-3.0;

        kernel[6]=0.0;kernel[7]=1.0;kernel[8]=0.0;
        kernel[3]=1.0;kernel[4]=-4.0;kernel[5]=1.0;
        kernel[0]=0.0;kernel[1]=1.0;kernel[2]=0.0;


    	int index=0;
    	for(int y=0;y<coreSize;y++)
    	{
    		for(int x=0;x<coreSize;x++)
    		{
    			vec4 currentColor=texture2D(inputTexture,textureCoordinate+vec2((-1.0+float(x))*texelOffset,(-1.0+float(y))*texelOffset));
    			color+=currentColor*kernel[index++];
    		}
    	}
    	vec4 oldColor= texture2D(inputTexture,textureCoordinate);
    	vec4 baseColor = 10.0*color+oldColor;
    	// float lightColor = dot(baseColor.rgb, colorWeight);
    	float lightColor = baseColor.r;
    	 //这个参数得调一下
    	 if(lightColor>0.05){
    	    lightColor = 1.0;
    	    baseColor = vec4(vec3(lightColor), 1.0);
    	 }else if(lightColor<0.05 && lightColor>0.0  ){
    	    baseColor = vec4(1.0,0.0,0.0, 1.0);
    	 }else{
    	    lightColor =0.0;
    	     baseColor = vec4(vec3(lightColor), baseColor.a);
    	 }

         gl_FragColor = baseColor;
}