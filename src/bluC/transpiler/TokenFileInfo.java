package bluC.transpiler;

/**
 *
 * @author John Schneider
 */
public class TokenFileInfo
{
    public static final String  NO_FILEPATH = "n/a";
    public static final int     NO_LINE_INDEX = -1;
    
    private String  filePath;
    private int     lineIndex;
    
    public TokenFileInfo(String filePath, int lineIndex)
    {
        this.filePath   = filePath;
        this.lineIndex  = lineIndex;
    }

    public String getFilePath()
    {
        return filePath;
    }
    
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    
    public int getLineIndex()
    {
        return lineIndex;
    }
    
    public void setLineIndex(int lineIndex)
    {
        this.lineIndex = lineIndex;
    }
}
