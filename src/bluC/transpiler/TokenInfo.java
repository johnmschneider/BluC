/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bluC.transpiler;

/**
 *
 * @author John Schneider
 */
public class TokenInfo
{
    public static final String NO_TEXT_CONTENT = "<n/a>";
    
    private String  textContent;
    private boolean wasEmittedByCompiler;
    
    public TokenInfo(String textContent, boolean wasEmittedByCompiler)
    {
        this.textContent            = textContent;
        this.wasEmittedByCompiler = wasEmittedByCompiler;
    }
    
    
    public String getTextContent()
    {
        return textContent;
    }

    public void setTextContent(String textContent)
    {
        this.textContent = textContent;
    }

    public boolean getWasEmittedByCompiler()
    {
        return wasEmittedByCompiler;
    }

    public void setWasEmittedByCompiler(boolean wasEmittedByCompiler)
    {
        this.wasEmittedByCompiler = wasEmittedByCompiler;
    }
}
