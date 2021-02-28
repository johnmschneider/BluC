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
class MalformedLiteralExpression extends Exception
{
    private final int offendingCharIndex;
    
    public MalformedLiteralExpression(String msg, int offendingCharIndex)
    {
        super(msg);
        
        this.offendingCharIndex = offendingCharIndex;
    }
    
    public int getOffendingCharIndex()
    {
        return this.offendingCharIndex;
    }
}
