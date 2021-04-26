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

package bluC.transpiler;

import bluC.transpiler.statements.vars.VarDeclaration;
import java.util.Objects;

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
        
        
        @Override
        public boolean isNullLiteral()
        {
            return 
                value.getTextContent().
                equals("null");
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

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.value);
            hash = 89 * hash + Objects.hashCode(this.getOperator());
            hash = 89 * hash + Objects.hashCode(this.getOperand1());
            hash = 89 * hash + Objects.hashCode(this.getOperand2());
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            if (!super.equals(obj))
            {
                return false;
            }
            
            final Literal other = (Literal) obj;
            if (!Objects.equals(this.value, other.value))
            {
                return false;
            }
            return true;
        }
    }
    
    /**
     * For usage of a variable in an EXPRESSION.
     */
    public static class Variable extends Expression
    {
        private VarDeclaration variableInfo;

        public Variable(VarDeclaration variableInfo)
        {
            super(variableInfo.getName(), null, null);
            this.variableInfo   = variableInfo;
        }
        
        public VarDeclaration getVariableInfo()
        {
            return variableInfo;
        }
        
        @Override
        public <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitVar(this);
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.variableInfo);
            hash = 67 * hash + Objects.hashCode(this.getOperator());
            hash = 67 * hash + Objects.hashCode(this.getOperand1());
            hash = 67 * hash + Objects.hashCode(this.getOperand2());
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            if (!super.equals(obj))
            {
                return false;
            }
            
            final Variable other = (Variable) obj;
            if (!Objects.equals(this.variableInfo, other.variableInfo))
            {
                return false;
            }
            return true;
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

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 47 * hash + (this.operatorIsOnRight ? 1 : 0);
            hash = 47 * hash + Objects.hashCode(this.getOperator());
            hash = 47 * hash + Objects.hashCode(this.getOperand1());
            hash = 47 * hash + Objects.hashCode(this.getOperand2());
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            if (!super.equals(obj))
            {
                return false;
            }
            
            final Unary other = (Unary) obj;
            if (this.operatorIsOnRight != other.operatorIsOnRight)
            {
                return false;
            }
            return true;
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

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.operator);
        hash = 11 * hash + Objects.hashCode(this.operand1);
        hash = 11 * hash + Objects.hashCode(this.operand2);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Expression other = (Expression) obj;
        if (!Objects.equals(this.operator, other.operator))
        {
            return false;
        }
        if (!Objects.equals(this.operand1, other.operand1))
        {
            return false;
        }
        if (!Objects.equals(this.operand2, other.operand2))
        {
            return false;
        }
        return true;
    }
    
    public boolean isNullLiteral()
    {
        return false;
    }
}