package bluC.transpiler;

import bluC.parser.exceptions.MalformedNumber;
import bluC.parser.exceptions.MalformedFloat;
import bluC.parser.exceptions.MalformedInt;

/**
 *
 * @author John Schneider
 */
public class Token
{       
    public static final String EOF              = "___bluC_eof";
    public static final String NO_TEXT_CONTENT  = "___n/a";

    private final TokenInfo       tokenInfo;
    private final TokenFileInfo   fileInfo;
    private final String          package_;
    
    public Token(TokenInfo tokenInfo, TokenFileInfo fileInfo, String package_)
    {
        this.tokenInfo  = tokenInfo;
        this.fileInfo   = fileInfo;
        this.package_   = package_;
    }
    
    /**
     * Temporary constructor until packages are implemented, then all tokens
     *  will have an associated package.
     */
    public Token(TokenInfo tokenInfo, TokenFileInfo fileInfo)
    {
        this.tokenInfo  = tokenInfo;
        this.fileInfo   = fileInfo;
        this.package_   = Statement.Package.noPackage;
    }
    
    
    public String getTextContent()
    {
        return tokenInfo.getTextContent();
    }

    public void setTextContent(String textContent)
    {
        tokenInfo.setTextContent(textContent);
    }
    
    public String getFilepath()
    {
        return fileInfo.getFilePath();
    }

    public void setFilepath(String filepath)
    {
        fileInfo.setFilePath(filepath);
    }

    public int getLineIndex()
    {
        return fileInfo.getLineIndex();
    }

    public void setLineIndex(int lineIndex)
    {
        fileInfo.setLineIndex(lineIndex);
    }
    
    public boolean isNumber() throws MalformedNumber
    {
        boolean dotFound = false;
        char[] textContentArray = getTextContent().toCharArray();
        boolean startsWithDigit = Character.isDigit(textContentArray[0]);
        
        if (!startsWithDigit)
        {
            return false;
        }
        
        for (int i = 1; i < textContentArray.length; i++)
        {
            char c = textContentArray[i];
            
            if (c == '.')
            {
                if (i == textContentArray.length - 1)
                {
                    throw new MalformedFloat(i);
                }
                
                //if there are multiple dots in a malformed float, the lexer
                //  will have lexed the dots as separate tokens
                dotFound = true;
            }
            else if (!Character.isDigit(c))
            {
                if (i == textContentArray.length - 1)
                {
                    if (c != 'f')
                    {
                        if (dotFound)
                        {
                            throw new MalformedFloat(i);
                        }
                        else
                        {
                            throw new MalformedInt(i);
                        }
                    }
                }
                else
                {
                    if (dotFound)
                    {
                        throw new MalformedFloat(i);
                    }
                    else
                    {
                        throw new MalformedInt(i);
                    }
                }
            }
        }
        
        return true;
    }
    
    public boolean isString()
    {
        String textContent = getTextContent();
        boolean startsWithQuote = textContent.charAt(0) == '"';
        
        if (!startsWithQuote)
        {
            return false;
        }
        
        return textContent.charAt(textContent.length() - 1) == '"';
    }
    
    /**
     * Returns true if this token's text matches a plain-old-data 
     *  type specifier.
     */
    public boolean isReservedDataTypeBase() 
    {
        String textContent = getTextContent();
        
        return
            textContent.equals("char") || textContent.equals("int") ||
            textContent.equals("float") || textContent.equals("double") ||
            textContent.equals("bool");
    }
    
    public boolean isReservedWord()
    {
        String textContent = getTextContent();
        
        return
            //BluC terms
            textContent.equals("class") || textContent.equals("extends") ||
            textContent.equals("instanceOf") || textContent.equals("package") ||
                
            //c terms
            textContent.equals("auto") || textContent.equals("break") || 
            textContent.equals("case") || textContent.equals("const") || 
            textContent.equals("continue") || textContent.equals("default") || 
            textContent.equals("do") || textContent.equals("else") || 
            textContent.equals("enum") || textContent.equals("extern") ||
            textContent.equals("for") || textContent.equals("goto") || 
            textContent.equals("if") || textContent.equals("inline") ||
            textContent.equals("long") || textContent.equals("register") || 
            textContent.equals("restrict") || textContent.equals("return") || 
            textContent.equals("short") || textContent.equals("signed") || 
            textContent.equals("sizeof") || textContent.equals("static") || 
            textContent.equals("struct") || textContent.equals("switch") || 
            textContent.equals("typedef") || textContent.equals("union") || 
            textContent.equals("unsigned") || textContent.equals("void") || 
            textContent.equals("volatile") || textContent.equals("while") ||
            
            isReservedDataTypeBase()
            ;
    }
    
    public boolean isReservedLexeme()
    {
        char at0 = getTextContent().charAt(0);
        
        return at0 == '(' || at0 == ')' || at0 == '[' || at0 == ']' ||
            at0 == '{' || at0 == '}' || at0 == '=' || at0 == ';' ||
            at0 == '+' || at0 == '-' || at0 == '/' || at0 == '*' ||
            at0 == '%' || at0 == '#' || at0 == ',' || at0 == '<' ||
            at0 == '>' || at0 == '.' || at0 == '&' || at0 == '|';
    }
    
    public boolean isValidName()
    {
        char startingChar = getTextContent().charAt(0);
        
        return !(Character.isDigit(startingChar) || isReservedWord() || 
            isReservedLexeme());
    }
    
    @Override
    public String toString()
    {
        return "[textContent == \"" + getTextContent() + "\"]\n" +
               "[line == \"" + (getLineIndex() + 1) + "\"]\n" +
               "[file == \"" + getFilepath() + "\"]";
    }
}
