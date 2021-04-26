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

import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.blocks.Block;
import java.util.ArrayList;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.parser.handlers.statement.ClassHandler;
import bluC.transpiler.statements.ExpressionStatement;
import bluC.transpiler.statements.Package;
import bluC.transpiler.statements.blocks.ClassDef;
import bluC.transpiler.statements.blocks.If;
import bluC.transpiler.statements.blocks.Method;
import bluC.transpiler.statements.blocks.Function;
import bluC.transpiler.statements.ParameterList;
import bluC.transpiler.statements.Return;
import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.statements.blocks.While;
import bluC.transpiler.statements.blocks.StructDef;

/**
 *
 * @author John Schneider
 */
public class AstPrinter implements Expression.Visitor<String>, 
    Statement.Visitor<String>
{
    private int     indentationLevel = 0;
    private boolean inOutermostBlock = true;
    
    public String printToString(Expression expression)
    {
        return expression.accept(this);
    }
    
    public String printToString(Statement statement)
    {
        return statement.accept(this) + "\t\"/* end stmt line " + 
            (statement.getStartingLineIndex() + 1) + " */\"";
    }
    
    public void print(Expression expression)
    {
        System.out.println(printToString(expression));
    }
    
    public void print(Statement statement)
    {
        System.out.println(printToString(statement));
    }
    
    public String parenthesize(Token operator, Expression... expressions)
    {
        String s = "";
        
        s += "(" + operator.getTextContent();
        for (Expression expression : expressions)
        {
            s += " " + expression.accept(this);
        }
        
        s += ") \"/* end stmt, line #" + (operator.getLineIndex() + 1) + 
            " */\"";
        return s;
    }
    
    @Override
    public String visitAssignment(Expression.Assignment expression)
    {
        return parenthesize(expression.getOperator(), expression.getOperand1(), 
            expression.getOperand2());
    }
    
    @Override
    public String visitBinary(Expression.Binary expression)
    {
        return parenthesize(expression.getOperator(), expression.getOperand1(), 
            expression.getOperand2());
    }

    @Override
    public String visitGrouping(Expression.Grouping expression)
    {
        Token group = new Token(
            new TokenInfo("grouping", true), 
                
            new TokenFileInfo(expression.getOperator().getFilepath(),
                expression.getOperator().getLineIndex()));
        
        return parenthesize(group, expression.getOperand1());
    }

    @Override
    public String visitLiteral(Expression.Literal expression)
    {
        return expression.getTextContent();
    }

    @Override
    public String visitUnary(Expression.Unary expression)
    {
        if (expression.isOperatorOnRight())
        {
            return "(" + expression.getOperand1().accept(this) + " " + 
                expression.getOperator().getTextContent() + ")";
        }
        else
        {
            return parenthesize(expression.getOperator(), 
                expression.getOperand1());
        }
    }
    
    @Override
    public String visitVar(Expression.Variable expression)
    {
        return "(var-expression " + expression.getOperator().getTextContent() +
            ")";
    }
    
    @Override
    public String visitBlock(Block statement)
    {
        ArrayList<Statement>    contents;
        String                  output;
        boolean                 thisBlockIsOutermostBlock;
        
        output = "\n";
        contents = statement.getBody();
        thisBlockIsOutermostBlock = inOutermostBlock;
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "(block ";
        indentationLevel ++;
        
        for (int i = 0; i < contents.size(); i++)
        {
            Statement s = contents.get(i);
            output += "\n";
            
            for (int i2 = 0; i2 < indentationLevel; i2++)
            {
                output += "    ";
            }
            
            output += s.accept(this);
        }
        
        output += ") \"/* end block, line #" + statement.getStartingLineIndex() + 
            " */\"";
        indentationLevel --;
        return output;
    }

    @Override
    public String visitFunction(Function statement)
    {
        String output = "\n";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        indentationLevel++; 
        
        output += "(function " + 
            statement.getReturnType().accept(this) + " " +
            statement.getNameText() + " " +
            statement.getParameters().accept(this) + 
            statement.acceptBlock(this) + ")\n";
        
        indentationLevel--;
        return output;
    }
    
    @Override
    public String visitMethod(Method statement)
    {
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        indentationLevel++; 
        
        output += "(method " +
            statement.getReturnType().accept(this) + " " +
            statement.getNameText() + " " +
            statement.getParameters().accept(this) + 
            statement.acceptBlock(this) + ")";
        
        indentationLevel--;
        return output;
    }
    
    @Override
    public String visitParameterList(ParameterList statement)
    {
        String output = "(parameter-list ";
        ArrayList<VarDeclaration> params = statement.getParameters();
        
        for (int i = 0; i < params.size() - 1; i++)
        {
            VarDeclaration param = params.get(i);
            output += param.accept(this) + " ";
        }
        
        if (!params.isEmpty())
        {
            output += params.get(params.size() - 1).accept(this);
        }
        
        output += ")";
        return output;
    }
    
    @Override
    public String visitIf(If statement)
    {
        String output = "";
        
        //indent opening brace
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        indentationLevel++;
        
        output = "(if " + statement.getCondition().accept(this) + ")";
        output += statement.acceptBlock(this);
        
        indentationLevel--;
        
        if (!statement.getElseIfs().isEmpty())
        {
            output += visitElseIfs(statement);
        }
        
        if (statement.getElse() != null)
        {
            output += visitElse(statement);
        }
        
        return output;
    }
    
    private String visitElseIfs(If statement)
    {
        String output = "\n";
        ArrayList<If.ElseIf> elseIfs = statement.getElseIfs();
        
        //indent opening paren
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "(else-ifs ";
        
        //all of the else-if statements are technically part of the 
        //  else-ifs ArrayList
        indentationLevel++;
        
        for (If.ElseIf elseIf : elseIfs)
        {
            output += visitElseIf(elseIf);
        }
        
        output += ")";
        indentationLevel--;
        
        return output;
    }
    
    private String visitElseIf(If.ElseIf elseIf)
    {
        String output = "\n";
        
        //indent "else if" token and "condition" tokens
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        // block auto-adds newline
        output += "(else-if (" + elseIf.getCondition().accept(this) + ")";
        output += elseIf.acceptBlock(this);
        output += ")";
        
        return output;
    }
    
    private String visitElse(If statement)
    {
        Block else_ = statement.getElse();
        String output = "\n";
        
        //indent "else if" token and "condition" tokens
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        // block auto-adds newline
        output += "else";
        output += else_.acceptBlock(this);
        
        return output;
    }
    
    @Override
    public String visitClassDef(ClassDef statement)
    {
        Token className = statement.getClassName();
        String output = "\n";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "(class-def " + className.getTextContent();
        
        indentationLevel++;
        output += statement.acceptBlock(this);
        indentationLevel--;
        
        output += ")\n";
        
        return output;
    }
    
    @Override
    public String visitStructDef(StructDef statement)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String visitWhile(While statement)
    {
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output = "(while (" + statement.getExitCondition().accept(this) + ")";
        
        indentationLevel++;
        output += statement.acceptBlock(this) +
            ") \"/* end while line #" + statement.getStartingLineIndex() +
            "*/\"";
        indentationLevel--;
        
        return output;
    }

    @Override
    public String visitReturn(Return statement)
    {
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output = "(return " + statement.getReturnedStatement().accept(this) + 
            ")";
        
        return output;
    }

    @Override
    public String visitExpressionStatement(ExpressionStatement 
        statement)
    {
        return "(expression-statement " + statement.getExpression().
            accept(this) + ")";
    }

    @Override
    public String visitVarDeclaration(VarDeclaration statement)
    {
        String output = "(var-declaration ";
        
        
        if (statement.getSimplifiedType() == SimplifiedType.CLASS)
        {
            ClassDef class_ = ClassHandler.getClassDefinition(
                statement.getClassID());
            output += class_.getClassName().getTextContent();
        }
        else
        {
            output += statement.getSign().name().toLowerCase() + " " + 
                statement.getSimplifiedType().name().toLowerCase().
                    replace("_", " ");
        }
        
        if (!statement.isReturnVar())
        {
            output += " ";
        }
        
        for (int i = 0; i < statement.getPointerLevel(); i++)
        {
            output += "*";
        }
        
        output += statement.getName().getTextContent();
        
        Expression value = statement.getValue();
        if (value != null)
        {
            output += " (= " + value.accept(this) + ")";
        }
        
        output += ")";
            
        return output;
    }

    @Override
    public String visitPackage(Package statement)
    {
        return "(package " + statement.getFullyQualifiedPackageName() + ")";
    }
    
}