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
	  float rc = clamp((0.393*baseColor.r+0.769*baseColor.g+0.189*baseColor.b),0.0,1.0);
	  float gc = clamp((0.349*baseColor.r+0.686*baseColor.g+0.168*baseColor.b),0.0,1.0);
	  float bc = clamp((0.272*baseColor.r+0.534*baseColor.g+0.131*baseColor.b),0.0,1.0);
	 gl_FragColor = vec4(rc,gc,bc,1.0);
}