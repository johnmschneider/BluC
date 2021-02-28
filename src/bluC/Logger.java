/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC;

import bluC.transpiler.Token;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author John Schneider
 */
public class Logger
{
    private static boolean      hasLoggedError          = false;
    private static OutputStream lastStreamWroteTo       = System.out;
    
    /**
     * The number of milliseconds we wait for the current buffer to flush
     *  output, have the console read that, and then swap to the new
     *  OutputStream.
     */
    private static long         bufferSwapTimeMillis    = 200;
    
    public static void warn(Token errAt, String message)
    {
        System.out.println("[" + errAt.getFilepath() + ", line " + 
            (errAt.getLineIndex() + 1) + "] " + "Warning: On token \"" + 
            errAt.getTextContent() + "\"\n\t" + message);
        
        ensureBufferSynchronization(System.out);
    }
    
    public static void err(Token errAt, String message)
    {
        hasLoggedError = true;
        
        System.err.println("[" + errAt.getFilepath() + ", line " + 
            (errAt.getLineIndex() + 1) + "] " + "Error: On token \"" + 
            errAt.getTextContent() + "\"\n\t" + message);
        
        ensureBufferSynchronization(System.err);
    }
    
    public static boolean hasLoggedError()
    {
        return hasLoggedError;
    }
    
    private static void ensureBufferSynchronization(OutputStream currentBuffer)
    {
        if (currentBuffer != lastStreamWroteTo)
        {
            swapAndSynchronizeBuffers(currentBuffer);
            lastStreamWroteTo = currentBuffer;
        }
    }
    
    private static void swapAndSynchronizeBuffers(OutputStream newBuffer)
    {
        try
        {
            lastStreamWroteTo.flush();
            Thread.sleep(bufferSwapTimeMillis);
        } catch (IOException | InterruptedException ex)
        {
            System.err.println("[" + Logger.class.getTypeName() + "] Fatal " +
                "logging error resulted in failed buffer swap:\n");
            ex.printStackTrace();
        }
    }
}
