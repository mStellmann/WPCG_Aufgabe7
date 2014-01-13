package factories;

import interfaces.IHalfEdgeDatastructure;
import interfaces.IHalfEdgeDatastructureOperations;
import interfaces.ITriangleMesh;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import classes.AppearanceHelper;
import classes.HalfEdgeDatastructureOperations;
import classes.Triangle;
import classes.TriangleMesh;

import com.sun.j3d.utils.geometry.GeometryInfo;

/**
 * Factory for converting a ITriangleMesh into a Shape3D.
 * 
 * @author Matthias Stellmann & Grzegorz Markiewicz
 * 
 */
public class MeshShapeFactory {

	/**
	 * private constructor, utility class.
	 */
	private MeshShapeFactory() {
	};

	/**
	 * Creates a Shape3D-Object out of an OBJ-File.
	 * 
	 * @param objFilePath
	 *            Filepath of the OBJ-File.
	 * @return Shape3D-Object
	 */
	public static Shape3D createMeshShape(String objFilepath) {
		FileReader fileReader;
		BufferedReader bufferedReader;
		Shape3D result = new Shape3D();
		String input;
		try {
			fileReader = new FileReader(objFilepath);
			bufferedReader = new BufferedReader(fileReader);

			input = bufferedReader.readLine();
			// reading all vertices from obj-File

			List<Point3d> vertexList = new ArrayList<>();
			input = readVertices(bufferedReader, input, vertexList);
			// reading all texCoords from obj-File
			List<TexCoord3f> texCoordList = new ArrayList<>();
			input = readTexCoords(bufferedReader, input, texCoordList);
			// reading all normals from obj-File
			List<Vector3d> normalList = new ArrayList<>();
			input = readNormals(bufferedReader, input, normalList);

			// check if normals and/or texCoords exists
			int arySize = 1;
			arySize += texCoordList.isEmpty() ? 0 : 1;
			arySize += normalList.isEmpty() ? 0 : 1;

			// reading all facets from obj-File
			List<Integer[][]> facetList = readFacets(bufferedReader, input, arySize);

			Map<Integer, Set<Integer>> normalFacetMap = new HashMap<>();
			if (!normalList.isEmpty()) {
				normalFacetMap = createNormalFacetMap(facetList, normalList, arySize);
			}

			ITriangleMesh mesh = new TriangleMesh();

			if (!texCoordList.isEmpty())
				for (TexCoord3f texCoord : texCoordList)
					mesh.addTexturecoordinate(texCoord);

			// Creating the Triangles
			if (!texCoordList.isEmpty() && !normalList.isEmpty()) {
				// arySize = 3
				for (Integer[][] facetInfo : facetList) {
					Point3d pA = vertexList.get(facetInfo[0][0]);
					Point3d pB = vertexList.get(facetInfo[1][0]);
					Point3d pC = vertexList.get(facetInfo[2][0]);

					int i = mesh.addVertex(pA);
					int j = mesh.addVertex(pB);
					int k = mesh.addVertex(pC);

					Triangle triangle = new Triangle(i, j, k, facetInfo[0][1], facetInfo[1][1], facetInfo[2][1]);

					Vector3f nA = computeNormal(normalFacetMap.get(facetInfo[0][2]), normalList);
					Vector3f nB = computeNormal(normalFacetMap.get(facetInfo[1][2]), normalList);
					Vector3f nC = computeNormal(normalFacetMap.get(facetInfo[2][2]), normalList);

					triangle.addNormal(nA);
					triangle.addNormal(nB);
					triangle.addNormal(nC);

					mesh.addTriangle(triangle);
				}
			} else if (!normalList.isEmpty() && texCoordList.isEmpty()) {
				// arySize = 2
				for (Integer[][] facetInfo : facetList) {
					Point3d pA = vertexList.get(facetInfo[0][0]);
					Point3d pB = vertexList.get(facetInfo[1][0]);
					Point3d pC = vertexList.get(facetInfo[2][0]);

					int i = mesh.addVertex(pA);
					int j = mesh.addVertex(pB);
					int k = mesh.addVertex(pC);

					Triangle triangle = new Triangle(i, j, k);

					Vector3f nA;
					Vector3f nB;
					Vector3f nC;

					if (facetInfo[0][1] != null) {
						nA = computeNormal(normalFacetMap.get(facetInfo[0][1]), normalList);
						nB = computeNormal(normalFacetMap.get(facetInfo[1][1]), normalList);
						nC = computeNormal(normalFacetMap.get(facetInfo[2][1]), normalList);
					} else {
						nA = computeNormal(normalFacetMap.get(facetInfo[0][0]), normalList);
						nB = computeNormal(normalFacetMap.get(facetInfo[1][0]), normalList);
						nC = computeNormal(normalFacetMap.get(facetInfo[2][0]), normalList);
					}

					triangle.addNormal(nA);
					triangle.addNormal(nB);
					triangle.addNormal(nC);

					mesh.addTriangle(triangle);
				}
				mesh.addTexturecoordinate(new TexCoord3f());
			} else if (!texCoordList.isEmpty() && normalList.isEmpty()) {
				// arySize = 2
				for (Integer[][] facetInfo : facetList) {
					Point3d pA = vertexList.get(facetInfo[0][0]);
					Point3d pB = vertexList.get(facetInfo[1][0]);
					Point3d pC = vertexList.get(facetInfo[2][0]);

					int i = mesh.addVertex(pA);
					int j = mesh.addVertex(pB);
					int k = mesh.addVertex(pC);

					Triangle triangle = new Triangle(i, j, k, facetInfo[0][1], facetInfo[1][1], facetInfo[2][1]);

					triangle.computeNormal(pA, pB, pC);
					mesh.addTriangle(triangle);
				}
			} else {
				// arySize = 1
				for (Integer[][] facetInfo : facetList) {
					Point3d pA = vertexList.get(facetInfo[0][0]);
					Point3d pB = vertexList.get(facetInfo[1][0]);
					Point3d pC = vertexList.get(facetInfo[2][0]);

					int i = mesh.addVertex(pA);
					int j = mesh.addVertex(pB);
					int k = mesh.addVertex(pC);

					Triangle triangle = new Triangle(i, j, k);
					triangle.computeNormal(pA, pB, pC);
					mesh.addTriangle(triangle);
				}
				mesh.addTexturecoordinate(new TexCoord3f());
			}
			if (!normalList.isEmpty()) {
				// TODO -> durchgehen und rechnen
			}

			result = createMeshShape(mesh);

			bufferedReader.close();
			fileReader.close();
			return result;
		} catch (FileNotFoundException e) {
			System.out.println("File not Found Exception");
			return result;
		} catch (IOException e) {
			System.out.println("IOException - Stream closing");
			return result;
		}
	}

