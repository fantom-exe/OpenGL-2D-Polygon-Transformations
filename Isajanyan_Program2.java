/***************************************************************
 * file: Isajanyan_Program2.java
 * author: Edward Isajanyan
 * class: CS 4450
 *
 * assignment: program 1
 * date: 3/5/2019
 *
 * purpose: Reads coordinates from a text file 'coordinates.txt'
 * and draws the filled polygons using the scanline polygon fill
 * algorithm.
 *
 ****************************************************************/

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;


public class Isajanyan_Program2 {
	// stores all polygons
	private ArrayList<Polygon> polygonArray = new ArrayList<>();

	// edge tables
	
 
	// start
	private void start() {
		try {
			readCoordinates();
			createWindow();
			initGL();
			render();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// read coordinates from file
	private void readCoordinates() throws FileNotFoundException {
		Scanner  scanner = new Scanner(new File("coordinates.txt"));
		String   line;
		String[] tokens;
		
		Polygon polygon = new Polygon();
		
		while(scanner.hasNextLine()) {
			line   = scanner.nextLine();
			tokens = line.split(" ");
			
			if(tokens[0].equals("P")) {
				polygon = new Polygon(); // create new polygon
				polygon.setColor(Float.parseFloat(tokens[1]),
								 Float.parseFloat(tokens[2]),
								 Float.parseFloat(tokens[3])); // store color
         
				do { // store vertices
					line = scanner.nextLine(); // go to next line
					tokens = line.split(" ");

					polygon.addVertex(Integer.parseInt(tokens[0]),
									  Integer.parseInt(tokens[1])); // store vertices
				} while(scanner.hasNextInt());

				polygonArray.add(polygon); // add polygon to array
			}
			else if(tokens[0].equals("T")) {
				do { // store transitions
					line = scanner.nextLine(); // go to next line
					tokens = line.split(" ");

					switch(tokens[0].charAt(0)) {
						case 'r':
							polygon.addRotation(Integer.parseInt(tokens[1]),
												Integer.parseInt(tokens[2]),
												Integer.parseInt(tokens[3]));
							break;
						case 's':
							polygon.addScaling(Float.parseFloat(tokens[1]),
											   Float.parseFloat(tokens[2]),
											   Float.parseFloat(tokens[3]),
											   Float.parseFloat(tokens[4]));
							break;
						case 't':
							polygon.addTranslation(Integer.parseInt(tokens[1]),
												   Integer.parseInt(tokens[2]));
							break;
					}
				} while(!scanner.hasNext("[P]") && scanner.hasNextLine());

			}

		} // while

		scanner.close();
	}
	
	// create window
	private void createWindow() throws Exception{
		Display.setFullscreen(false);

		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.setTitle("Isajanyan_Program2");
		Display.create( );
	}
	
	// init GL
	private void initGL() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(-320, 320, -240, 240, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	}
	
	// render
	private void render() {
		// apply transformations & update vertices
		polygonArray.forEach(Polygon::applyTransformations);
		
		// init edge tables
		polygonArray.forEach(Polygon::initAllEdges);

		while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			try {
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				glLoadIdentity( );

				// draw polygons
				polygonArray.forEach(Polygon::draw);

				Display.update();
				Display.sync(60);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		Display.destroy( );
	}

	// main
	public static void main(String[] args) {
		Isajanyan_Program2 program2 = new Isajanyan_Program2();
		program2.start();
	}

}

// added color attribute to Polygon class
class Polygon extends java.awt.Polygon {
	private float[] color;

	private ArrayList<int[]>   vertices;
	private ArrayList<float[]> transitions;

	Polygon( ) {
		vertices    = new ArrayList<>( );
		transitions = new ArrayList<>( );
	}
	
    // draw vertices
	void draw( ) {
		glColor3f(getColorAt(0), getColorAt(1), getColorAt(2));
		glPointSize(10);
		
		glBegin(GL_LINE_LOOP);
		vertices.forEach(ints -> {
			glVertex2f(ints[0], ints[1]);
		});
		glEnd( );
	}

	// applies transformations and updates vertices
	void applyTransformations( ) {
		// call functions to apply transitions in order
		transitions.forEach(floats -> {
			switch(floats.length) {
				case 2: // t
					translate(floats);
					break;
				case 3: // r
					rotate(floats);
					break;
				case 4: // s
					scale(floats);
					break;
			}
		});
	}
	
	// init all edges
	void initAllEdges() {
		Iterator iterator = vertices.iterator();
		
		vertices.forEach();
		
		iterator.next();
	}
	
	void setColor(float f1, float f2, float f3) {
		color = new float[] { f1, f2, f3 };
	}

	private float getColorAt(int i) {
		return color[i];
	}

	void addVertex(int x, int y) {
		vertices.add(new int[] { x, y });
	}

	void addRotation(float angle, float pivotX, float pivotY) {
		transitions.add(new float[] { angle, pivotX, pivotY });
	}

	void addScaling(float factorX, float factorY, float pivotX, float pivotY) {
		transitions.add(new float[] { factorX, factorY, pivotX, pivotY });
	}

	void addTranslation(float x, float y) {
		transitions.add(new float[] { x, y });
	}
	
	// applies TRANSLATION
	private void translate(float[] floats) {
		float transX = floats[0], transY = floats[1];

		vertices.forEach(vertices -> {
			int origX = vertices[0], newX,
				origY = vertices[1], newY;

			// x' = x + tx
			newX = (int) (origX + transX);
			// y' = y + ty
			newY = (int) (origY + transY);

			vertices[0] = newX;
			vertices[1] = newY;
		});
	}

	// applies ROTATION
	private void rotate(float[] floats) {
		float angle = floats[0], pivotX = floats[1], pivotY = floats[2];

		vertices.forEach(vertices -> {
			int origX = vertices[0], newX,
				origY = vertices[1], newY;

			// x' = xr + (x − xr) cos θ − (y − yr) sin θ
			newX = (int) ((pivotX + (origX - pivotX) * Math.cos(Math.toRadians(angle))) - ((origY - pivotY) * Math.sin(Math.toRadians(angle))));
			// y' = yr + (x − xr) sin θ + (y − yr) cos θ
			newY = (int) ((pivotY + (origX - pivotX) * Math.sin(Math.toRadians(angle))) + ((origY + pivotY) * Math.cos(Math.toRadians(angle))));

			vertices[0] = newX;
			vertices[1] = newY;
		});
	}

	// applies SCALING
	private void scale(float[] floats) {
		float factorX = floats[0], factorY = floats[1], pivotX = floats[2], pivotY = floats[3];

		vertices.forEach(vertices -> {
			int origX = vertices[0], newX,
				origY = vertices[1], newY;

			// x' = x · sx + xf (1 − sx)
			newX = (int) (origX * factorX + pivotX * (1 - factorX));
			// y' = y · sy + yf (1 − sy)
			newY = (int) (origY * factorY + pivotY * (1 - factorY));

			vertices[0] = newX;
			vertices[1] = newY;
		});
	}
}