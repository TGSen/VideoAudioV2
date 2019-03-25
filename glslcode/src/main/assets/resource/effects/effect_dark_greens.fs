#ifdef GL_ES
precision mediump float;
#endif
varying vec2 V_textcoord;
uniform sampler2D U_Texture;
varying vec4 V_poistion;
void main()
{
    //碧绿效果，算法原型来自图像编程精髓
	vec4 baseColor = texture2D(U_Texture,V_textcoord);
	  float rc = clamp(pow((baseColor.g-baseColor.b),2.0)/0.5,0.0,1.0);
	  float gc = clamp(pow((baseColor.r-baseColor.b),2.0)/0.5,0.0,1.0);
	  float bc = clamp(pow((baseColor.r-baseColor.g),2.0)/0.5,0.0,1.0);
	 gl_FragColor = vec4(rc,gc,bc,1.0);
}