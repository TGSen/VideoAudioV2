#ifdef GL_ES
precision mediump float;
#endif
varying vec2 V_textcoord;
uniform sampler2D U_Texture;
varying vec4 V_poistion;
void main()
{
    //手绘效果，这个是灰色+边缘检测结合+discard 操作
	vec4 color=vec4(0.0);
	int coreSize=3;
	//float texelOffset=1.0/rand;
	float texelOffset=0.003;
	float kernel[9];
	kernel[6]=0.0;kernel[7]=1.0;kernel[8]=0.0;
	kernel[3]=1.0;kernel[4]=-4.0;kernel[5]=1.0;
	kernel[0]=0.0;kernel[1]=1.0;kernel[2]=0.0;
	int index=0;
	for(int y=0;y<coreSize;y++)
	{
		for(int x=0;x<coreSize;x++)
		{
			vec4 currentColor=texture2D(U_Texture,V_textcoord+vec2((-1.0+float(x))*texelOffset,(-1.0+float(y))*texelOffset));
			color+=currentColor*kernel[index++];
		}
	}
	vec4 baseColor = 10.0*color+texture2D(U_Texture,V_textcoord);
	 float blendColor = (baseColor.r+baseColor.g+baseColor.b)/3.0;
	 baseColor = vec4(blendColor, blendColor, blendColor, 1.0);
	// vec3 result = vec3(1.0)-baseColor.rgb;
     gl_FragColor = baseColor;
}