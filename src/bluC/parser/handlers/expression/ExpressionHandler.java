package bluC.parser.handlers.expression;

import bluC.Logger;
import bluC.transpiler.Expression;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.parser.Parser;
import bluC.parser.exceptions.MalformedNumber;
import bluC.parser.handlers.statement.StatementHandler;
import bluC.parser.handlers.statement.VariableHandler;

/**
 * @author John Schneider
 */
public class ExpressionHandler
{
    private Parser parser;
    private VariableHandler varHandler;
    private ObjectHandler objectHandler;
    
    public ExpressionHandler(Parser parser, StatementHandler statementHandler)
    {
        this.parser = parser;
        varHandler = statementHandler.getVarHandler();
        objectHandler = new ObjectHandler(parser, this);
    }
    
    
    public Expression handleExpression()
    {
        return handleAssignmentOrHigher();
    }

    private Expression handleAssignmentOrHigher()
    {
        Expression expression = handleEqualityOrHigher();
        Token potentialOperator;
        parser.nextToken();
        potentialOperator = parser.getCurToken();
        
        if (potentialOperator.getTextContent().equals("="))
        {
            if (expression instanceof Expression.Variable)
            {
                Expression target = expression;
                Expression value;
                value = handleAssignmentOrHigher();
                return new Expression.Assignment(potentialOperator, target, 
                    value);
            } else
            {
                Logger.err(potentialOperator, "Left expression of " +
                    "assignment operator must be a variable");
            }
        }
        
        parser.prevToken();
        return expression;
    }
    
    private Expression handleEqualityOrHigher()
    {
        Expression expression = handleComparisonOrHigher();
        
        while (parser.peekMatches("!=", "=="))
        {
            parser.nextToken();
            Token operator = parser.getCurToken();
            
            Expression right = handleComparisonOrHigher();
            expression = new Expression.Binary(operator, expression, right);
        }
        
        return expression;
    }
    
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
                next.isString())
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
        Expression expression = handleVariableOrHigher();
        
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
        return handleInvalidStartOfExpression();
    }
    
    private Expression handleVariableOrHigher()
    {
        Token potentialVarName = parser.peek();
        Statement.VarDeclaration varInfo = 
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
}
