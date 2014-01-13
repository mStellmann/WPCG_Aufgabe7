// Normal direction
varying vec3 N;
// Viewer direction
varying vec3 E;

/**
 * Vertex shader used for evironment shading.
 */
void main(void)
{
    // Compute the viewing direction.
    E = vec3(gl_ModelViewMatrix * gl_Vertex);
    // Compute the normal direction.
    N = normalize(gl_NormalMatrix * gl_Normal);
    // Compute the position in 3-space.
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    
}