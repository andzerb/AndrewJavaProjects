package mandelgui;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.ArrayList; 
import org.math.plot.*;


class TriplexNumbers{
    
    public double X;
    public double Y;
    public double Z;
        
    public TriplexNumbers(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }
    public static TriplexNumbers TriplexPow(double pow, TriplexNumbers initial){
        
        
        double r = Math.sqrt((initial.X*initial.X)+(initial.Y*initial.Y)+(initial.Z*initial.Z));
        double theta = Math.atan(initial.Y/initial.X);
        double phi = Math.atan(initial.Z/Math.sqrt(initial.X*initial.X + initial.Y*initial.Y));
        
        double rN = Math.pow(r, pow);
        double Wx = rN * Math.cos(pow * theta) * Math.cos(phi * pow);
        double Wy = Math.sin(theta * pow) * Math.sin(phi * pow);
        double Wz = Math.cos(theta * pow);
        
        TriplexNumbers W = new TriplexNumbers(rN * Wx,rN * Wy,rN * Wz);
        
        return W;
        
        //return new TriplexNumbers(1,2,3);
        
    }
    public static void TriplexPrint (TriplexNumbers printplease){
        
        
        System.out.println(printplease.X + ", " + printplease.Y + ", " + printplease.Z);
        
    }
    public static boolean isInFractal(TriplexNumbers Z, TriplexNumbers c, double pow) {
                
                double maxit = 2;
                double x =  Z.X;
                double y =  Z.Y;
                double z =  Z.Z;
                double newx, newy, newz;

                double r, theta, phi;
                
                int i;

                for (i = 0, x =  Z.X, y =  Z.Y, z =  Z.Z;;i++) {
                        
                        if(i >= 50000){
                            //System.out.println("max its");
                            break;
                            
                        }
                        if(Math.sqrt(x*x+y*y+z*z) > 80){
                            
                            //System.out.println("Max mag." + Math.sqrt(x*x+y*y+z*z));
                            break;
                        }
                        
                
                        double rpow;
                        
                        r = Math.sqrt(x * x + y * y + z * z);
                        
                        // convert to polar coordinates
                         
                        theta = Math.atan2(Math.sqrt(x*x + y*y), z);
                         phi = Math.atan2(y, x);
                        
                         
                         
                        rpow = Math.pow(r,8.0);
                        
                        newx = rpow * Math.sin(theta * pow) * Math.cos(phi*pow);
                        newy = rpow * Math.sin(theta*pow) * Math.sin(phi * pow);
                        newz = rpow * Math.cos(theta*pow);
                       

                        x = newx + c.X;
                        y = newy + c.Y;
                        z = newz + c.Z;
                        
                        
                }
                //System.out.println(i);
                return (i >= 4);
    }
    
       
    
}



class Mandelbulb {
    
    

    final int maxit=80;
    final double mandpow=8.0;

    double valInRange(double low, double high,  int size,  int off)
    {
      return low+((high-low)/(double)size)*(double)off;
    }

    public static Plot3DPanel mandelGen(double adder, TriplexNumbers Z, double pow ) {
            ArrayList <Double> xValue = new ArrayList <Double>();
            ArrayList <Double> yValue = new ArrayList <Double>();
            ArrayList <Double> zValue = new ArrayList <Double>();

		
            Plot3DPanel toPlot = new Plot3DPanel();




            for(double i = -2; i < 2; i+=adder){
                for(double j = -2; j < 2; j+=adder){
                    for(double k =-2; k < 2; k+=adder){
                        if(TriplexNumbers.isInFractal(Z,new TriplexNumbers(i, j, k),pow)){

                            TriplexNumbers point =new TriplexNumbers(i, j, k);
                            xValue.add(point.X);
                            yValue.add(point.Y);
                            zValue.add(point.Z);
                            //if(k>0.5)
                                //TriplexNumbers.TriplexPrint(new TriplexNumbers(i, j, k));
                                //System.out.println(k);
                             //TriplexNumbers.TriplexPrint(new TriplexNumbers(i,j,k+1));
                        }
                        //System.out.println(i);

                        //System.out.println((int)(i*100/1.31) + "%");


                    }

                }


            }
            System.out.println("math done.");
            double[] x = new double[xValue.size()];
            double[] y = new double[yValue.size()];
            double[] z = new double[zValue.size()];


            for(int i = 0; i < xValue.size(); i++){
                x[i] = xValue.get(i);
                y[i] = yValue.get(i);
                z[i] = zValue.get(i);
            }
            int addScatterPlot = toPlot.addScatterPlot("Mandelbulb",Color.CYAN, x, y, z);
            xValue.clear();
            yValue.clear();
            zValue.clear();
            return toPlot;
    }
    public static Plot3DPanel juliaGen(double adder, TriplexNumbers C, double pow ) {
            ArrayList <Double> xValue = new ArrayList <Double>();
            ArrayList <Double> yValue = new ArrayList <Double>();
            ArrayList <Double> zValue = new ArrayList <Double>();
		
            Plot3DPanel toPlot = new Plot3DPanel();




            for(double i = -2; i < 2; i+=adder){
                for(double j = -2; j < 2; j+=adder){
                    for(double k =-2; k < 2; k+=adder){
                        if(TriplexNumbers.isInFractal(new TriplexNumbers(i, j, k),C,pow)){
                            TriplexNumbers point =new TriplexNumbers(i, j, k);
                            xValue.add(point.X);
                            yValue.add(point.Y);
                            zValue.add(point.Z);
                            //if(k>0.5)
                                //TriplexNumbers.TriplexPrint(new TriplexNumbers(i, j, k));
                                //System.out.println(k);
                             TriplexNumbers.TriplexPrint(new TriplexNumbers(i,j,k+1));
                        }
                        //System.out.println(i);

                        //System.out.println((int)(i*100/1.31) + "%");


                    }

                }


            }
            double[] x = new double[xValue.size()];
            double[] y = new double[yValue.size()];
            double[] z = new double[yValue.size()];


            for(int i = 0; i < xValue.size(); i++){
                x[i] = xValue.get(i);
                y[i] = yValue.get(i);
                z[i] = zValue.get(i);
            }
            int addScatterPlot = toPlot.addScatterPlot("Mandelbulb",Color.RED, x, y, z);
            xValue.clear();
            yValue.clear();
            zValue.clear();
            return toPlot;
    }
    
}