/**
 * Prof. Philipp Jenke
 * Hochschule für Angewandte Wissenschaften (HAW), Hamburg
 * Lecture demo program.
 */
package classes;

import java.io.File;
import java.io.IOException;

import javax.media.j3d.Appearance;
import javax.media.j3d.GLSLShaderProgram;
import javax.media.j3d.Material;
import javax.media.j3d.Shader;
import javax.media.j3d.ShaderAppearance;
import javax.media.j3d.ShaderProgram;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SourceCodeShader;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.vecmath.Color3f;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.shader.StringIO;

/**
 * @author Philipp Jenke
 * 
 */
public class AppearanceHelper {

	/**
	 * Set the color for a primitive.
	 * 
	 * @param primitive
	 *            Primitive 3D object.
	 * @param diffuseColor
	 *            (Diffuse) color in the appearance of the primitive.
	 */
	public static void setColor(Primitive primitive, Color3f diffuseColor) {
		Color3f emissiveColor = new Color3f(0, 0, 0);
		Color3f ambientColor = new Color3f(0, 0, 0);
		Color3f specularColor = new Color3f(1, 1, 1);
		float shininessValue = 128.0f;
		Material material = new Material(ambientColor, emissiveColor, diffuseColor, specularColor, shininessValue);
		Appearance ap = new Appearance();
		ap.setMaterial(material);
		primitive.setAppearance(ap);
	}

	/**
	 * Set the color for a shape.
	 * 
	 * @param shape
	 *            Shape object.
	 * @param diffuseColor
	 *            Diffuse color of the object.
	 */
	public static void setColor(Shape3D shape, Color3f diffuseColor) {
		Color3f emissiveColor = new Color3f(0, 0, 0);
		Color3f specularColor = new Color3f(10.0f, 10.0f, 10.0f);
		Color3f ambientColor = new Color3f(0, 0, 0);
		float shininessValue = 128.0f;
		Material material = new Material(ambientColor, emissiveColor, diffuseColor, specularColor, shininessValue);
		Appearance ap = new Appearance();
		ap.setMaterial(material);
		shape.setAppearance(ap);
	}

	/**
	 * Create a appearance object for a given texture filename.
	 * 
	 * @param textureFilename
	 *            Name of the texture file.
	 * @return Appearance for the texture.
	 */
	public static Appearance createTextureAppearance(String textureFilename) {
		Appearance app = new Appearance();

		// Your code here
		app.setTexture(createTexture(textureFilename));

		// This attribute tells the appearance how to handle the texture.
		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		app.setTextureAttributes(texAttr);
		return app;
	}

	/**
	 * Create a texture object. This can be used for regular texture Appearances
	 * or for shader Appearances which use textures.
	 * 
	 * @param textureFilename
	 * @return
	 */
	private static Texture createTexture(String textureFilename) {
		return new TextureLoader(textureFilename, TextureLoader.BY_REFERENCE | TextureLoader.Y_UP, null).getTexture();
	}

	/**
	 * Create an appearance based on a vertex and a fragment shader. A texture
	 * can additionally by provided (optionally). If it is uses is defined in
	 * the shaders.
	 * 
	 * @param vertexShaderFilename
	 *            Filename of the vertex shader source code file.
	 * @param fragmentShaderFilename
	 *            Filename of the fragment shader source code file.
	 * @param textureFilename
	 *            Filename of the texture file. Use null, if no texture is used.
	 * @return Appearance object based on the vertex and fragment shader.
	 *         Returns null, if the creation of the shader Appearance failed.
	 */
	public static Appearance createShaderAppearance(String vertexShaderFilename, String fragmentShaderFilename, String textureFilename) {

		ShaderAppearance appearance = new ShaderAppearance();

		// Check if vertex shader file exists.
		if (!new File(vertexShaderFilename).exists()) {
			System.out.println("Can't find vertex shader " + vertexShaderFilename);
			return null;
		}

		// Check if fragment shader file exists.
		if (!new File(vertexShaderFilename).exists()) {
			System.out.println("Can't find fragment shader " + fragmentShaderFilename);
			return null;
		}

		// Try to create a texture, if the a filename is provided.
		if (textureFilename != null) {
			Texture tex = createTexture(textureFilename);
			if (tex == null) {
				System.out.println("Failed to create texture.");
				return null;
			}
			appearance.setTexture(tex);
		}

		// Read the shader programs from file.
		String vertexProgram = null;
		String fragmentProgram = null;
		try {
			vertexProgram = StringIO.readFully(vertexShaderFilename);
			fragmentProgram = StringIO.readFully(fragmentShaderFilename);
		} catch (IOException e) {
			System.out.println("Failed to read shader program.");
			throw new RuntimeException(e);
		}

		// Create shader objects and assemble them to a program.
		Shader[] shaders = new Shader[2];
		shaders[0] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_VERTEX, vertexProgram);
		shaders[1] = new SourceCodeShader(Shader.SHADING_LANGUAGE_GLSL, Shader.SHADER_TYPE_FRAGMENT, fragmentProgram);
		ShaderProgram shaderProgram = new GLSLShaderProgram();
		shaderProgram.setShaders(shaders);

		// Finalize and retur the shader Appearance
		appearance.setShaderProgram(shaderProgram);
		return appearance;
	}
}
