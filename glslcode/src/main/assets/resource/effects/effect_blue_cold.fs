#ifdef GL_ES
precision mediump float;
#endif
varying vec2 V_textcoord;
uniform sampler2D U_Texture;
varying vec4 V_poistion;
void main()
{
    //旧图片效果，算法原型来自图像编程精髓
	vec4 baseColor = texture2D(U_Texture,V_textcoord);
	  float rc = clamp(abs(baseColor.r-baseColor.g-baseColor.b),0.0,1.0);
	  float gc = clamp(abs(baseColor.g-baseColor.b-baseColor.r),0.0,1.0);
	  float bc = clamp(abs(baseColor.b-baseColor.g-baseColor.r),0.0,1.0);
	 gl_FragColor = vec4(rc,gc,bc,1.0);
}