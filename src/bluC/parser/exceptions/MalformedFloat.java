/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC.parser.exceptions;

/**
 *
 * @author John Schneider
 */
public class MalformedFloat extends MalformedNumber
{
    public MalformedFloat(int offendingCharIndex)
    {
        super("Malformed floating-point number", offendingCharIndex);
    }
    
}
