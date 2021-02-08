package bluC.transpiler.parser.handlers.statement;

import bluC.Logger;
import bluC.transpiler.Expression;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Statement.If.Else;
import bluC.transpiler.Statement.If.ElseIf;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.transpiler.parser.Parser;
import bluC.transpiler.parser.handlers.expression.ExpressionHandler;

/**
 *
 * @author John Schneider
 */
public class IfHandler
{
    private final Parser parser;
    private final BlockHandler blockHandler;
    private final ExpressionHandler expressionHandler;
    
    public IfHandler(Parser parser, BlockHandler blockHandler,
        ExpressionHandler expressionHandler)
    {
        this.parser = parser;
        this.blockHandler = blockHandler;
        this.expressionHandler = expressionHandler;
    }
    
    
    public Statement handleIfStatement(Token potentialIf)
    {
        Token openParen;
            
        parser.nextToken();
        openParen = parser.peek();

        if (openParen.getTextContent().equals("("))
        {
            return handleOpenParenthesisAndCondition(openParen, 
                potentialIf);
        }
        else
        {
            return handleInvalidIfCondition(openParen,
                potentialIf);
        }
    }
    
    private Statement handleOpenParenthesisAndCondition(Token openParen,
        Token potentialIf)
    {
        Statement.If statement = newIfWithCondition();
        Token closeParen = parser.peek();
        
        if (closeParen.getTextContent().equals(")"))
        {
            if (doesIfOrElseIfHaveEmptyBody())
            {
                //consume )
                parser.nextToken();
                
                //consume {
                parser.nextToken();
                
                //} is consumed by handleStatement
            }
            else
            {
                handleBody(statement, getOpenBrace());
            }
        }
        else
        {
            Logger.err(closeParen, "Expected \")\" to close " +
                "condition of if statement at line " + 
                (potentialIf.getLineIndex() + 1));
        }
        
        handleElseIfs(statement);
        
        return statement;
    }
    
    private boolean doesIfOrElseIfHaveEmptyBody()
    {
        return parser.peekMatches(3, "}");
    }
    
    private Statement.If newIfWithCondition()
    {
        Expression condition;
        
        //consume "("        
        parser.nextToken();
        condition = expressionHandler.handleExpression();
        
        return new Statement.If(condition);
    }
    
    private void handleBody(Statement.Block statement, Token openBrace)
    {
        //consume ")" and set curToken == "{"
        parser.nextToken();

        parser.pushScope(new Scope(parser.getCurrentScope(), statement));
        blockHandler.addStatementsToBlock(openBrace, statement);
        parser.popScope(parser.peek());
    }
    
    private Token getOpenBrace()
    {
        Token openBrace;
            
        parser.nextToken();
        openBrace = parser.peek();
        
        return openBrace;
    }
    
    private Statement handleInvalidIfCondition(Token openParen, 
        Token potentialIf)
    {
        Token fakeOpenParen;
        
        Logger.err(openParen, "Expected \"(\" to open condition of " + 
                "if statement at line " + (potentialIf.getLineIndex() + 1));
        
        fakeOpenParen = new Token(
            new TokenInfo("(", true),
                
            new TokenFileInfo(openParen.getFilepath(), 
                openParen.getLineIndex()));
        
        parser.addToken(fakeOpenParen, parser.indexOf(openParen));
        
        return handleOpenParenthesisAndCondition(fakeOpenParen, 
            potentialIf);
    }
    
    private void handleElseIfs(Statement.If statement)
    {
        Token else_;
        
        while (!parser.atEOF())
        {
            parser.nextToken();
            else_ = parser.peek();

            if (else_.getTextContent().equals("else"))
            {
                handleElseIfCheck(statement, else_);
            }
            else
            {
                parser.prevToken();
                break;
            }
        }
    }
    
    private void handleElseIfCheck(Statement.If statement, Token else_)
    {
        Token ifOrOpenBrace;
        String ifOrOpenBraceText;

        parser.nextToken();
        ifOrOpenBrace = parser.peek();
        ifOrOpenBraceText = ifOrOpenBrace.getTextContent();

        if (ifOrOpenBraceText.equals("if"))
        {
            parser.nextToken();
            handleElseIf(statement, ifOrOpenBrace);
        }
        else if (ifOrOpenBraceText.equals("{"))
        {
            handleElse(statement, ifOrOpenBrace);
        }
        else
        {
            Logger.err(ifOrOpenBrace, "Expected \"if\" or \"{\" to " +
                "follow \"" + else_.getTextContent() + " on line " + 
                 (else_.getLineIndex() + 1));
        }
    }
    
    private void handleElseIf(Statement.If statement, Token ifOfTheElse)
    {
        Token openParen = parser.peek();
        String openParenText = openParen.getTextContent();
        
        if (openParenText.equals("("))
        {
            handleValidatedElseIf(statement, ifOfTheElse);
        }
        else
        {
            handleElseIfNoOpenParenthesis(statement, ifOfTheElse, openParen);
        }
    }
    
    private void handleValidatedElseIf(Statement.If statement, 
        Token ifOfTheElse)
    {
        Expression condition;
        Token closeParen;
        String closeParenText;
        
        parser.nextToken();
        condition = expressionHandler.handleExpression();
        
        closeParen = parser.peek();
        closeParenText = closeParen.getTextContent();
        
        if (closeParenText.equals(")"))
        {
            ElseIf elseIf = new ElseIf(condition);
            
            if (doesIfOrElseIfHaveEmptyBody())
            {
                //consume )
                parser.nextToken();
                
                //consume {
                parser.nextToken();
                
                //} is consumed by handleStatement
            }
            else
            {
                handleBody(elseIf, getOpenBrace());
            }
            
            statement.addElseIf(elseIf);
        }
        else
        {
            Logger.err(closeParen, "Expected \")\" to close " +
                "condition of else-if statement at line " + 
                (ifOfTheElse.getLineIndex() + 1));
        }
    }
    
    
    private void handleElseIfNoOpenParenthesis(Statement.If statement,
        Token ifOfTheElse, Token expectedOpenParen)
    {
        Token fakeOpenParen;
        
        Logger.err(expectedOpenParen, "Expected \"(\" to open condition of " + 
            "else-if statement at line " + 
            (ifOfTheElse.getLineIndex() + 1));
        
        fakeOpenParen = new Token(
            new TokenInfo("(", true), 
                
            new TokenFileInfo(expectedOpenParen.getFilepath(), 
                expectedOpenParen.getLineIndex()));
        
        parser.addToken(fakeOpenParen, parser.indexOf(expectedOpenParen));
        
        handleValidatedElseIf(statement, ifOfTheElse);
    }
    
    private void handleElse(Statement.If statement, Token openBrace)
    {
        Else elseStatement = new Else();
        
        if (doesElseHaveEmptyBody())
        {
            //consume {
            parser.nextToken();
            
            //} is consumed by handleStatement
        }
        else
        {
            handleBody(elseStatement, openBrace);
        }
        
        statement.setElse(elseStatement);
    }
    
    private boolean doesElseHaveEmptyBody()
    {
        return parser.peekMatches(2, "}");
    }
}
