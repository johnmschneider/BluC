/*
 * Copyright 2021 John Schneider.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bluC.parser;

import bluC.Flags;
import java.util.ArrayList;
import bluC.Logger;
import bluC.Utils;
import bluC.transpiler.AstPrinter;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.Token;
import bluC.parser.handlers.statement.StatementHandler;
import bluC.parser.handlers.statement.StatementHandler.JustParseExprResult;
import bluC.transpiler.statements.blocks.ClassDef;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Parser
{
    public static final boolean BLOCK_DID_END       = true;
    public static final boolean BLOCK_DID_NOT_END   = false;
    public static final boolean STATEMENT_DID_END          = true;
    public static final boolean STATEMENT_DID_NOT_END      = true;
    
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
    private final   boolean thisInstanceIsFirstParser;
    
    private final StatementHandler handler;
    
    public Parser(ArrayList<Token> lexedTokens)
    {
        this.lexedTokens    = lexedTokens;
        abstractSyntaxTree  = new ArrayList<>();
        curTokIndex         = -1;
        currentScope        = new Scope(Scope.NO_PARENT, Scope.NO_SCOPE_TYPE);
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
    
    public Parser(ArrayList<Token> lexedTokens, Scope parentScope)
    {
        this(lexedTokens);
        currentScope = parentScope;
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
                String stackTrace = Arrays.deepToString(ex.getStackTrace()).
                        replace(", ", "\n");
                
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Exception whose type is " + ex.getClass().
                    getTypeName() + ":\n" + stackTrace + "\n\nDumping parse " +
                    "tree below:\n\n" + dumpAstToString());
            }
            catch (Error err)
            {
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Throwable.Error whose type is " + err.
                    getClass().getTypeName() + ":\n" + err.getMessage() + 
                    "\n\nDumping parse tree below:\n\n" + dumpAstToString());
            }
            catch (Throwable t)
            {
                // as of the current java spec of the compiler, the compiler
                //  SHOULD only be throwing exceptions or errors (since no
                //  classes simply derive Throwable in this project)
                Logger.err(curToken, "Fatal parse error resulted in uncaught " +
                    "java.lang.Throwable which is neither an Exception or " +
                    "Error whose type is " + t.getClass().getTypeName() + 
                    ":\n" + "class == " + t.getClass().getTypeName() +
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
    
    public boolean curTextMatches(String... textToMatch)
    {
        String curText = getCurTokText();
        
        for (String s : textToMatch)
        {
            // use Objects.equals so it's null safe
            if (Objects.equals(curText, s))
            {
                return true;
            }
        }
        
        return false;
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
    
    /**
     * Attempts to advance to the end of the statement.
     * 
     * Returns true if an end was actually found, false otherwise.
     */
    public boolean gotoEndOfStatement(TokenListener listener)
    {
        boolean didEnd = STATEMENT_DID_NOT_END;
        
        while (!atEOF())
        {
            listener.onNextToken(getCurToken());
            
            if (peekMatches(";"))
            {
                didEnd = STATEMENT_DID_END;
                break;
            }
            
            nextToken();
        }
        
        return didEnd;
    }
    
    public boolean gotoEndOfStatement()
    {
        return gotoEndOfStatement(new TokenListener()
        {
            @Override
            public void onNextToken(Token nextToken)
            {
                //no listener needed
            }

            @Override
            public boolean continueGoto(Token nextToken)
            {
                return true;
            }
        });
    }
    
    public boolean gotoEndOfBlock()
    {
        int openBraceCount = 1;
        
        while (true)
        {
            if (peekMatches("}"))
            {
                openBraceCount--;
                
                if (openBraceCount == 0)
                {
                    // set parser's current token to the closing brace
                    //  of the block "}"
                    nextToken();
                    break;
                }
            }
            else if (peekMatches("{"))
            {
                openBraceCount++;
            }
            
            nextToken();
            if (atEOF())
            {
                return BLOCK_DID_NOT_END;
            }
        }
        
        return BLOCK_DID_END;
    }
    
    public boolean isInAClass()
    {
        Scope currentSearchScope = currentScope;
        
        while (currentSearchScope != null)
        {
            if (currentSearchScope.getScopeType() instanceof ClassDef)
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
            
            /**
             * Upon further inspection of Java exceptions it appears to be
             *  calling terminate/some sort of exit method when the exception
             *  is either thrown or printStackTrace'd
             */
            // TODO - change this from an exception to some other error recovery
            //  mechanism
            
            //this *should* keep parsing (for further errors) but will stop the 
            //  compile to C IR.
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
     * For use by a debugger.
     */
    @Override
    public String toString()
    {
        return "Parser current token:\n" + curToken.toString() + 
            "\n\nParser current scope:\n" + currentScope.toString();
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Parser)
        {
            Parser otherParser = (Parser) other;
            
            return
                abstractSyntaxTree.equals(otherParser.abstractSyntaxTree) &&
                getCurTokIndex() == otherParser.getCurTokIndex() && 
                curTokTextEquals(otherParser) &&
                curTokEquals(otherParser) &&
                curScopeEquals(otherParser) && 
                lexedTokEquals(otherParser);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.lexedTokens);
        hash = 13 * hash + Objects.hashCode(this.abstractSyntaxTree);
        hash = 13 * hash + this.curTokIndex;
        hash = 13 * hash + Objects.hashCode(this.curToken);
        hash = 13 * hash + Objects.hashCode(this.curTokText);
        hash = 13 * hash + Objects.hashCode(this.currentScope);
        return hash;
    }
    
    private boolean curTokTextEquals(Parser other)
    {
        String curText = getCurTokText();
        
        return Utils.<Parser, String> nullSafeEquals(
            this, curText, other, other.getCurTokText());
    }
    
    private boolean curTokEquals(Parser other)
    {
        Token curTok = getCurToken();
        
        return Utils.<Parser, Token> nullSafeEquals(
            this, curTok, other, other.getCurToken());
    }
    
    private boolean curScopeEquals(Parser other)
    {
        Scope curScope = getCurrentScope();
        
        return Utils.<Parser, Scope> nullSafeEquals(
            this, curScope, other, other.getCurrentScope());
    }
    
    private boolean lexedTokEquals(Parser other)
    {
        return Utils.<Parser, ArrayList<Token>> nullSafeEquals(
            this, lexedTokens, other, other.lexedTokens);
    }
    
    /**
     * Parses only an expression. On error, returns appropriate error code.
     */
    public JustParseExprResult justParseExpression()
    {
        return handler.justParseExpression();
    }
}