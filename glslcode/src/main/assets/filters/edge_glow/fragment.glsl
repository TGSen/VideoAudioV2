precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;
//这个是边缘发光
void main() {
    vec2 uv = textureCoordinate;
    	vec4 color=vec4(0.0);
    	int coreSize=3;
    	float texelOffset=1/150.0;
    	float kernel[9];

    	kernel[6]=-1.0;kernel[7]=-1.0;kernel[8]=-1.0;
    	kernel[3]=-1.0;kernel[4]=8.0;kernel[5]=-1.0;
    	kernel[0]=-1.0;kernel[1]=-1.0;kernel[2]=-1.0;

    	//移动高斯核,对原图做卷积计算
    	int index=0;
    	for(int y=0;y<coreSize;y++)
    	{
    		for(int x=0;x<coreSize;x++)
    		{
    			//原图像素点
    			vec4 currentColor=texture2D(U_MainTexture,V_Texcoord+vec2((-1+x)*texelOffset,(-1+y)*texelOffset));
    			//卷积计算
    			color+=currentColor*kernel[index++];
    		}
    	}
    	//根据邻域内像素的加权平均灰度值去替代模板中心像素点的值
    	color/=16.0;
    	gl_FragColor=color;

    gl_FragColor = texture2D(inputTexture, fract(uv));
}