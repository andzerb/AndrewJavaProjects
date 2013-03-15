/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package euler4;

/**
 *
 * @author andzerb
 */
public class Euler4 {

    /**
     * @param args the command line arguments
     */
    public static String reverse(String s) {
        return new StringBuffer(s).reverse().toString();
    }
    public static void main(String[] args) {
        int largest = 0;
        for(int i = 100; i <=999; i++ ){
          for(int j = 100; j <=999; j++ ){
            int palindrome = i * j;
            
            String palinString = Integer.toString(palindrome);
            //System.out.println(palinString + " " + reverse(palinString));
            if (reverse(palinString).equals(palinString)){
                
               if (palindrome > largest){
                   
                   largest = palindrome;
                   
               }
                
            }
            
            
          }  
            
            
        }
        System.out.println("Largest:" + largest);
    }
}