	private static Vector3f computeNormal(Set<Integer> normalIndizes, List<Vector3d> normalList) {
		Vector3f normalVec = new Vector3f();
		for(Integer normalIndex : normalIndizes) {
			normalVec.add(new Vector3f(normalList.get(normalIndex)));
		}
//		normalVec.scale((1 / normalIndizes.size()));
//		normalVec.normalize();
//		normalVec.negate();
		return normalVec;
	}

	private static Map<Integer, Set<Integer>> createNormalFacetMap(List<Integer[][]> facetList, List<Vector3d> normalList, int arySize) {
		Map<Integer, Set<Integer>> result = new HashMap<>();
		for (int i = 0; i < facetList.size(); i++) {
			Integer[][] facetInfo = facetList.get(i);
			// arySize == 2 => normals at position x=1 in facetInfo[_][x]
			if (arySize == 2) {
					Integer normalIndexA = facetInfo[0][1];
					Integer normalIndexB =facetInfo[1][1];
					Integer normalIndexC =facetInfo[2][1];
					// normalIndex == null => normalIndex == vertexIndex
					if (normalIndexA == null) {
						normalIndexA = facetInfo[0][0];
						normalIndexB = facetInfo[1][0];
						normalIndexC = facetInfo[2][0];
					}
					if (!result.containsKey(normalIndexA)) {
						result.put(normalIndexA, new HashSet<Integer>());
					}
					if (!result.containsKey(normalIndexB)) {
						result.put(normalIndexB, new HashSet<Integer>());
					}
					if (!result.containsKey(normalIndexC)) {
						result.put(normalIndexC, new HashSet<Integer>());
					}
					result.get(normalIndexA).add(normalIndexA);
					result.get(normalIndexA).add(normalIndexB);
					result.get(normalIndexA).add(normalIndexC);
					result.get(normalIndexB).add(normalIndexA);
					result.get(normalIndexB).add(normalIndexB);
					result.get(normalIndexB).add(normalIndexC);
					result.get(normalIndexC).add(normalIndexA);
					result.get(normalIndexC).add(normalIndexB);
					result.get(normalIndexC).add(normalIndexC);
				
			} else {
				Integer normalIndexA = facetInfo[0][2];
				Integer normalIndexB =facetInfo[1][2];
				Integer normalIndexC =facetInfo[2][2];
				// normalIndex == null => normalIndex == vertexIndex
				if (normalIndexA == null) {
					normalIndexA = facetInfo[0][0];
					normalIndexB = facetInfo[1][0];
					normalIndexC = facetInfo[2][0];
				}
					
				if (!result.containsKey(normalIndexA)) {
					result.put(normalIndexA, new HashSet<Integer>());
				}
				if (!result.containsKey(normalIndexB)) {
					result.put(normalIndexB, new HashSet<Integer>());
				}
				if (!result.containsKey(normalIndexC)) {
					result.put(normalIndexC, new HashSet<Integer>());
				}
				result.get(normalIndexA).add(normalIndexA);
				result.get(normalIndexA).add(normalIndexB);
				result.get(normalIndexA).add(normalIndexC);
				result.get(normalIndexB).add(normalIndexA);
				result.get(normalIndexB).add(normalIndexB);
				result.get(normalIndexB).add(normalIndexC);
				result.get(normalIndexC).add(normalIndexA);
				result.get(normalIndexC).add(normalIndexB);
				result.get(normalIndexC).add(normalIndexC);
				
			}
		}
		return result;
	}

