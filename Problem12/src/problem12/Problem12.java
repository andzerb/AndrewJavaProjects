/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package problem12;

/**
 *
 * @author andzerb
 */
public class Problem12 {

    /**
     * @param args the command line arguments
     */
    public static long Factorssimple(long num) {
        long numFactors = 0;
        System.out.println("factoring " + num);
        for (long i = 1; i <= num; i++) {
            if (num % i == 0) {
               numFactors++;
            }
        }
         return numFactors;
    }
    
   
    public static void main(String[] args) {
        long fact = 0;
        for(long i = 1; fact <=500; i++){
           long tri = (i*(i+1))/2;
            fact = Factorssimple(tri);
           System.out.println("factors: " + fact);
          
        }
        System.out.println("greater than 500! " + fact);
    }
}

