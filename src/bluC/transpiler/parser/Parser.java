package bluC.transpiler.parser;

import java.util.ArrayList;
import bluC.Logger;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.transpiler.parser.handlers.statement.StatementHandler;

/**
 *
 * @author John Schneider
 */
public class Parser
{
    private ArrayList<Token> lexedTokens;
    private int curTokIndex;
    private Token curToken;
    private String curTokText;
    private Scope currentScope;
    private final StatementHandler handler;
    
    public Parser(ArrayList<Token> lexedTokens)
    {
        this.lexedTokens = lexedTokens;
        curTokIndex = -1;
        currentScope = new Scope(null, null);
        handler = new StatementHandler(this);
    }
    
    
    public ArrayList<Statement> parse()
    {
        ArrayList<Statement> statements = new ArrayList<>();
        
        boolean eof = atEOF();
        while (!eof)
        {
            statements.add(handler.handleStatement(true));
            eof = atEOF();
        }
        
        return statements;
    }
    
    public boolean peekMatches(String... textToMatch)
    {
        Token t = peek();

        for (String s : textToMatch)
        {
            if (t.getTextContent().equals(s))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean peekMatches(int howManyTokensAhead, String... textToMatch)
    {
        Token t = peek(howManyTokensAhead);

        for (String s : textToMatch)
        {
            if (t.getTextContent().equals(s))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public Token peek()
    {
        return curTokIndex < lexedTokens.size() - 1 ? 
            lexedTokens.get(curTokIndex + 1) : 
            lexedTokens.get(lexedTokens.size() - 1);
    }
    
    public Token peek(int howManyTokensAhead)
    {
        if (curTokIndex + howManyTokensAhead < lexedTokens.size() - 1)
        {
            if (curTokIndex > -1)
            {
                return lexedTokens.get(curTokIndex + howManyTokensAhead);
            }
            else
            {
                return lexedTokens.get(0);
            }
        }
        else
        {
            return lexedTokens.get(lexedTokens.size() - 1);
        }
    }
    
    public boolean atEOF()
    {
        return peekMatches(Token.EOF);
    }
    
    public void nextToken()
    {
        if (curTokIndex < lexedTokens.size() - 1)
        {
            curTokIndex ++;
        }
        
        curToken = lexedTokens.get(curTokIndex);
        curTokText = curToken.getTextContent();
    }
    
    public void setToken(int tokenIndex)
    {
        if (tokenIndex > 0)
        {
            curToken = lexedTokens.get(tokenIndex);
            curTokText = curToken.getTextContent();
        }
        
        curTokIndex = tokenIndex;
    }
    
    public void prevToken()
    {
        if (curTokIndex > 0)
        {
            curTokIndex --;
        }
        
        curToken = lexedTokens.get(curTokIndex);
        curTokText = curToken.getTextContent();
    }
    
    public Token getCurToken()
    {
        return curToken;
    }
    
    public String getCurTokText()
    {
        return curTokText;
    }
    
    public int getCurTokIndex()
    {
        return curTokIndex;
    }
    
    public void addToken(Token token, int index)
    {
        if (index < lexedTokens.size())
        {
            lexedTokens.add(index, token);
        }
        else
        {
            lexedTokens.add(token);
        }
    }
    
    public void gotoEndOfStatement()
    {
        while (!peekMatches(";") && !atEOF())
        {
            nextToken();
        }
    }
    
    public boolean isInAClass()
    {
        Scope currentSearchScope = currentScope;
        
        while (currentSearchScope != null)
        {
            if (currentSearchScope.getScopeType() instanceof Statement.ClassDef)
            {
                return true;
            }
            
            currentSearchScope = currentSearchScope.getParent();
        }
        
        return false;
    }
    
    public Scope getCurrentScope()
    {
        return currentScope;
    }
    
    public void pushScope(Scope newScope)
    {
        currentScope = newScope;
    }
    
    public void popScope(Token causeOfPop)
    {
        Scope parent = currentScope.getParent();
        
        if (parent != null)
        {
            currentScope = parent;
        }
        else
        {
            //this should never happen so it is an error if it does
            
            //this will keep parsing (for further errors) but will not compile
            Logger.err(causeOfPop, "Fatal parse error");
            
            //printStackTrace an exception so we get the stack frame in question
            new Exception("Compiler error: popScope called on global " +
                "scope. Previous scope type == " + 
                currentScope.getScopeType()).printStackTrace();
        }
    }
    
    public int indexOf(Token indexOfThis)
    {
        return lexedTokens.indexOf(indexOfThis);
    }
}
