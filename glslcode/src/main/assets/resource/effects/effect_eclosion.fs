#ifdef GL_ES
precision mediump float;
#endif
varying vec2 V_textcoord;
uniform sampler2D U_Texture;
varying vec4 V_poistion;
void main()
{
    //羽化效果，先求出像素点跟指定点的距离 rgba+0.5*距离
	//改点到圆点的距离
	//float point =distance(V_poistion,vec4(-0.12,0.18,V_poistion.z,V_poistion.w));
	float point =distance(V_poistion,vec4(0.0,0.0,V_poistion.z,V_poistion.w));
	vec4 baseColor = texture2D(U_Texture,V_textcoord);
	   //float rc =clamp(baseColor.r+(1.0-point)*0.5,0.0,1.0);
       //float gc =clamp(baseColor.g+(1.0-point)*0.5,0.0,1.0);
       //float bc =clamp(baseColor.b+(1.0-point)*0.5,0.0,1.0);
	   //float ac =clamp(baseColor.a+(1.0-point)*0.5,0.0,1.0);
	   float radio = 0.5;
         if(point >0.9){
        	  radio =0.9;
         }else if(point >0.8 && point<=0.9){
              radio =0.8;
         }else if(point >0.3&&point <0.8){
              radio =0.6;
          }

         float rc =clamp(baseColor.r+point*0.5,0.0,1.0);
         float gc =clamp(baseColor.g+point*0.5,0.0,1.0);
         float bc =clamp(baseColor.b+point*0.5,0.0,1.0);
         float ac =clamp(baseColor.a+point*0.5,0.0,1.0);
         baseColor = vec4(rc, gc, bc, ac);

	 gl_FragColor = baseColor;


}