	/**
	 * Creates a Shape3D-Object out of an ITriangleMesh.
	 * 
	 * @param mesh
	 *            ITriangleMesh to create the Shape3D-Object.
	 * @return Shape3D-Object
	 */
	public static Shape3D createMeshShape(ITriangleMesh mesh) {
		GeometryArray geoAry = createGeometryArray(mesh);

		Shape3D resultShape = new Shape3D(geoAry, new Appearance());
//		AppearanceHelper.setColor(resultShape, new Color3f(0.2f, 0.7f, 0f));
		return resultShape;
	}

	/**
	 * Creates a Shape3D-Object with Texturecoordinates out of an ITriangleMesh.
	 * 
	 * @param mesh
	 *            ITriangleMesh to create the Shape3D-Object.
	 * @return Shape3D-Object with Texturecoordinates
	 */
	public static Shape3D createMeshShape(ITriangleMesh mesh, String textureFilename) {
		GeometryArray geoAry = createGeometryArray(mesh);
		Shape3D resultShape = new Shape3D(geoAry, AppearanceHelper.createTextureAppearance(textureFilename));
		return resultShape;
	}

	/**
	 * Creates a Shape3D-Object with texturecoordinates and shader out of an
	 * ITriangleMesh.
	 * 
	 * @param mesh
	 *            ITriangleMesh to create the Shape3D-Object.
	 * @return Shape3D-Object with texturecoordinates and shader
	 */
	public static Shape3D createMeshShape(ITriangleMesh mesh, String vertexShaderFilename, String fragmentShaderFilename, String textureFilename) {
		GeometryArray geoAry = createGeometryArray(mesh);
		Shape3D resultShape = new Shape3D(geoAry, AppearanceHelper.createShaderAppearance(vertexShaderFilename, fragmentShaderFilename, textureFilename));
		return resultShape;
	}

	/**
	 * This method creates the GeometryArray out of a TriangleMesh.
	 * 
	 * @param mesh
	 *            ITriangleMesh to create the Shape3D-Object
	 * @return GeometryArray
	 */
	private static GeometryArray createGeometryArray(ITriangleMesh mesh) {
		TriangleArray triAry = new TriangleArray(mesh.getNumberOfVertices(), TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.TEXTURE_COORDINATE_3);

		int i = 0;
		for (Triangle elem : mesh.getTriangleList()) {
			Point3d[] coords = { mesh.getVertex(elem.getI()), mesh.getVertex(elem.getJ()), mesh.getVertex(elem.getK()) };
			TexCoord3f[] texCoords = { mesh.getTexturecoordinate(elem.getTexI()), mesh.getTexturecoordinate(elem.getTexJ()), mesh.getTexturecoordinate(elem.getTexK()) };
			// Setting the coordinates and the normals of the triangle
			triAry.setCoordinates(i, coords);
			triAry.setTextureCoordinates(0, i, texCoords);

			if (elem.isNormalListEmpty()) {
				triAry.setNormal(i++, elem.getNormalVectorAsFloat());
				triAry.setNormal(i++, elem.getNormalVectorAsFloat());
				triAry.setNormal(i++, elem.getNormalVectorAsFloat());
			} else {
				for (int j = 0; j < 3; j++)
					triAry.setNormal(i++, elem.getNormal(j));
			}
		}

		GeometryInfo geoInfo = new GeometryInfo(triAry);
		return geoInfo.getGeometryArray();
	}

