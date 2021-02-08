package bluC.transpiler;

/**
 *
 * @author John Schneider
 */
public abstract class Expression
{
    private Token operator;
    private Expression operand1, operand2;
    
    public static interface Visitor<T>
    {
        T visitAssignment(Expression.Assignment visitor);
        T visitBinary(Expression.Binary visitor);
        T visitUnary(Expression.Unary visitor);
        T visitLiteral(Expression.Literal visitor);
        T visitVar(Expression.Variable visitor);
        T visitGrouping(Expression.Grouping visitor);
    }
    
    public static class Assignment extends Expression
    {
        public Assignment(Token operator, Expression target, Expression value)
        {
            super(operator, target, value);
        }

        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitAssignment(this);
        }
    }
    
    public static class Binary extends Expression
    {
        public Binary(Token operator, Expression operand1, Expression operand2)
        {
            super(operator, operand1, operand2);
        }

        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitBinary(this);
        }
    }
    
    public static class Grouping extends Expression
    {
        public Grouping(Token operator, Expression operand1)
        {
            super(operator, operand1, null);
        }
        
        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitGrouping(this);
        }
    }
    
    public static class Literal extends Expression
    {   
        private Token value;

        public Literal(Token value)
        {
            super(null, null, null);
            
            this.value = value;
        }

        public Token getValue()
        {
            return value;
        }

        public String getTextContent()
        {
            return getValue().getTextContent();
        }
        
        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitLiteral(this);
        }
    }
    
    public static class Variable extends Expression
    {
        private Statement.VarDeclaration variableInfo;
        
        public Variable(Statement.VarDeclaration variableInfo)
        {
            super(variableInfo.getName(), null, null);
            this.variableInfo = variableInfo;
        }
        
        public Statement.VarDeclaration getVariableInfo()
        {
            return variableInfo;
        }
        
        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitVar(this);
        }
    }
    
    public static class Unary extends Expression
    {
        private final boolean operatorIsOnRight;
        
        public Unary(Token operator, Expression operand1)
        {
            super(operator, operand1, null);
            operatorIsOnRight = false;
        }
        
        public Unary(Token operator, Expression operand1,
            boolean operatorIsOnRight)
        {
            super(operator, operand1, null);
            this.operatorIsOnRight = operatorIsOnRight;
        }
        
        public boolean isOperatorOnRight()
        {
            return operatorIsOnRight;
        }
        
        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitUnary(this);
        }
    }
    
    public Expression(Token operator, Expression operand1, 
        Expression operand2)
    {
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    
    public Token getOperator()
    {
        return operator;
    }

    public void setOperator(Token operator)
    {
        this.operator = operator;
    }

    public Expression getOperand1()
    {
        return operand1;
    }

    public void setOperand1(Expression operand1)
    {
        this.operand1 = operand1;
    }

    public Expression getOperand2()
    {
        return operand2;
    }

    public void setOperand2(Expression operand2)
    {
        this.operand2 = operand2;
    }
    
    public abstract <T> T accept(Visitor<T> visitor);
}