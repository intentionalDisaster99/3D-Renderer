
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.Path2D;


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

				// Moving to the center of the screen
				g2.translate(getWidth() / 2, getHeight() / 2);

				// This is where the rendering happens
				tetrahedron(g2, pitchSlider, yawSlider);

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
	static void tetrahedron(Graphics2D g2, JSlider pitchSlider, JSlider yawSlider) {

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


