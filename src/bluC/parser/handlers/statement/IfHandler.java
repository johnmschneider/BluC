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

package bluC.parser.handlers.statement;

import bluC.Logger;
import bluC.transpiler.Expression;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.blocks.If.Else;
import bluC.transpiler.statements.blocks.If.ElseIf;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.parser.Parser;
import bluC.parser.handlers.expression.ExpressionHandler;
import bluC.transpiler.statements.blocks.If;
import bluC.transpiler.statements.blocks.Block;

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
        If statement = newIfWithCondition(openParen);
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
    
    private If newIfWithCondition(Token openParen)
    {
        Expression condition;
        
        //consume "("        
        parser.nextToken();
        condition = expressionHandler.handleExpression();
        
        return new If(condition, openParen.getLineIndex());
    }
    
    private void handleBody(Block statement, Token openBrace)
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
    
    private void handleElseIfs(If statement)
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
    
    private void handleElseIfCheck(If statement, Token else_)
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
    
    private void handleElseIf(If statement, Token ifOfTheElse)
    {
        Token openParen = parser.peek();
        String openParenText = openParen.getTextContent();
        
        if (openParenText.equals("("))
        {
            handleValidatedElseIf(statement, ifOfTheElse, openParen);
        }
        else
        {
            handleElseIfNoOpenParenthesis(statement, ifOfTheElse, openParen);
        }
    }
    
    private void handleValidatedElseIf(If statement, 
        Token ifOfTheElse, Token openParen)
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
            ElseIf elseIf = new ElseIf(condition, openParen.getLineIndex());
            
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
    
    
    private void handleElseIfNoOpenParenthesis(If statement,
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
        
        handleValidatedElseIf(statement, ifOfTheElse, expectedOpenParen);
    }
    
    private void handleElse(If statement, Token openBrace)
    {
        Else elseStatement = new Else(openBrace.getLineIndex());
        
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
