package bluC.transpiler;

/**
 * 
 * @author John Schneider
 */
public class TokenBuilder
{
    private String  fileName;
    private int     lineIndex;
    private String  textContent;
    private boolean wasEmittedByCompiler;
    
    public TokenBuilder()
    {
        fileName                = TokenFileInfo.NO_FILEPATH;
        lineIndex               = TokenFileInfo.NO_LINE_INDEX;
        textContent             = TokenInfo.NO_TEXT_CONTENT;
        wasEmittedByCompiler    = false;
    }
    
    
    public String getFileName()
    {
        return fileName;
    }

    public TokenBuilder setFileName(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    public int getLineIndex()
    {
        return lineIndex;
    }

    public TokenBuilder setLineIndex(int lineIndex)
    {
        this.lineIndex = lineIndex;
        return this;
    }
    
    public TokenBuilder setWasEmittedByCompiler(boolean wasEmittedByCompiler)
    {
        this.wasEmittedByCompiler = wasEmittedByCompiler;
        return this;
    }
    
    public String getTextContent()
    {
        return textContent;
    }

    public TokenBuilder setTextContent(String textContent)
    {
        this.textContent = textContent;
        return this;
    }
    
    public Token build()
    {
        TokenFileInfo fileInfo;
        TokenInfo     tokenInfo;
        
        fileInfo    = new TokenFileInfo(fileName, lineIndex);
        tokenInfo   = new TokenInfo(textContent, wasEmittedByCompiler);
        
        return new Token(tokenInfo, fileInfo);
    }
    
}
