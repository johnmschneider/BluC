package bluC.transpiler.parser.handlers.expression;

import bluC.transpiler.Expression;
import bluC.transpiler.Token;
import bluC.transpiler.parser.Parser;

/**
 *
 * @author John Schneider
 */
public class ObjectHandler
{
    private ExpressionHandler expressionHandler;
    private Parser parser;
    
    public ObjectHandler(Parser parser, ExpressionHandler expressionHandler)
    {
        this.expressionHandler = expressionHandler;
        this.parser = parser;
    }
    
    
    public Expression handleObjectAccess(Expression.Variable var)
    {
        Token operator = parser.peek();
        Expression right;
        
        parser.nextToken();
        right = expressionHandler.handleExpression();
         
        return new Expression.Binary(operator, var, right);
    }
}
