#define M_PI 3.1415926535897932384626433832795

// Normal direction
varying vec3 N;
// Viewer direction
varying vec3 E;

uniform sampler2D my_color_texture;

/**
* Fragment shader used for evironment shading.
*/
void main(void) 
{
	// R = E – 2(E * N)N
	vec3 R = normalize(reflect(E, N));
    float u = (M_PI + atan(R.y, R.x)) / (2.0 * M_PI);
    float v = (atan(sqrt((R.x * R.x) + (R.y * R.y)), R.z)) / M_PI;
    vec2 texture_coordinate = vec2(u,v);
    gl_FragColor = texture2D(my_color_texture, texture_coordinate);
}