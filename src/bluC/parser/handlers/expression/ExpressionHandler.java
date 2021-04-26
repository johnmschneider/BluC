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

package bluC.parser.handlers.expression;

import bluC.Logger;
import bluC.transpiler.Expression;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.parser.Parser;
import bluC.parser.exceptions.MalformedNumber;
import bluC.parser.handlers.statement.StatementHandler;
import bluC.parser.handlers.statement.VariableHandler;
import bluC.transpiler.Expression.Binary;
import bluC.transpiler.statements.vars.VarDeclaration;

/**
 * @author John Schneider
 */
public class ExpressionHandler
{
    private final Parser          parser;
    private final VariableHandler varHandler;
    private final ObjectHandler   objectHandler;
    
    public ExpressionHandler(Parser parser, StatementHandler statementHandler)
    {
        this.parser     = parser;
        varHandler      = statementHandler.getVarHandler();
        objectHandler   = new ObjectHandler(parser, this);
    }
    
    public Expression handleExpression()
    {
        return handlePreDeclAssignmentOrHigher();
    }
    
    /**
     * Handles an expression assigning a value to a variable that has not yet
     *  been declared (presumably as part of a VarDeclaration).
     * 
     * Expects to be on the token immediately before the "=".
     */
    private Expression handlePreDeclAssignmentOrHigher()
    {
        Expression result;
        
        if (parser.peekMatches("="))
        {
            // move to "="
            parser.nextToken();
        }

        result = handlePostDeclAssignmentOrHigher();
        if (result == null)
        {
            // TODO - temp test for debugging
            System.err.println("RESULT IS NULL:");
            new NullPointerException().printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Handles an expression assigning a value to a variable that has already
     *  been declared.
     * 
     * Expects to be on the token immediately before the start of the left-hand
     *  expression.
     */
    private Expression handlePostDeclAssignmentOrHigher()
    {
        Token       potentialOperator;
        Expression  leftOperand;
        
        leftOperand         = handleEqualityOrHigher();
        potentialOperator   = parser.peek();
        
        if (potentialOperator.getTextContent().equals("="))
        {
            // move to "="
            parser.nextToken();
            
            Binary assignment = new Binary(
                potentialOperator, leftOperand,
                handlePostDeclAssignmentOrHigher());
            
            return assignment;
        }
        
        return leftOperand;
    }
    
    private Expression handleEqualityOrHigher()
    {
        boolean     currentOperatorIsEquality = false;
        int         startIndex;
        
        startIndex          = parser.getCurTokIndex();
        
        while (!parser.atEOF())
        {
            if (parser.peekMatches("!=", "=="))
            {
                currentOperatorIsEquality = true;
                break;
            }
            else if(parser.peekMatches(";", "(", ")", "{", "}"))
            {
                break;
            }
            
            parser.nextToken();
        }
        
        parser.setToken(startIndex);
        
        Expression result;
        if (currentOperatorIsEquality)
        {
            result = parseEquality();
        }
        else 
        {
            result = handleComparisonOrHigher();
        }
        
        return result;
    }
    
    /**
     * Parses equality expression.
     * 
     * Expects to be on the starting token of the expression.
     * 
     * Leaves parser on end token of the expression.
     */
    private Expression parseEquality()
    {
        Expression left     = handleComparisonOrHigher();
        Expression right    = null;
        Binary     result   = null;
        
        // handle first comparison
        if (parser.peekMatches("!=", "=="))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            right = handleComparisonOrHigher();
            result = new Expression.Binary(
                operator, left, right);
        }
        
        // handle any further comparisons chained together
        while (parser.peekMatches("!=", "=="))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();

            left     = right;
            right    = handleComparisonOrHigher();
            result   = new Expression.Binary(
                operator, left, right);
        }
        
        return (result == null ? left : result);
    }
    
    // TODO: move this into static analysis/type checker, really anything other
    //  than the parser..
    /**
     * Prints out bad operand message and 
     */
    /*private Expression handleBadEqualityLeftOp(
        ArrayList<Token> expressionSoFar, Token firstOperatorFound)
    {
        String output = "l-value \"";
        
        for (int i = 0; i < expressionSoFar.size() - 1; i++)
        {
            Token t = expressionSoFar.get(i);
            output += t.getTextContent() + " ";
        }
        
        output += expressionSoFar.get(expressionSoFar.size()) + 
            "\" is not able to be used with infix operator " + 
            firstOperatorFound.getTextContent();
        
        
        Expression.Literal nullLit = createNullLiteral(
            firstOperatorFound.getFilepath(),
            firstOperatorFound.getLineIndex());
        
        Binary resultToSyncParser = 
            new Binary(firstOperatorFound, nullLit, nullLit);
        
        return resultToSyncParser;
    }*/
    
    /**
     * Expects the peek() to be a comparison expression or higher.
     * 
     * Leaves the parser on the ending token of whatever expression
     *  it managed to match (or the best-fit token for synchronization).
     */
    private Expression handleComparisonOrHigher()
    {
        Expression expression = handleAdditionOrHigher();
        
        while (parser.peekMatches(">", ">=", "<", "<="))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            Expression right = handleAdditionOrHigher();
            expression = new Expression.Binary(operator, expression, right);
        }
        
