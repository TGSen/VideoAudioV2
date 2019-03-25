#version 300 es
#ifdef GL_ES
precision mediump float;
#endif
in vec2 V_Texcoord;
uniform sampler2D U_Texture;
layout (location = 0) out vec4 FragColor;
void main()
{
	//gl_FragColor=texture2D(U_Texture,V_Texcoord);

	FragColor=texture(U_Texture,V_Texcoord);
}