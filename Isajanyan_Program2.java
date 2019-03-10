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
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;


public class Isajanyan_Program2 {
	ArrayList<Polygon> polygonArray = new ArrayList<>(); // stores all polygons
	
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
        
        glOrtho(0, 640, 0, 480, 1, -1);
        
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
    
    // render
    private void render() {
        while(!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            try {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity( );
                
                // apply transitions & draw polygons
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
    
    Polygon() {
	    vertices    = new ArrayList<>();
	    transitions = new ArrayList<>();
    }
    
    void draw() {
        glPointSize(10);
        glColor3f(getColorAt(0), getColorAt(1), getColorAt(2));
        
        // apply transformations
        
        
        // draw vertices
        glBegin(GL_LINE_LOOP);
        vertices.forEach(ints -> {
            glVertex2f(ints[0], ints[1]);
            
            System.out.println("vertex x: " + ints[0]); // debug
            System.out.println("vertex y: " + ints[1]); // debug
        });
        glEnd();
    }
    
    // applies transformations and updates vertices
    private void applyTransitions( ) {
        // call functions to apply transitions in order
        transitions.forEach(floats -> {
            switch(floats.length) {
                case 2: // t
                    translate(floats);
                    System.out.println("applyTransitions t"); // debug
                    break;
                case 3: // r
                    rotate(floats);
                    System.out.println("applyTransitions r"); // debug
                    break;
                case 4: // s
                    scale(floats);
                    System.out.println("applyTransitions s"); // debug
                    break;
            }
        });
    }
    
    void setColor(float f1, float f2, float f3) {
        color = new float[] { f1, f2, f3 };
    }
    
    float getColorAt(int i) {
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
    
    // applies SCALING
    private void scale(float[] floats) {
    
    }
    
    // applies TRANSLATION
    private void translate(float[] floats) {
    
    }
    
    // applies ROTATION
    private void rotate(float[] floats) {
        float angle = floats[0], pivotX = floats[1], pivotY = floats[2];
        
        vertices.forEach(vertices -> {
            int  origX = vertices[0], origY = vertices[1],
                 newX, newY;
            
            System.out.println("orig vertices X: " + origX); // debug
            System.out.println("orig vertices Y: " + origY); // debug
            
            newX = (int)( (origX * Math.cos(angle)) - (origY * Math.sin(angle)) );
            newY = (int)( (origX * Math.sin(angle)) + (origY * Math.cos(angle)) );
            
            vertices[0] = newX;
            vertices[1] = newY;
            
            System.out.println("rotate vertices X: " + newX); // debug
            System.out.println("rotate vertices Y: " + newY); // debug
        });
    }
    
}