	/**
	 * TODO
	 * 
	 * @param mesh
	 */
	private static String readVertices(BufferedReader objReader, String input, List<Point3d> vertexList) {
		try {
			while (!input.startsWith("vt ") && !input.startsWith("vn ") && !input.startsWith("f ")) {
				// Skipping comments and empty lines
				if (!input.startsWith("v ")) {
					input = objReader.readLine();
				} else {
					String[] inputSplit = input.split("\\s+");
					double p1 = Double.parseDouble(inputSplit[1]);
					double p2 = Double.parseDouble(inputSplit[2]);
					double p3 = Double.parseDouble(inputSplit[3]);
					vertexList.add(new Point3d(p1, p2, p3));
					input = objReader.readLine();
				}
			}
		} catch (IOException e) {
			System.out.println("Reading Error");
		}
		return input;
	}

	/**
	 * TODO
	 * 
	 * @param mesh
	 */
	private static String readTexCoords(BufferedReader objReader, String input, List<TexCoord3f> texCoordList) {
		try {
			while (!input.startsWith("vn ") && !input.startsWith("f ")) {
				// Skipping comments and empty lines
				if (!input.startsWith("vt ")) {
					input = objReader.readLine();
				} else {
					String[] inputSplit = input.split("\\s+");
					float p1 = Float.parseFloat(inputSplit[1]);
					float p2 = Float.parseFloat(inputSplit[2]);
					float p3 = 0f;
					texCoordList.add(new TexCoord3f(p1, p2, p3));
					input = objReader.readLine();
				}
			}
		} catch (IOException e) {
			System.out.println("Reading Error");
		}
		return input;
	}

	/**
	 * TODO
	 * 
	 * @param mesh
	 */
	private static String readNormals(BufferedReader objReader, String input, List<Vector3d> normalList) {
		try {
			while (!input.startsWith("f ")) {
				// Skipping comments and empty lines
				if (!input.startsWith("vn ")) {
					input = objReader.readLine();
				} else {
					String[] inputSplit = input.split("\\s+");
					double p1 = Double.parseDouble(inputSplit[1]);
					double p2 = Double.parseDouble(inputSplit[2]);
					double p3 = Double.parseDouble(inputSplit[3]);
					normalList.add(new Vector3d(p1, p2, p3));
					input = objReader.readLine();
				}
			}
		} catch (IOException e) {
			System.out.println("Reading Error");
		}
		return input;
	}

	/**
	 * TODO
	 * 
	 * @param gotNormals
	 * 
	 * @param mesh
	 */
	private static List<Integer[][]> readFacets(BufferedReader objReader, String input, int arySize) {
		List<Integer[][]> indexList = new ArrayList<>();

		try {
			while (input != null) {
				// Skipping comments and empty lines
				if (!input.startsWith("f ")) {
					input = objReader.readLine();
				} else {
					// [ [vertexIndizes], [texIndizes], [normalIndizes]]
					// texIndizes and normalIndizes optional
					Integer[][] triangleArray = new Integer[3][arySize];
					String[] inputSplit = input.split("\\s+");
					if (!input.contains("/")) {
						triangleArray[0][0] = Integer.parseInt(inputSplit[1]) - 1;
						triangleArray[1][0] = Integer.parseInt(inputSplit[2]) - 1;
						triangleArray[2][0] = Integer.parseInt(inputSplit[3]) - 1;
					} else {
						for (int i = 1; i < inputSplit.length; i++) {
							String[] splitAry = inputSplit[i].split("/");
							for (int j = 0; j < arySize; j++) {
								triangleArray[i - 1][j] = Integer.parseInt(splitAry[j]) - 1;
							}
						}
					}
					indexList.add(triangleArray);
					input = objReader.readLine();
				}
			}
		} catch (IOException e) {
			System.out.println("Reading Error");
		}
		return indexList;
	}
}
