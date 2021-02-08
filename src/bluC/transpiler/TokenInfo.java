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
    private String textContent;
    private boolean wasEmittedByTranspiler;
    
    public TokenInfo(String textContent, boolean wasEmittedByTranspiler)
    {
        this.textContent = textContent;
        this.wasEmittedByTranspiler = wasEmittedByTranspiler;
    }
    
    
    public String getTextContent()
    {
        return textContent;
    }

    public void setTextContent(String textContent)
    {
        this.textContent = textContent;
    }

    public boolean isWasEmittedByTranspiler()
    {
        return wasEmittedByTranspiler;
    }

    public void setWasEmittedByTranspiler(boolean wasEmittedByTranspiler)
    {
        this.wasEmittedByTranspiler = wasEmittedByTranspiler;
    }
}
