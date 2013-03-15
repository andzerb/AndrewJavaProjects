/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3dchaos;

import java.awt.Color;
import org.math.plot.*;
import java.util.Random;
import javax.swing.JFrame;
import java.util.ArrayList;
/**
 *
 * @author andzerb
 */
public class Main {
    public static int numChaos = 0;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double root3 = Math.sqrt(3); 
        numChaos++;
        boolean DidItWork = true;
        Plot3DPanel panel = new Plot3DPanel();
        panel = Gen3D(49999, new double[] {0.0, 8.0, 4.0, 4.0},new double[] {0.0, 0.0, root3*4, 4.0},new double[] {0.0, 0.0, 0.0, root3*4 }, 2, new double[] {0,0,0} );
        
        JFrame ChaosPanel = new JFrame("ChaAwesomeness" + numChaos);
        ChaosPanel.setSize(600, 600);
        ChaosPanel.setContentPane(panel);
        ChaosPanel.setVisible(true);
        
    }
    public static Plot3DPanel Gen3D(int Maxpoints, double[]vX, double[] vY, double vZ[], double denom, double[] seed){
        double xDif = 0;
        double yDif = 0;
        double zDif = 0;
        double[] point = {seed[0], seed[1], seed[2]};
        ArrayList <Double> X = new ArrayList<Double>();
        ArrayList <Double> Y = new ArrayList<Double>();
        ArrayList <Double> Z = new ArrayList<Double>();
        int index = 0;

        Plot3DPanel panel = new Plot3DPanel();
        panel.addScatterPlot("vertices", Color.blue, vX, vY, vZ);
        for (int i = 0; i < Maxpoints; i++){

                Random generator = new Random();
                int rand = generator.nextInt(4);
                xDif = vX[rand] - point[0]; 
                yDif = vY[rand] - point[1];
                zDif = vZ[rand] - point[2];
                //System.out.println("xDif: " + xDif + " Ydif: " + yDif + " MovementX: " + xDif/DENOM + " MovementY: " + yDif/DENOM );

                point[0] += xDif/denom;
                point[1] += yDif/denom;
                point[2] += zDif/denom;
                
                X.add(point[0]);
                Y.add(point[1]);
                Z.add(point[2]);

                index++;
                if(i % 500 == 0){
                    System.out.println("Generated " + i + " points. " + (Maxpoints - i) + " points to go.");


                }



        }
        double[] x = new double[X.size()];
        double[] y = new double[Y.size()];
        double[] z = new double[Z.size()];
        
        
        for(int i = 0; i < X.size(); i++){
            x[i] = X.get(i);
            y[i] = Y.get(i);
            z[i] = Z.get(i);
            
        }
        
        System.out.println("Generated " + Maxpoints + " points.");
        panel.addScatterPlot("Points",Color.RED, x, y, z);
        return(panel);
        
        
    }
}
