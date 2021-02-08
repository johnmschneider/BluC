package bluC.transpiler.parser.exceptions;

import bluC.transpiler.Token;

/**
 *
 * @author John Schneider
 */
public class InvalidSizeModifier extends Exception
{
    private Token sizeMod1;
    private Token sizeMod2;
    private Token typeAttemptedToModify;
    
    public InvalidSizeModifier(Token sizeMod1, Token typeAttemptedToModify)
    {
        super("Attempted to use size modifier \"" + 
            sizeMod1.getTextContent() + "\" which is invalid for type \"" +
            typeAttemptedToModify.getTextContent() + "\"");
        
        this.sizeMod1 = sizeMod1;
        this.sizeMod2 = null;
        this.typeAttemptedToModify = typeAttemptedToModify;
    }
    
    public InvalidSizeModifier(Token sizeMod1, Token sizeMod2,
        Token typeAttemptedToModify)
    {
        super("Attempted to use size modifier \"" + 
            sizeMod1.getTextContent() + " " + sizeMod2.getTextContent() +
            "\" which is invalid for type \"" +
            typeAttemptedToModify.getTextContent() + "\"");
        
        this.sizeMod1 = sizeMod1;
        this.sizeMod2 = sizeMod2;
        this.typeAttemptedToModify = typeAttemptedToModify;
    }
    
    
    public Token getSizeMod1()
    {
        return sizeMod1;
    }
    
    public Token getSizeMod2()
    {
        return sizeMod2;
    }
}
