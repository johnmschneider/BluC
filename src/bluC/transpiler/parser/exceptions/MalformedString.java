/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC.transpiler.parser.exceptions;

/**
 *
 * @author John Schneider
 */
class MalformedString extends MalformedLiteralExpression
{
    public MalformedString(String msg, int offendingCharIndex)
    {
        super(msg, offendingCharIndex);
    }
}
