package ChaosGui3;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author andzerb
 */
import java.util.Random;
import org.math.plot.*;
import java.util.ArrayList;

public class Generator {
    public static Plot2DPanel generate(int MAXPOINTS, double NUMER, double DENOM, double seedx, double seedy, double[] vX, double[] vY, int numVertices ){
        double xDif = 0;
        double yDif = 0;
       
        double[] point = {seedx, seedy};
        ArrayList<Double> X = new ArrayList<Double>();
        ArrayList<Double> Y = new ArrayList<Double>();
        
        //double[] X = new double[500000];
        //double[] Y = new double[500000];
        int index = 0;

        Plot2DPanel panel = new Plot2DPanel();
        for (int i = 0; i < MAXPOINTS; i++){

                Random generator = new Random();
                int rand = generator.nextInt(numVertices);
                xDif = vX[rand] - point[0]; 
                yDif = vY[rand] - point[1];

                //System.out.println("xDif: " + xDif + " Ydif: " + yDif + " MovementX: " + xDif/DENOM + " MovementY: " + yDif/DENOM );

                point[0] += xDif*NUMER/DENOM;
                point[1] += yDif*NUMER/DENOM;
                
                X.add(point[0]);
                Y.add(point[1]);

                index++;
                if(i % 500 == 0){
                    System.out.println("Generated " + i + " points. " + (MAXPOINTS - i) + " points to go.");


                }



        }
        double[] x = new double[X.size()];
        double[] y = new double[Y.size()];
        
        
        for(int i = 0; i < X.size(); i++){
            x[i] = X.get(i);
            y[i] = Y.get(i);
            
        }
        System.out.println("Generated " + MAXPOINTS + " points.");
        panel.addScatterPlot("Points", x, y);
        return(panel);
    
    
    
    }
}
