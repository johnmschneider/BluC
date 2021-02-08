/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC;

/**
 *
 * @author John Schneider
 */
public class Debug
{
    private static final boolean DEBUG_ON = false;
    
    public static class Out
    {
        public void println(String... args)
        {
            if (DEBUG_ON)
            {
                String output = "";
                
                for (String s : args)
                {
                    output += s;
                }
                
                System.out.println(output);
            }
        }
    }
    
    public static class Err
    {
        public void println(String... args)
        {
            if (DEBUG_ON)
            {
                String output = "";
                
                for (String s : args)
                {
                    output += s;
                }
                
                System.out.println(output);
            }
        }
    }
    
    public static final Out out = new Out();
    public static final Err err = new Err();
}
