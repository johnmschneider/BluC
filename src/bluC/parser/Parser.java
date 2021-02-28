package bluC.parser;

import bluC.Flags;
import java.util.ArrayList;
import bluC.Logger;
import bluC.transpiler.AstPrinter;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.parser.handlers.statement.StatementHandler;

/**
 *
 * @author John Schneider
 */
public class Parser
{
    private final ArrayList<Token>      lexedTokens;
    private final ArrayList<Statement>  abstractSyntaxTree;
    private int     curTokIndex;
    private Token   curToken;
    private String  curTokText;
    private Scope   currentScope;
    
    /**
     * Whether or not this is the topmost parser (the root of all other 
     *  parser classes). Currently only used if -time flag is set, to
     *  measure only the parse time as a whole, and not that of each parser.
     */
    private static  boolean isFirstParserInitialized = false;
    private final   boolean  thisInstanceIsFirstParser;
    
    private final StatementHandler handler;
    
    public Parser(ArrayList<Token> lexedTokens)
    {
        this.lexedTokens    = lexedTokens;
        abstractSyntaxTree  = new ArrayList<>();
        curTokIndex         = -1;
        currentScope        = new Scope(null, null);
        handler             = new StatementHandler(this);
        
        if (isFirstParserInitialized == false)
        {
            thisInstanceIsFirstParser   = true;
            isFirstParserInitialized    = true;
        }
        else
        {
            thisInstanceIsFirstParser = false;
        }
    }
    
    
    public ArrayList<Statement> parse()
    {
        if (doesParseTimeNeedTracked()) 
        {
            return parseWhileTrackingTime();
        }
        else
        {
            return parseWithoutTrackingTime();
        }
    }
    
    private boolean doesParseTimeNeedTracked()
    {
        return thisInstanceIsFirstParser && (Flags.get("time") != null);
    }
    
    private ArrayList<Statement> parseWhileTrackingTime()
    {
        long                    parserStartTime = System.currentTimeMillis();
        ArrayList<Statement>    statements;
        
        statements = parseWithoutTrackingTime();
        outputParseTime(parserStartTime);
        
        return statements;
    }
    
    private ArrayList<Statement> parseWithoutTrackingTime()
    {
        // as of right now each Parser object should only be used once, so if we
        //  try to parse an empty file it's most likely an error in the compiler
        //  code
        assert(!lexedTokens.isEmpty());
        
        boolean eof = atEOF();

        while (!eof)
        {
            try
            { 
                abstractSyntaxTree.add(handler.handleStatement(true));
            }
            catch (Exception ex)
            {
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Exception:\n" + ex.getMessage() + 
                    "\n\nDumping parse tree below:\n\n" + dumpAstToString());
            }
            catch (Error err)
            {
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Throwable.Error:\n" + err.getMessage() + 
                    "\n\nDumping parse tree below:\n\n" + dumpAstToString());
            }
            catch (Throwable t)
            {
                // as of the current java spec of the compiler, the compiler
                //  SHOULD only be throwing exceptions or errors (since no
                //  classes simply derive Throwable in this project)
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Throwable which is neither an Exception or " +
                    "Error:\n" + "class == " + t.getClass().getTypeName() +
                    "\n" + t.getMessage() + "\n\n" + dumpAstToString());
            }
            
            eof = atEOF();
        }
        
        return abstractSyntaxTree;
    }
    
    public String dumpAstToString()
    {
        AstPrinter  printer     = new AstPrinter();
        String      dumpedAst   = "Dumping parse tree below:\n\n" ;
        
        if (abstractSyntaxTree.isEmpty())
        {
            dumpedAst += "<no elements in parse tree>";
        }
        else
        {
            dumpedAst += printer.printToString(
                abstractSyntaxTree.get(0)) + "\n";

            for (int i = 1; i < abstractSyntaxTree.size(); i++)
            {
                dumpedAst += printer.printToString(abstractSyntaxTree.get(i)) +
                    "\n";
            }
        }
        
        return dumpedAst;
    }
    
    public void dumpAstToStdout()
    {
        System.out.println(dumpAstToString());
    }
    
    private void outputParseTime(long parserStartTime)
    {
        long parserEndTime  = System.currentTimeMillis();
        long elapsedTime    = parserEndTime - parserStartTime;
        
        System.out.println("Parsing done in " + elapsedTime + "ms.");
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
        if (curTokIndex < lexedTokens.size() - 1)
        {
            return lexedTokens.get(curTokIndex + 1);
        }
        else
        {
            return lexedTokens.get(lexedTokens.size() - 1);
        }
    }
    
    public Token peek(int howManyTokensAhead)
    {
        if (curTokIndex + howManyTokensAhead < lexedTokens.size() - 1)
        {
            return lexedTokens.get(curTokIndex + howManyTokensAhead);
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
        
        curToken    = lexedTokens.get(curTokIndex);
        curTokText  = curToken.getTextContent();
    }
    
    public void setToken(int tokenIndex)
    {
        if (tokenIndex > 0)
        {
            curToken    = lexedTokens.get(tokenIndex);
            curTokText  = curToken.getTextContent();
        }
        
        curTokIndex = tokenIndex;
    }
    
    public void prevToken()
    {
        if (curTokIndex > 0)
        {
            curTokIndex --;
        }
        
        curToken    = lexedTokens.get(curTokIndex);
        curTokText  = curToken.getTextContent();
    }
    
    public Token getCurToken()
    {
        return curToken;
    }
    
    public String getCurTokText()
    {
        return curTokText;
    }
    
    /**
     * Returns the index of the current token in the LEXED TOKENS ArrayList.
     */
    public int getCurTokIndex()
    {
        return curTokIndex;
    }
    
    /**
     * Returns the line index (line in sourcecode - 1) of the current token.
     */
    public long getCurTokLineIndex()
    {
        return curToken.getLineIndex();
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
    
    /**
     * For use in debugging.
     */
    @Override
    public String toString()
    {
        return "Parser current token:\n" + curToken.toString();
    }
}
