package bluC.transpiler;

import java.util.ArrayList;

/**
 *
 * @author John Schneider
 */
public class Lexer
{
    //private IncludeHandler includeHandler;
    private final String filePath;
    private final ArrayList<String> fileContents;
    private ArrayList<Token> tokens;            
    private boolean inStringLiteral;
    private int currentCharacterIndex;
    private String currentLine;
    
    //The value is whitespace since whitespace is ignored by the lexer
    private static final char END_OF_LINE = ' ';
    
    public Lexer(String filePath, ArrayList<String> fileContents)
    {
        this.filePath = filePath;
        this.fileContents = fileContents;
        
        tokens = new ArrayList<>();
        //includeHandler = new IncludeHandler(this);
    }
    
    private String pushTokens(
        ArrayList<Token> tokens, String curTokVal, String newTokVal, int line)
    {
        /*if (curTokVal.equals(""))
        {
            new Exception("invalid arg").printStackTrace();
        }*/
        
        if (!curTokVal.equals("") && 
            !(curTokVal.length() == 1 && Character.isWhitespace(
                curTokVal.charAt(0))) &&
            !(inStringLiteral && curTokVal.equals("\\")))
        {
            Token prevToken = new Token(
                new TokenInfo(curTokVal, false),
                    
                new TokenFileInfo(filePath, line));
            
            tokens.add(prevToken);
            //includeHandler.tokenFormationListener(prevToken, tokens.size() - 1);
        }
        
        curTokVal = "";
        
        if (newTokVal != null)
        {
            curTokVal += newTokVal;
            
            if (!(inStringLiteral && curTokVal.equals("\\")))
            {
                Token curToken = new Token(
                    new TokenInfo(curTokVal, false),
                    
                    new TokenFileInfo(filePath, line));
                
                tokens.add(curToken);
                //includeHandler.tokenFormationListener(curToken, tokens.size() - 1);
            }
        }
        
        return "";
    }
    
    private String pushTokens(
        ArrayList<Token> tokens, String curTokVal, char curChar, int line)
    {
        return pushTokens(tokens, curTokVal, "" + curChar, line);
    }
    