        return expression;
    }
    
    /**
     * Expects the peek() to be a n addition expression or higher.
     * 
     * Leaves the parser on the ending token of whatever expression
     *  it managed to match (or the best-fit token for synchronization).
     */
    private Expression handleAdditionOrHigher()
    {
        Expression expression = handleMultiplicationOrHigher();
        
        while (parser.peekMatches("-", "+"))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            Expression right = handleMultiplicationOrHigher();
            expression = new Expression.Binary(operator, expression, right);
        }
        
        return expression;
    }

    private Expression handleMultiplicationOrHigher()
    {
        Expression expression = handleUnaryOrHigher();
        
        while (parser.peekMatches("/", "*"))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            Expression right = handleUnaryOrHigher();
            expression = new Expression.Binary(operator, expression, right);
        }
        
        return expression;
    }

    private Expression handleUnaryOrHigher()
    {
        while (parser.peekMatches("!", "-"))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            Expression right = handleUnaryOrHigher();
            return new Expression.Unary(operator, right);
        }
        
        return handleLiteralOrHigher();
    }
    
    private Expression handleLiteralOrHigher()
    {   
        Expression literal = handleLiteral();
        
        if (literal != null)
        {
            return literal;
        }
        
        return handleHighestPrecedence();
    }
    
    private Expression handleLiteral()
    {
        Token next = parser.peek();
        
        try
        {
            if (parser.peekMatches("false") || parser.peekMatches("true") ||
                parser.peekMatches("null") || next.isNumber() || 
                next.isStringLiteral() || next.isCharLiteral())
            {
                parser.nextToken();
                Token literal = parser.getCurToken();
                
                return new Expression.Literal(literal);
            }
        }
        catch (MalformedNumber ex)
        {
            bluC.Logger.err(next, ex.getMessage() + " Unexpected character \"" + 
                next.getTextContent().charAt(ex.getOffendingCharIndex())+ "\"");
            return new Expression.Literal(new Token(
                    
                new TokenInfo("null", true),
                    
                new TokenFileInfo(next.getFilepath(), next.getLineIndex())));
        }
        
        return null;
    }
    
    private Expression handleHighestPrecedence()
    {
        Expression expression = handleVariable();
        
        if (expression == null)
        {
            if (parser.peekMatches("("))
            {
                expression = handleGrouping();
            }
            else if (parser.peekMatches("--", "++"))
            {
                expression = handlePrefixIncrementOrDecrement();
            }
            
            if (expression != null)
            {
                return expression;
            }
        }
        else
        {
            return expression;
        }
        
        //if we reached here then something went wrong with the parsing,
        //  so throw an error
        
        // TODO - fix this, it logs an error before it returns back to the
        //  other statement handlers to try and match a different grammar.
        return handleInvalidStartOfExpression();
    }
    
    /**
     * Attempts to parse the next token as a variable usage, returns null if
     *  this failed.
     * 
     * Expects to be on the token immediately before a variable declaration.
     * 
     * @param isLvalue - whether or not this is on the left hand side of
     *  an expression.
     */
    private Expression handleVariable()
    {
        Token potentialVarName = parser.peek();
        VarDeclaration varInfo = 
            varHandler.getVarAlreadyDeclaredInThisScopeOrHigher
            (potentialVarName);
        Expression returnee = null;
        
        if (varInfo != null)
        {
            Expression.Variable var;
            
            //set token to var name such that nextToken isn't varName
            parser.nextToken();
            
            var = new Expression.Variable(varInfo);
            
            if (parser.peekMatches("--", "++"))
            {
                returnee = handleValidPostfixIncrementOrDecrement(var);
            }
            else if (parser.peekMatches(".", "->"))
            {
                returnee = objectHandler.handleObjectAccess(var);
            }
            else
            {
                returnee = var;
            }
        }
        
        return returnee;
    }
    
    private Expression handleGrouping()
    {
        parser.nextToken();
        Token operator = parser.getCurToken();
        Expression groupee = handleExpression();

        if (!parser.peekMatches(")"))
        {
            Logger.err(operator, "Expected \")\" to close \"" + 
                operator.getTextContent() + "\"");
        }
        else
        {
            //throw away ")" token and move onto the next
            parser.nextToken();
            return new Expression.Grouping(operator, groupee);
        }
        
        //synchronize parser
        parser.nextToken();
        return null;
    }
    
    private Expression handlePrefixIncrementOrDecrement()
    {
        Token operator = parser.peek();
        Expression right;
        
        parser.nextToken();
        right = handleExpression();

        if (right instanceof Expression.Variable)
        {
            return handleValidPrefixIncrementOrDecrement(operator, 
                right);
        }
        else
        {
            Logger.err(operator, "Right expression of prefix-" +
                (operator.getTextContent().equals("--") ? "decrement" : 
                    "increment") + 
                " operator must be a variable");

            return null;
        }
    }
    
    private Expression handleValidPrefixIncrementOrDecrement(Token operator,
        Expression right)
    {
        return new Expression.Unary(operator, right);
    }
    
    private Expression handleValidPostfixIncrementOrDecrement(
        Expression.Variable var)
    {
        Token operator = parser.peek();
        parser.nextToken();
        
        return new Expression.Unary(operator, var, true);
    }
    
    private Expression handleInvalidStartOfExpression()
    {
        Token next = parser.peek();
        Logger.err(next, "\"" + next.getTextContent() + "\" cannot be the " +
            "start of a new expression");
        
        parser.gotoEndOfStatement();
        
        if (parser.atEOF())
        {
            //this should never happen so dump ast and stack trace
            parser.dumpAstToStdout();
            new Exception("Fatal parse error: prematurely reached end of " +
                "file").printStackTrace();
            
            //exit because otherwise our parser goes into an infinite loop
            System.exit(1);
        }
        
        return new Expression.Literal(new Token(
            new TokenInfo("null", true),
                
            new TokenFileInfo(next.getFilepath(), next.getLineIndex())));
    }
    
    public static Expression.Literal createNullLiteral(String filePath, 
        int lineIndex)
    {
        return new Expression.Literal(new Token(
            new TokenInfo("null", true),
                
            new TokenFileInfo(filePath, lineIndex)));
    }
    
}
