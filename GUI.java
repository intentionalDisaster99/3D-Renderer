
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;


// The gui that we are drawing
public class GUI {

	// A simple demo gui
	public static void main(String[] args) {

		// Creating the frame that we will be using
		JFrame frame = new JFrame("Testing GUI");
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());

		// Horizontal slider
		JSlider yawSlider = new JSlider(0, 360, 180);
		pane.add(yawSlider, BorderLayout.SOUTH);

		// Vertical slider
		JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
		pane.add(pitchSlider, BorderLayout.EAST);

		// A panel to display the rendered stuff on
		JPanel renderPanel = new JPanel() {

			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;

				// Drawing the background black
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, getWidth(), getHeight());

				// This is where the rendering happens
				// wireTetrahedron(g2, pitchSlider, yawSlider);
				// tetrahedron(g2, pitchSlider, yawSlider, getWidth(), getHeight());
				// tetrahedronWithShading(g2, pitchSlider, yawSlider, getWidth(), getHeight());
				sphereish(g2, pitchSlider, yawSlider, getWidth(), getHeight(), 6);


			}

		};

		pane.add(renderPanel, BorderLayout.CENTER);

		// Telling it to redraw each time that we have a change in the sliders
		yawSlider.addChangeListener(e -> renderPanel.repaint());
		pitchSlider.addChangeListener(e -> renderPanel.repaint());

		// Actually showing the stuff
		frame.setSize(400, 400);
		frame.setVisible(true);

	}



	// Here are the different examples that I am writing
	static void wireTetrahedron(Graphics2D g2, JSlider pitchSlider, JSlider yawSlider, double width, double height) {

		// Moving to the center of the screen
		g2.translate(width / 2, height / 2);

		ArrayList<Triangle> triangles = new ArrayList<>();

		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(-100, 100, -100),
				Color.WHITE));
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(100, -100, -100),
				Color.RED));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(100, 100, 100),
				Color.GREEN));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(-100, -100, 100),
				Color.BLUE));

		// Updating the pitch and yaw of the tetrahedron
		double yaw = Math.toRadians(yawSlider.getValue());
		Matrix3 yawTransform = new Matrix3(new double[] {
				Math.cos(yaw), 0, -Math.sin(yaw),
				0, 1, 0,
				Math.sin(yaw), 0, Math.cos(yaw)
		});
		double pitch = Math.toRadians(pitchSlider.getValue());
		Matrix3 pitchTransform = new Matrix3(new double[] {
				1, 0, 0,
				0, Math.cos(pitch), Math.sin(pitch),
				0, -Math.sin(pitch), Math.cos(pitch)
		});
		Matrix3 transform = yawTransform.multiply(pitchTransform);

		// Actually drawing the lines between each vertex
		g2.setColor(Color.WHITE);
		for (Triangle t : triangles) {

			// Transforming the vertices so that they match the sliders
			Vertex v1 = transform.transform(t.v1);
			Vertex v2 = transform.transform(t.v2);
			Vertex v3 = transform.transform(t.v3);

			// Making and drawing the path
			Path2D path = new Path2D.Double();
			path.moveTo(v1.x, v1.y);
			path.lineTo(v2.x, v2.y);
			path.lineTo(v3.x, v3.y);
			path.closePath();
			g2.draw(path);
		}

	}
	

	// A prettier tetrahedron while I figure out how to draw triangles
	static void tetrahedron(Graphics2D g2, JSlider pitchSlider, JSlider yawSlider, double width, double height) {

		// This uses a buffered image, which is fun
		// We have to use barycentric coordinates, but I didn't go too far into it because
		// people have already done the math, why should I do it again? (I'll look into it more later I'm sure)
		BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

		// The same triangles as before
		ArrayList<Triangle> triangles = new ArrayList<>();
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(-100, 100, -100),
				Color.WHITE));
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(100, -100, -100),
				Color.RED));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(100, 100, 100),
				Color.GREEN));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(-100, -100, 100),
				Color.BLUE));

		// Same transforms as before
		double yaw = Math.toRadians(yawSlider.getValue());
		Matrix3 yawTransform = new Matrix3(new double[] {
				Math.cos(yaw), 0, -Math.sin(yaw),
				0, 1, 0,
				Math.sin(yaw), 0, Math.cos(yaw)
		});
		double pitch = Math.toRadians(pitchSlider.getValue());
		Matrix3 pitchTransform = new Matrix3(new double[] {
				1, 0, 0,
				0, Math.cos(pitch), Math.sin(pitch),
				0, -Math.sin(pitch), Math.cos(pitch)
		});
		Matrix3 transform = yawTransform.multiply(pitchTransform);

		// Making a depth buffer so that we know what to show at the frong
		double[] depthBuffer = new double[img.getWidth() * img.getHeight()];
		// initialize array with extremely far away depths
		for (int q = 0; q < depthBuffer.length; q++) {
			depthBuffer[q] = Double.NEGATIVE_INFINITY;
		}

		// Iterating to show each triangle
		for (Triangle t : triangles) {

			Vertex v1 = transform.transform(t.v1);
			Vertex v2 = transform.transform(t.v2);
			Vertex v3 = transform.transform(t.v3);

			// Doing the fun translations because we can't just translate to the center now
			// We are drawing an image so we need to move ot the center of the image
			v1.x += width / 2;
			v1.y += height / 2;
			v2.x += width / 2;
			v2.y += height / 2;
			v3.x += width / 2;
			v3.y += height / 2;

			// Doing fun mathy stuff to figure out the bounds for each triangle
			int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
			int maxX = (int) Math.min(img.getWidth() - 1,
					Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
			int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
			int maxY = (int) Math.min(img.getHeight() - 1,
					Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

			// Now to calculate the area of the triangle
			double area = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

			// Iterating to change the color of each one on the image
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x < maxX; x++) {
					double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / area;
					double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / area;
					double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / area;
					if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
						// Checking to see if we should show it or not based on depth
						double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
						int zIndex = y * img.getWidth() + x;
						if (depthBuffer[zIndex] < depth) {
							img.setRGB(x, y, t.color.getRGB());
							depthBuffer[zIndex] = depth;
						}
					}
				}
			}

		}

		// Actually drawing the thing
		g2.drawImage(img, 0, 0, null);

	}
	
	// Now it's on to shading so that it looks a bit more realistic
	static void tetrahedronWithShading(Graphics2D g2, JSlider pitchSlider, JSlider yawSlider, double width,
			double height) {

		// This uses a buffered image, which is fun
		// We have to use barycentric coordinates, but I didn't go too far into it because
		// people have already done the math, why should I do it again? (I'll look into it more later I'm sure)
		BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

		// The same triangles as before
		ArrayList<Triangle> triangles = new ArrayList<>();
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(-100, 100, -100),
				Color.WHITE));
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(100, -100, -100),
				Color.RED));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(100, 100, 100),
				Color.GREEN));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(-100, -100, 100),
				Color.BLUE));

		// Same transforms as before
		double yaw = Math.toRadians(yawSlider.getValue());
		Matrix3 yawTransform = new Matrix3(new double[] {
				Math.cos(yaw), 0, -Math.sin(yaw),
				0, 1, 0,
				Math.sin(yaw), 0, Math.cos(yaw)
		});
		double pitch = Math.toRadians(pitchSlider.getValue());
		Matrix3 pitchTransform = new Matrix3(new double[] {
				1, 0, 0,
				0, Math.cos(pitch), Math.sin(pitch),
				0, -Math.sin(pitch), Math.cos(pitch)
		});
		Matrix3 transform = yawTransform.multiply(pitchTransform);

		// Making a depth buffer so that we know what to show at the frong
		double[] depthBuffer = new double[img.getWidth() * img.getHeight()];
		// initialize array with extremely far away depths
		for (int q = 0; q < depthBuffer.length; q++) {
			depthBuffer[q] = Double.NEGATIVE_INFINITY;
		}

		// Iterating to show each triangle
		for (Triangle t : triangles) {

			Vertex v1 = transform.transform(t.v1);
			Vertex v2 = transform.transform(t.v2);
			Vertex v3 = transform.transform(t.v3);

			// Doing the fun translations because we can't just translate to the center now
			// We are drawing an image so we need to move ot the center of the image
			v1.x += width / 2;
			v1.y += height / 2;
			v2.x += width / 2;
			v2.y += height / 2;
			v3.x += width / 2;
			v3.y += height / 2;

			// Doing fun mathy stuff to figure out the bounds for each triangle
			int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
			int maxX = (int) Math.min(img.getWidth() - 1,
					Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
			int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
			int maxY = (int) Math.min(img.getHeight() - 1,
					Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

			// Now to calculate the area of the triangle
			double area = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

			// Calculating the normal so that we can shade things 
			Vertex norm = new Vertex(
					v3.y * v2.z - v3.z * v2.y,
					v3.z * v2.x - v3.x * v2.z,
					v3.x * v2.y - v3.y * v2.x);
			// Normalizing the normal heh
			double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
			norm.x /= normalLength;
			norm.y /= normalLength;
			norm.z /= normalLength;

			double angleCos = Math.abs(norm.z);

			// Iterating to change the color of each one on the image
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x < maxX; x++) {
					double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / area;
					double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / area;
					double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / area;
					if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
						// Checking to see if we should show it or not based on depth
						double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
						int zIndex = y * img.getWidth() + x;
						if (depthBuffer[zIndex] < depth) {
							img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
							depthBuffer[zIndex] = depth;
						}
					}
				}
			}

		}

		// Actually drawing the thing
		g2.drawImage(img, 0, 0, null);

	}
	
	// A function to return the color based on the angle between the normal and the light source
	public static Color getShade(Color color, double shade) {

		// This is just an approximation for sRGB to linear RGB and back (Java uses sRGB)

		double redLinear = Math.pow(color.getRed(), 2.4) * shade;
		double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
		double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

		int red = (int) Math.pow(redLinear, 1 / 2.4);
		int green = (int) Math.pow(greenLinear, 1 / 2.4);
		int blue = (int) Math.pow(blueLinear, 1 / 2.4);

		return new Color(red, green, blue);
	}

	// Shows a bit of a sphere by subdividing the trianlges
	static void sphereish(Graphics2D g2, JSlider pitchSlider, JSlider yawSlider, double width,
			double height, int sphericity) {

		// This uses a buffered image, which is fun
		// We have to use barycentric coordinates, but I didn't go too far into it because
		// people have already done the math, why should I do it again? (I'll look into it more later I'm sure)
		BufferedImage img = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

		// The same triangles as before
		ArrayList<Triangle> triangles = new ArrayList<>();
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(-100, 100, -100),
				Color.WHITE));
		triangles.add(new Triangle(new Vertex(100, 100, 100),
				new Vertex(-100, -100, 100),
				new Vertex(100, -100, -100),
				Color.RED));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(100, 100, 100),
				Color.GREEN));
		triangles.add(new Triangle(new Vertex(-100, 100, -100),
				new Vertex(100, -100, -100),
				new Vertex(-100, -100, 100),
				Color.BLUE));

		// Same transforms as before
		double yaw = Math.toRadians(yawSlider.getValue());
		Matrix3 yawTransform = new Matrix3(new double[] {
				Math.cos(yaw), 0, -Math.sin(yaw),
				0, 1, 0,
				Math.sin(yaw), 0, Math.cos(yaw)
		});
		double pitch = Math.toRadians(pitchSlider.getValue());
		Matrix3 pitchTransform = new Matrix3(new double[] {
				1, 0, 0,
				0, Math.cos(pitch), Math.sin(pitch),
				0, -Math.sin(pitch), Math.cos(pitch)
		});
		Matrix3 transform = yawTransform.multiply(pitchTransform);

		// Making a depth buffer so that we know what to show at the frong
		double[] depthBuffer = new double[img.getWidth() * img.getHeight()];
		// initialize array with extremely far away depths
		for (int q = 0; q < depthBuffer.length; q++) {
			depthBuffer[q] = Double.NEGATIVE_INFINITY;
		}

		// Inflating our triangles
		for (int i = 0; i < sphericity; i++) {
			triangles = inflate(triangles);
		}

		// Iterating to show each triangle
		for (Triangle t : triangles) {

			Vertex v1 = transform.transform(t.v1);
			Vertex v2 = transform.transform(t.v2);
			Vertex v3 = transform.transform(t.v3);

			// Doing the fun translations because we can't just translate to the center now
			// We are drawing an image so we need to move ot the center of the image
			v1.x += width / 2;
			v1.y += height / 2;
			v2.x += width / 2;
			v2.y += height / 2;
			v3.x += width / 2;
			v3.y += height / 2;

			// Doing fun mathy stuff to figure out the bounds for each triangle
			int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
			int maxX = (int) Math.min(img.getWidth() - 1,
					Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
			int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
			int maxY = (int) Math.min(img.getHeight() - 1,
					Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

			// Now to calculate the area of the triangle
			double area = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

			// A bit of a better normal calculation
			Vertex edge1 = new Vertex(t.v2.x - t.v1.x, t.v2.y - t.v1.y, t.v2.z - t.v1.z);
			Vertex edge2 = new Vertex(t.v3.x - t.v1.x, t.v3.y - t.v1.y, t.v3.z - t.v1.z);

			// Compute the normal vector using the cross product
			Vertex norm = new Vertex(
				edge1.y * edge2.z - edge1.z * edge2.y,
				edge1.z * edge2.x - edge1.x * edge2.z,
				edge1.x * edge2.y - edge1.y * edge2.x
			);
			// Normalize the normal vector
			double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
			if (normalLength != 0) {
				norm.x /= normalLength;
				norm.y /= normalLength;
				norm.z /= normalLength;
			}

			double angleCos = Math.abs(norm.z);

			// Iterating to change the color of each one on the image
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / area;
					double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / area;
					double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / area;
					if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
						// Checking to see if we should show it or not based on depth
						double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
						int zIndex = y * img.getWidth() + x;
						if (depthBuffer[zIndex] < depth) {
							img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
							depthBuffer[zIndex] = depth;
						}
					}
				}
			}

		}

		// Actually drawing the thing
		g2.drawImage(img, 0, 0, null);

	}

	// A method that inflates the tetrahedron by subdividing it until it looks a bit like a sphere
	public static ArrayList<Triangle> inflate(ArrayList<Triangle> triangles) {
		ArrayList<Triangle> result = new ArrayList<>();
		for (Triangle t : triangles) {
			Vertex m1 =
				new Vertex((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
			Vertex m2 =
				new Vertex((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
			Vertex m3 =
				new Vertex((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);
			result.add(new Triangle(t.v1, m1, m3, t.color));
			result.add(new Triangle(t.v2, m1, m2, t.color));
			result.add(new Triangle(t.v3, m2, m3, t.color));
			result.add(new Triangle(m1, m2, m3, t.color));
		}
		for (Triangle t : result) {
			for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
				double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
				v.x /= l;
				v.y /= l;
				v.z /= l;
			}
		}
		return result;
	}


}



// Down here is where are classes for the vertices and triangles will be 

class Vertex {

	double x;
	double y;
	double z;

	// Constructor
	Vertex(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

}

class Triangle {

	Vertex v1;
	Vertex v2;
	Vertex v3;
	Color color;

	Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.color = color;		
	}

}



// The matrix classes for the math and stuff
class Matrix3 { // Only for 1 x 3 and 3 x 3 matrices, hence the 3

	double[] values;
	
	// Constructor
	Matrix3(double[] values) {
		this.values = values;
	}
	
	// Simple matrix multiplication operation 
	Matrix3 multiply(Matrix3 other) {
		double[] output = new double[9];

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				for (int i = 0; i < 3; i++) {
					output[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
				}
			}
		}
		return new Matrix3(output);
	}

	// Another simple operation that rotates a vertex using a matrix
	Vertex transform(Vertex in) {
		return new Vertex(
			in.x * values[0] + in.y * values[3] + in.z * values[6],
			in.x * values[1] + in.y * values[4] + in.z * values[7],
			in.x * values[2] + in.y * values[5] + in.z * values[8]
		);
	}
		

}


