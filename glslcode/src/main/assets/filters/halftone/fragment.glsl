precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

const highp float fractionalWidthOfPixel =  0.01;
const highp float aspectRatio =1.2;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main()
{
	highp vec2 sampleDivisor = vec2(fractionalWidthOfPixel, fractionalWidthOfPixel / aspectRatio);

	highp vec2 samplePos = textureCoordinate - mod(textureCoordinate, sampleDivisor) + 0.5 * sampleDivisor;
	highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
	highp vec2 adjustedSamplePos = vec2(samplePos.x, (samplePos.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
	highp float distanceFromSamplePoint = distance(adjustedSamplePos, textureCoordinateToUse);

	lowp vec3 sampledColor = texture2D(inputTexture, samplePos ).rgb;
	highp float dotScaling = 1.0 - dot(sampledColor, W);

	lowp float checkForPresenceWithinDot = 1.0 - step(distanceFromSamplePoint, (fractionalWidthOfPixel * 0.5) * dotScaling);

	gl_FragColor = vec4(vec3(checkForPresenceWithinDot), 1.0);
}