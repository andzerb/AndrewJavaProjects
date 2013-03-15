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
    public static int Factorssimple(int num){
        int numFactors = 0;
        System.out.println("factoring " + num);
        for(int i = 1; i <= Math.sqrt(num); i++){
          System.out.println(i);
          if(num % i == 0 && Math.sqrt(num)!= i){
              System.out.println("!");
              numFactors+=2;
            
          }else{ 
             if(Math.sqrt(num)== i){
                System.out.println(num + " square!");
                numFactors++; 
             }  
          }
        }
        return numFactors;
        
        
    }
    public static void main(String[] args) {
       
        for(int i = 1; i <= 102; i++){
           int tri = (i*(i+1))/2;
           int fact = Factorssimple(tri);
           System.out.println("factors: " + fact);
        }
    }
}
