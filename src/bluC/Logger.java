/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC;

import java.util.ArrayList;
import bluC.transpiler.Token;

/**
 *
 * @author John Schneider
 */
public class Logger
{
    private static boolean hasLoggedError = false;
            
    public static void warn(Token errAt, String message)
    {
        System.out.println("[" + errAt.getFilepath() + ", line " + 
            (errAt.getLineIndex() + 1) + "] " + "Warning: On token \"" + 
            errAt.getTextContent() + "\"\n\t" + message);
        System.out.flush();
    }
    
    public static void err(Token errAt, String message)
    {
        hasLoggedError = true;
        
        System.err.println("[" + errAt.getFilepath() + ", line " + 
            (errAt.getLineIndex() + 1) + "] " + "Error: On token \"" + 
            errAt.getTextContent() + "\"\n\t" + message);
        System.err.flush();
    }
    
    public static boolean hasLoggedError()
    {
        return hasLoggedError;
    }
    
}
