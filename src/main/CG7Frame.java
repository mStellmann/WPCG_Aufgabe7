package main;

import factories.MeshShapeFactory;
import interfaces.ICurve;

import java.awt.BorderLayout;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import classes.AppearanceHelper;
import classes.HermiteCurve;
import classes.MonomialCurve;
import classes.SliderListener;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class CG7Frame extends JFrame {

	/**
	 * Gernerated serialnumber.
	 */
	private static final long serialVersionUID = -6619879728982489623L;

	/**
	 * Canvas object for the 3D content.
	 */
	private Canvas3D canvas3D;

	/**
	 * Simple universe (provides reasonable default values).
	 */
	protected SimpleUniverse universe;

	/**
	 * Scene graph for the 3D content scene.
	 */
	protected BranchGroup scene = new BranchGroup();

	/**
	 * Default constructor.
	 */
	public CG7Frame() {
		// Create canvas object to draw on
		canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

		// The SimpleUniverse provides convenient default settings
		universe = new SimpleUniverse(canvas3D);
		universe.getViewingPlatform().setNominalViewingTransform();

		// Setup lighting
//		addLight(universe);

		// Allow for mouse control
		OrbitBehavior ob = new OrbitBehavior(canvas3D);
		ob.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.MAX_VALUE));
		universe.getViewingPlatform().setViewPlatformBehavior(ob);

		// Set the background color
		Background background = new Background(new Color3f(0.9f, 0.9f, 0.9f));
		BoundingSphere sphere = new BoundingSphere(new Point3d(0, 0, 0), 100000);
		background.setApplicationBounds(sphere);
		scene.addChild(background);

		// Setup frame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Aufgabe 7 - Meshes from .obj");
		setSize(500, 500);
		getContentPane().add("Center", canvas3D);
		setVisible(true);
	}

	/**
	 * Setup the lights in the scene. Attention: The light need to be added to
	 * the scene before the scene in compiled (see createSceneGraph()).
	 */
	private void addLight(SimpleUniverse universe) {
		addPointLight(new Point3f(10, 10, 10));
		addPointLight(new Point3f(-10, -10, -10));
		addPointLight(new Point3f(10, -10, 10));
		addDirectionalLight(new Vector3f(0, 0, 1));
	}

	void addPointLight(Point3f position) {
		PointLight light = new PointLight();
		light.setPosition(position);
		light.setColor(new Color3f(1, 1, 1));
		light.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
		scene.addChild(light);
	}

	void addDirectionalLight(Vector3f direction) {
		DirectionalLight light = new DirectionalLight();
		light.setDirection(direction);
		light.setColor(new Color3f(1, 1, 1));
		light.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
		scene.addChild(light);
	}

	/**
	 * Create the default scene graph.
	 */
	protected void createSceneGraph() {
		Shape3D shape = MeshShapeFactory.createMeshShape("ressources/meshes/cow.obj");
		AppearanceHelper.setColor(shape, new Color3f(0f, 0.3f, 0.4f));
//				AppearanceHelper.createShaderAppearance("ressources/shader/vertex_shader_environment_mapping.glsl", 
//				"ressources/shader/fragment_shader_environment_mapping.glsl", "ressources/environmentmaps/environment_parkinglot.jpg");
//		shape.setAppearance(AppearanceHelper.createShaderAppearance("ressources/shader/vertex_shader_environment_mapping.glsl", 
//				"ressources/shader/fragment_shader_environment_mapping.glsl", "ressources/environmentmaps/environmentmap_forest.jpg"));
		shape.setAppearance(AppearanceHelper.createShaderAppearance("ressources/shader/vertex_shader_environment_mapping.glsl", 
				"ressources/shader/fragment_shader_environment_mapping.glsl", "ressources/environmentmaps/environmentmap_coast.jpg"));
		
		Transform3D transform3d = new Transform3D();
		transform3d.setScale(0.3);
		TransformGroup transformGroup = new TransformGroup(transform3d);
		transformGroup.addChild(shape);
		scene.addChild(transformGroup);
		// Assemble scene
		scene.compile();
		universe.addBranchGraph(scene);
	}

	private Shape3D createCurveLine(ICurve curve) {
		Point3f[] points = new Point3f[1000];
		for (int i = 0; i < 1000; i++) {
			points[i] = new Point3f(curve.eval((double) i / 1000));
		}
		LineArray lineAry = new LineArray(points.length, LineArray.COORDINATES);
		lineAry.setCoordinates(0, points);
		LineAttributes lineAttributes = new LineAttributes(0.2f, LineAttributes.PATTERN_DASH_DOT, true);
		Appearance lineAppearance = new Appearance();
		lineAppearance.setLineAttributes(lineAttributes);
		lineAppearance.setColoringAttributes(new ColoringAttributes(new Color3f(0f, 0f, 0f), ColoringAttributes.SHADE_FLAT));
		return new Shape3D(lineAry, lineAppearance);
	}

	/**
	 * Starting method.
	 * 
	 * @param args
	 *            Program arguments.
	 */
	public static void main(String[] args) {
		// Create the central frame
		CG7Frame frame = new CG7Frame();
//		// Add content to the scene graph
		frame.createSceneGraph();
	}

}
