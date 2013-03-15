/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chaosgame;

/**
 *
 * @author andzerb
 */

import org.math.plot.*;
import javax.swing.*;
import java.util.Random;
import javax.swing.JOptionPane;
import java.awt.Color;

public class ChaosGame {
    
    
   


    
    
    
    
    
    
    public static void main(String[] args){
        
        double[] SEED = {0.0, 0.0};
        double DENOM = 2;
        int MAXPOINTS = 100000;
        int NUMVERTICES = 3;
        String delims = "[ ]+";
        double[] vX = new double[30];
        double[] vY = new double[30];
        vX[0] = 0;
        vX[1] = 1;
        vX[2] = 2;
        vY[0] = 0;
        vY[1] = 2;
        vY[2] = 0;
      
      
        JOptionPane.showMessageDialog(null, " Thanks for trying this program out! Contact me for source code.");

      
        String seedX = JOptionPane.showInputDialog(null, "Firstly, enter in the X coordinate and Y coordinate of the seed point. \n (E.G. '2 3')");
        
        String[] seedCoordinatesS = seedX.split(delims);
        
        SEED[0] = Double.parseDouble(seedCoordinatesS[0]);
        SEED[1] = Double.parseDouble(seedCoordinatesS[1]);
        
        
        
        String Sdenom = JOptionPane.showInputDialog(null, "Now, assuming you want the point to move some fraction 1/x to the chosen vertice, enter X.\n (E.G I want it to move 1/3 of the way to the vertice, so I enter 3.)");
        DENOM = Double.parseDouble(Sdenom);
        String Smax = JOptionPane.showInputDialog(null, "How many points do you want it to generate? \n(Try to do more than 1000, preferably 10000. Don't use commas.)");
        MAXPOINTS = Integer.parseInt(Smax);
        
        String SGoAhead = JOptionPane.showInputDialog(null, "Do you want to mess with vertices?");
        Boolean GoAhead = Boolean.parseBoolean(SGoAhead);
        if(GoAhead == true){
        
            String SNumVertices = JOptionPane.showInputDialog(null, "How many vertices?");
            NUMVERTICES = Integer.parseInt(SNumVertices);
            
            String SVertices = JOptionPane.showInputDialog(null, "Enter vertices like so: x1 y1 x2 y2 for as many vertices as you specified in the previous dialog. ");
            
            String[] coordinates =  SVertices.split(delims);
            
            for (int i = 0; i < NUMVERTICES; i++){
                System.out.println(coordinates[2*i] + " " + coordinates[(2*i)+1]);
                vX[i] = Double.parseDouble(coordinates[2*i]);
                vY[i] = Double.parseDouble(coordinates[(2*i)+1]);
            
            }
        
        
        }
        
        //JOptionPane.showMessageDialog(null, "As of now, the vertices are arranged in a triangle. I'm working to make you be able to choose points-- shouldn't take too long.");

        
        
       
        double[] X = new double[MAXPOINTS];
        double[] Y = new double[MAXPOINTS];
        double[] point = SEED;
        int index = 0;
        
       
        
        Plot2DPanel plot = new Plot2DPanel();
        
        plot.addScatterPlot("Vertices",Color.GREEN, vX, vY); // Created vertices to go from
        
        JFrame frame = new JFrame("2DPlot");
        frame.setSize(600, 600);
	frame.setContentPane(plot);
	frame.setVisible(true);
        
        double xDif, yDif = 0;
        int rand = 0;
        
        for (int i = 0; i < MAXPOINTS; i++){
            
            Random generator = new Random();
            rand = generator.nextInt(NUMVERTICES);
            xDif = vX[rand] - point[0]; 
            yDif = vY[rand] - point[1];
            
            //System.out.println("xDif: " + xDif + " Ydif: " + yDif + " MovementX: " + xDif/DENOM + " MovementY: " + yDif/DENOM );
            
            point[0] += xDif/DENOM;
            point[1] += yDif/DENOM;
            X[index] = point[0];
            Y[index] = point[1];
            
            index++;
            if(i % 100 == 0){
                System.out.println("Generated " + i + " points. " + (MAXPOINTS - i) + " points to go.");
                
                
            }
            
                        
            
        }
        System.out.println("Finished generating.");
        
        
        plot.addScatterPlot(null, X, Y);
	frame.setVisible(true);
        
    }
}