package mandelmusic;

import java.awt.Color;
import java.awt.FlowLayout;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.math.plot.*;
import java.util.*;
import javax.sound.sampled.*;

class TriplexNumbers {

    public double X;
    public double Y;
    public double Z;

    public TriplexNumbers(double x, double y, double z) {
        X = x;
        Y = y;
        Z = z;
    }

    public static TriplexNumbers TriplexPow(double pow, TriplexNumbers initial) {


        double r = Math.sqrt((initial.X * initial.X) + (initial.Y * initial.Y) + (initial.Z * initial.Z));
        double theta = Math.atan(initial.Y / initial.X);
        double phi = Math.atan(initial.Z / Math.sqrt(initial.X * initial.X + initial.Y * initial.Y));

        double rN = Math.pow(r, pow);
        double Wx = rN * Math.cos(pow * theta) * Math.cos(phi * pow);
        double Wy = Math.sin(theta * pow) * Math.sin(phi * pow);
        double Wz = Math.cos(theta * pow);

        TriplexNumbers W = new TriplexNumbers(rN * Wx, rN * Wy, rN * Wz);

        return W;

        //return new TriplexNumbers(1,2,3);

    }

    public static void TriplexPrint(TriplexNumbers printplease) {


        System.out.println(printplease.X + ", " + printplease.Y + ", " + printplease.Z);

    }

    static boolean isInFractal(TriplexNumbers Z, TriplexNumbers c, double pow) {

        double maxit = 2;
        double x = Z.X;
        double y = Z.Y;
        double z = Z.Z;
        double newx, newy, newz;

        double r, theta, phi;

        int i;

        for (i = 0, x = Z.X, y = Z.Y, z = Z.Z;; i++) {

            if (i >= 9) {
                //System.out.println("max its");
                break;

            }
            if (Math.sqrt(x * x + y * y + z * z) > 80) {

                //System.out.println("Max mag." + Math.sqrt(x*x+y*y+z*z));
                break;
            }


            double rpow;

            r = Math.sqrt(x * x + y * y + z * z);

            // convert to polar coordinates

            theta = Math.atan2(Math.sqrt(x * x + y * y), z);
            phi = Math.atan2(y, x);



            rpow = Math.pow(r, 8.0);

            newx = rpow * Math.sin(theta * pow) * Math.cos(phi * pow);
            newy = rpow * Math.sin(theta * pow) * Math.sin(phi * pow);
            newz = rpow * Math.cos(theta * pow);


            x = newx + c.X;
            y = newy + c.Y;
            z = newz + c.Z;


        }
        //System.out.println(i);
        return (i >= 4);
    }
}

public class MandelMusic {

    public static ArrayList<Double> xValue = new ArrayList<Double>();
    public static ArrayList<Double> yValue = new ArrayList<Double>();
    public static ArrayList<Double> zValue = new ArrayList<Double>();
    public static final double D = 8.0;
    public static float SAMPLE_RATE = 8000f;
    static final TriplexNumbers C = new TriplexNumbers(0, 0, 0);
    final int maxit = 80;
    final double mandpow = 8.0;

    double valInRange(double low, double high, int size, int off) {
        return low + ((high - low) / (double) size) * (double) off;
    }

    public static void sound(int hz, int msecs, double vol)
            throws LineUnavailableException {

        if (hz <= 0) {
            throw new IllegalArgumentException("Frequency <= 0 hz");
        }

        if (msecs <= 0) {
            throw new IllegalArgumentException("Duration <= 0 msecs");
        }

        if (vol > 1.0 || vol < 0.0) {
            throw new IllegalArgumentException("Volume out of range 0.0 - 1.0");
        }

        byte[] buf = new byte[(int) SAMPLE_RATE * msecs / 1000];

        for (int i = 0; i < buf.length; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[i] = (byte) (Math.sin(angle) * 127.0 * vol);
        }

        // shape the front and back 10ms of the wave form
        for (int i = 0; i < SAMPLE_RATE / 100.0 && i < buf.length / 2; i++) {
            buf[i] = (byte) (buf[i] * i / (SAMPLE_RATE / 100.0));
            buf[buf.length - 1 - i] =
                    (byte) (buf[buf.length - 1 - i] * i / (SAMPLE_RATE / 100.0));
        }

        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        sdl.write(buf, 0, buf.length);
        sdl.drain();
        sdl.close();
    }

    public static void threadSound(final int hz, final int msecs) throws InterruptedException {

        final boolean done = false;
        Thread a = (new Thread() {

            @Override
            public void run() {
                try {
                    sound((int) hz, 200, 1);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(MandelMusic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        a.setPriority(Thread.MIN_PRIORITY);
        a.start();
        Thread.sleep(110 + 10);
        a.interrupt();



    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException {
        
        /*(new Thread() {

            @Override
            public void run() {
                try {
                    sound(586, 100000, 1);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(MandelMusic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        (new Thread() {

            @Override
            public void run() {
                try {
                    sound(1172, 100000, 1);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(MandelMusic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();*/
        TriplexNumbers a = new TriplexNumbers(5.0, 2.0, 1.0);
        TriplexNumbers b = new TriplexNumbers(6.0, 3.0, -1.0);
        TriplexNumbers c = new TriplexNumbers(1.0, 1.0, 1.0);
        Plot3DPanel toPlot = new Plot3DPanel();

        double adder = 0.25;

        FileReader inputStream = null;
        FileWriter outputStream = null;
        int d;

      
        double x =0;
        double y=0;
        double z=0;
        for (double i = -1; i < 1; i += adder) {
            for (double j = -1; j < 1; j += adder) {
                for (double k = -1; k < 1; k += adder) {
                    if (TriplexNumbers.isInFractal(new TriplexNumbers(0, 0, 0), new TriplexNumbers(i, j, k), 8)) {
                        final TriplexNumbers printplease = new TriplexNumbers(i, j, k);
                        TriplexNumbers.TriplexPrint(printplease);
                        z = (printplease.Z);
                        x = (printplease.X);
                        y = (printplease.Y);
                        //threadSound((int)(z*100+230),140);
                        //threadSound((int)(x*100+230),120);
                        //threadSound((int)(y*100+230),110);
                        //threadSound((int)(=              z*100+230),100);

                    }
                    threadSound((int)(x*100+230),120);
                        threadSound((int)(y*100+230),110);
                        threadSound((int)(z*100+230),100);
                        threadSound(230,90);


                }
                
                

            }


        }


       
        //for (int i = 0; i < xValue.size(); i++) {
        // x[i] = xValue.get(i);
        //y[i] = yValue.get(i);
        //z[i] = zValue.get(i);
        //}
        //int addScatterPlot = toPlot.addScatterPlot("Mandelbulb", Color.CYAN, x, y, z);

        //JFrame frame = new JFrame("Mandelbulb");
        //frame.setSize(600, 600);
        //frame.add(toPlot);
        //frame.setVisible(true);



    }

    static void addTriplex(TriplexNumbers point) {

        xValue.add(point.X);
        yValue.add(point.Y);
        zValue.add(point.Z);


    }
}