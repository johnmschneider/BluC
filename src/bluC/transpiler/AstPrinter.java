package bluC.transpiler;

import java.util.ArrayList;
import bluC.transpiler.Statement.VarDeclaration.SimplifiedType;
import bluC.parser.handlers.statement.ClassHandler;

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
    public String visitBlock(Statement.Block statement)
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
    public String visitFunction(Statement.Function statement)
    {
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        indentationLevel++; 
        
        output += "(function " + 
            statement.getReturnType().accept(this) + " " +
            statement.getNameText() + " " +
            statement.getParameters().accept(this) + 
            statement.acceptBlock(this) + ")";
        
        indentationLevel--;
        return output;
    }
    
    @Override
    public String visitMethod(Statement.Method statement)
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
    public String visitParameterList(Statement.ParameterList statement)
    {
        String output = "(parameter-list ";
        ArrayList<Statement.VarDeclaration> params = statement.getParameters();
        
        for (int i = 0; i < params.size() - 1; i++)
        {
            Statement.VarDeclaration param = params.get(i);
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
    public String visitIf(Statement.If statement)
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
    
    private String visitElseIfs(Statement.If statement)
    {
        String output = "\n";
        ArrayList<Statement.If.ElseIf> elseIfs = statement.getElseIfs();
        
        //indent opening paren
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "(else-ifs ";
        
        //all of the else-if statements are technically part of the 
        //  else-ifs ArrayList
        indentationLevel++;
        
        for (Statement.If.ElseIf elseIf : elseIfs)
        {
            output += visitElseIf(elseIf);
        }
        
        output += ")";
        indentationLevel--;
        
        return output;
    }
    
    private String visitElseIf(Statement.If.ElseIf elseIf)
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
    
    private String visitElse(Statement.If statement)
    {
        Statement.Block else_ = statement.getElse();
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
    public String visitClassDef(Statement.ClassDef statement)
    {
        Token className = statement.getClassName();
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "(class-def " + className.getTextContent() + "\n";
        
        indentationLevel++;
        output += statement.acceptBlock(this);
        indentationLevel--;
        
        output += ")";
        
        return output;
    }
    
    @Override
    public String visitStructDef(Statement.StructDef statement)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String visitWhile(Statement.While statement)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String visitReturn(Statement.Return statement)
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
    public String visitExpressionStatement(Statement.ExpressionStatement 
        statement)
    {
        return "(expression-statement " + statement.getExpression().
            accept(this) + ")";
    }

    @Override
    public String visitVarDeclaration(Statement.VarDeclaration statement)
    {
        String output = "(var-declaration ";
        
        
        if (statement.getSimplifiedType() == SimplifiedType.CLASS)
        {
            Statement.ClassDef class_ = ClassHandler.getClassDefinition(
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
    public String visitPackage(Statement.Package statement)
    {
        return "(package " + statement.getFullyQualifiedPackageName() + ")";
    }
    
}