    private boolean isIntegerSoFar(String numSoFar)
    {
        for (char c : numSoFar.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if current token is a new token, and if so,
     *  pushes both old token and new token to tokens ArrayList
     */
    private String checkForNewToken(
        ArrayList<Token> tokens, String curToken, char curChar, int line)
    {
        if (Character.isWhitespace(curChar))
        {
            curToken = pushTokens(tokens, curToken, null, line);
        }
        else if (curChar == '(')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == ')')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '[')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == ']')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '{')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '}')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '=')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == ';')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '+')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=' || next == '+')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '-')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=' || next == '-' || next == '>')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '/')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        } 
        else if (curChar == '%')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '*')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '#')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == ',')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == '<')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else if (next == '<')
                {
                    char nextNext = peek(2);
                    
                    if (nextNext == '=')
                    {
                        curToken = pushTokens(tokens, curToken, "" + curChar + 
                            next + nextNext, line);
                        currentCharacterIndex += 2;
                    }
                    else
                    {
                        curToken = pushTokens(tokens, curToken, "" + curChar + 
                            next, line);
                        currentCharacterIndex ++;
                    }
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '>')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '=')
                {
                    
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else if (next == '>')
                {
                    char nextNext = peek(2);
                    
                    if (nextNext == '=')
                    {
                        curToken = pushTokens(tokens, curToken, "" + curChar + 
                            next + nextNext, line);
                        currentCharacterIndex += 2;
                    }
                    else
                    {
                        curToken = pushTokens(tokens, curToken, "" + curChar + 
                            next, line);
                        currentCharacterIndex ++;
                    }
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '.')
        {
            if (!isIntegerSoFar(curToken))
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
            else
            {
                //now it's a float/double
                curToken += curChar;
            }
        }
        else if (curChar == '&')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '&')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '|')
        {
            if (currentCharacterIndex < currentLine.length() - 1)
            {
                char next = peek();

                if (next == '|')
                {
                    curToken = pushTokens(tokens, curToken, "" + curChar + next,
                        line);
                    currentCharacterIndex ++;
                }
                else 
                {
                    curToken = pushTokens(tokens, curToken, curChar, line);
                }
            }
            else 
            {
                curToken = pushTokens(tokens, curToken, curChar, line);
            }
        }
        else if (curChar == '?')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else if (curChar == ':')
        {
            curToken = pushTokens(tokens, curToken, curChar, line);
        }
        else
        {
            if (curChar != '"')
            {
                curToken += curChar;
            }
        }
        
        
        return curToken;
    }
    
    private char peek()
    {
        return peek(1);
    }
    
    private char peek(int howManyCharsAhead)
    {
        int index = currentCharacterIndex + howManyCharsAhead;
        return index < currentLine.length() ?
            currentLine.charAt(index) : 
            Lexer.END_OF_LINE;
    }
    
    public ArrayList<Token> lex()
    {
        String curToken;
        char curChar;
        char lastChar;
        
        for (int line = 0; line < fileContents.size(); line++)
        {
            currentLine = fileContents.get(line);
            curToken = "";
            inStringLiteral = false;
            
            //  separate tokens in this line
            for (currentCharacterIndex = 0; ; 
                currentCharacterIndex++)
            {
                if (currentCharacterIndex >= currentLine.length())
                {
                    break;
                }
                
                curChar = currentLine.charAt(currentCharacterIndex);
                lastChar = (currentCharacterIndex > 0 
                    ? currentLine.charAt(currentCharacterIndex - 1)
                    : curChar);
                
                if (lastChar != '\\' && curChar == '"')
                {
                    curToken += curChar;
                    
                    if (inStringLiteral)
                    {
                        curToken = pushTokens(tokens, curToken, null, line);
                    }
                    
                    inStringLiteral = !inStringLiteral;
                }
                else if (!inStringLiteral)
                {
                    curToken = checkForNewToken(tokens, curToken, curChar, 
                        line);
                }
                else
                {
                    curToken += curChar;
                }
            } // end for (int curCharIndex = 0...)
            
            if (!curToken.equals(""))
            {
                tokens.add(new Token(
                    new TokenInfo(curToken, false), 
                    
                    new TokenFileInfo(filePath, line)));
            }
            //curToken set to "" in the next loop
        } //end for (int line...)
        
        tokens.add(new Token(
            new TokenInfo(Token.EOF, false),
            
            new TokenFileInfo(filePath, fileContents.size() - 1)));
        
        return tokens;
    }
    
    public void insertToken(int index, Token token)
    {
        tokens.add(index, token);
    }
    
    public void addToken(Token token)
    {
        if (tokens.isEmpty())
        {
            insertToken(0, token);
        }
        else
        {
            insertToken(tokens.size(), token);
        }
    }
    
    public void removeToken(int index)
    {
        tokens.remove(index);
    }
    
    public void removeTokens(int startIndex, int endIndex)
    {
        for (int i = startIndex; i <= endIndex; i++)
        {
            //startIndex not i because elements are
            //  shifted to the left on remove
            removeToken(startIndex);
        }
    }
    
    public void debug_writeOutput()
    {
        ArrayList<String> output = new ArrayList<>();
        String curLine = tokens.get(0).getTextContent();
        
        for (int i = 1; i < tokens.size(); i++)
        {
            Token t = tokens.get(i);
            
            if (t.getTextContent().equals(";"))
            {
                curLine += t.getTextContent();
                output.add(curLine);
                curLine = "";
            }
            else
            {
                if (tokens.get(i - 1).getTextContent().equals(";"))
                {
                    curLine += t.getTextContent();
                }
                else 
                {
                    curLine += " " + t.getTextContent();
                }
            }
        }
        
        output.add(curLine);
        bluC.BluC.writeFile("src/lexerContents.txt", output);
    }
}