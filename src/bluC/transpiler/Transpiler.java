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
import bluC.Logger;
import bluC.parser.Parser;
import java.util.ArrayList;
import bluC.transpiler.statements.blocks.If.ElseIf;
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
public class Transpiler implements Expression.Visitor<String>,
    Statement.Visitor<String>
{
    private String filepath;
    private ArrayList<String> rawFileContents;
    private Lexer lexer;
    private ArrayList<Token> lexedTokens;
    private Parser parser;
    private ArrayList<Statement> statements;
    private ArrayList<String> outputFileContents;
    
    /**
     * How many blocks the transpiler is currently inside
     */
    private int indentationLevel = 0;
    
    public Transpiler(String filepath, ArrayList<String> rawFileContents)
    {
        this.filepath = filepath;
        this.rawFileContents = rawFileContents;
        lexer = new Lexer(filepath, rawFileContents);
        outputFileContents = new ArrayList<>();
    }
    
    public ArrayList<String> transpile()
    {
        AstPrinter printer;
        
        importCoreLanguageDependencies();
        lex();
        parse();
        
        if (!Logger.hasLoggedError())
        {
            // TODO : only forward-declare funcs/methods/classes that
            //  require forward declaration to work right (right now 
            //  we forward-declare all of them)
            forwardDeclareFuncsClassesAndMethods();
            printer = new AstPrinter();

            for (Statement statement : statements)
            {
                String output = statement.accept(this);

                if (statement.needsSemicolon())
                {
                    output += ";";
                }

                addToOutputFileContents(output);

                printer.print(statement);
            }
        }
        else
        {
            parser.dumpAstToStdout();
        }
        
        return outputFileContents;
    }
    
    private void addToOutputFileContents(String strToSplit)
    {
        String[] splitStr = strToSplit.split("\n");
        
        for (String s : splitStr)
        {
            outputFileContents.add(s);
        }
    }
    
    private void lex()
    {
        CommentsRemover.run(filepath, rawFileContents);
        for (String s : rawFileContents)
        {
            System.out.println(s);
        }
        
        lexedTokens = lexer.lex();
        lexer.debug_writeOutput();
    }
    
    private void parse()
    {
        parser      = new Parser(lexedTokens);
        statements  = parser.parse();
    }
    
    private void importCoreLanguageDependencies()
    {
        
    }
    
    private void forwardDeclareFuncsClassesAndMethods()
    {
        
    }
    
    @Override
    public String visitBlock(Block statement)
    {
        String output = "";
        
        //indent opening brace
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "{\n";
        indentationLevel ++;
        
        for (Statement s : statement.getBody())
        {
            if (!(s instanceof Block))
            {
                //indent code
                for (int i = 0; i < indentationLevel; i++)
                {
                    output += "    ";
                }
            }
            
            output += s.accept(this);
            
            if (!(s instanceof Block))
            {
                if (s.needsSemicolon())
                {
                    output += ";";
                }

                output += "\n";
            }
            //else blocks add a newline to themselves
        }
        
        indentationLevel --;
        
        //indent closing brace
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "}\n";
        
        if (statement.needsExtraSpace())
        {
            output += " \n";
        }
        
        return output;
    }

    @Override
    public String visitFunction(Function statement)
    {
        String output = "";
        
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += statement.getReturnType().accept(this) + " " + 
                statement.getNameText() + 
                statement.getParameters().accept(this) + "\n" +
                statement.acceptBlock(this);
        
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
        
        output += statement.getReturnType().accept(this) + " " + 
                statement.getMangledName() + 
                statement.getParameters().accept(this) + "\n" +
                statement.acceptBlock(this);
        
        return output;
    }
    
    @Override
    public String visitParameterList(ParameterList statement)
    {
        String output = "(";
        ArrayList<VarDeclaration> params = statement.getParameters();
        
        for (int i = 0; i < params.size() - 1; i++)
        {
            VarDeclaration param = params.get(i);
            output += param.accept(this) + ", ";
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
        
        //indent "if" token and "condition" tokens
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "if (" + statement.getCondition().accept(this) + ")\n";
        output += statement.acceptBlock(this);
        
        output += visitElseIfs(statement);
        output += visitElse(statement);
        output += "\n";
        
        return output;
    }
    
    private String visitElseIfs(If statement)
    {
        String output = "";
        ArrayList<ElseIf> elseIfs = statement.getElseIfs();
        
        for (ElseIf elseIf : elseIfs)
        {
            output += visitElseIf(elseIf);
        }
        
        return output;
    }
    
    private String visitElseIf(ElseIf elseIf)
    {
        String output = "";
        
        //indent "else if" token and "condition" tokens
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "else if (" + elseIf.getCondition().accept(this) + ")\n";
        output += elseIf.acceptBlock(this);
        
        return output;
    }
    
    private String visitElse(If statement)
    {
        Block else_ = statement.getElse();
        String output = "";
        
        //indent "else if" token and "condition" tokens
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "else\n";
        output += else_.acceptBlock(this);
        
        return output;
    }
    
    @Override
    public String visitClassDef(ClassDef statement)
    {
        ArrayList<Method> methods = new ArrayList<>();
        String output = "typedef struct\n" +
                        "{\n";
        
        indentationLevel++;
        
        for (Statement s : statement.getBody()) 
        {
            if (s instanceof Method)
            {
                methods.add((Method) s);
            }
            else
            {
                for (int i = 0; i < indentationLevel; i++)
                {
                    output += "    ";
                }

                output += s.accept(this);

                if (s.needsSemicolon())
                {
                    output += ";\n";
                }
            }
        }
        
        indentationLevel--;
        output += "} " + statement.getClassName().getTextContent() + ";\n \n";
        
        for (Method m : methods)
        {
            for (int i = 0; i < indentationLevel; i++)
            {
                output += "    ";
            }

            output += m.accept(this);
        }
        
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
        
        // indent while
        for (int i = 0; i < indentationLevel; i++)
        {
            output += "    ";
        }
        
        output += "while (" + statement.getExitCondition().accept(this) + ")\n";
        output += statement.acceptBlock(this);
        
        return output;
    }

    @Override
    public String visitReturn(Return statement)
    {
        return "return " + statement.getReturnedStatement().accept(this);
    }

    @Override
    public String visitExpressionStatement(ExpressionStatement 
        statement)
    {
        return statement.getExpression().accept(this);
    }

    @Override
    public String visitVarDeclaration(VarDeclaration statement)
    {
        String sign = statement.getSign().name().toLowerCase();
        String output = "";
        Expression value = statement.getValue();
        
        if (!sign.equals("unspecified"))
        {
            output = sign + " ";
        }
        
        if (statement.getSimplifiedType() == SimplifiedType.CLASS)
        {
            output += ClassHandler.getClassDefinition(statement.getClassID()).
                getClassName().getTextContent();
        }
        else 
        {
            output += statement.getSimplifiedType().name().toLowerCase().
                replace("_", " ");
        }
        
        if (!statement.isReturnVar())
        {
            output += " ";
        }
        //else it's a return declaration
        
        for (int i = 0; i < statement.getPointerLevel(); i++)
        {
            output += "*";
        }
        
        output += statement.getNameText();
        
        if (value != null)
        {
            output += " " + statement.getAssignmentOperator().getTextContent() +
                " " + value.accept(this);
        }
        
        return output;
    }
    
    @Override
    public String visitAssignment(Expression.Assignment visitor)
    {
        return visitor.getOperand1().accept(this) + " " +
            visitor.getOperator().getTextContent() + " " +
            visitor.getOperand2().accept(this);
    }
    
    @Override
    public String visitBinary(Expression.Binary visitor)
    {
        return visitor.getOperand1().accept(this) + " " +
            visitor.getOperator().getTextContent() + " " +
            visitor.getOperand2().accept(this);
    }

    @Override
    public String visitGrouping(Expression.Grouping visitor)
    {
        return "(" + visitor.getOperand1().accept(this) + ")";
    }

    @Override
    public String visitLiteral(Expression.Literal visitor)
    {
        return visitor.getTextContent();
    }

    @Override
    public String visitUnary(Expression.Unary visitor)
    {
        if (visitor.isOperatorOnRight())
        {
            return visitor.getOperand1().accept(this) +
                visitor.getOperator().getTextContent();
        }
        else
        {
            return visitor.getOperator().getTextContent() +
                visitor.getOperand1().accept(this);
        }
    }
    
    @Override
    public String visitVar(Expression.Variable expression)
    {
        return expression.getOperator().getTextContent();
    }

    @Override
    public String visitPackage(Package statement)
    {
        /**
         * there's no concept of a package in c, and our package keyword
         *  is basically just an additional string to mangle onto mangled names
         */
        return statement.getFullyQualifiedPackageName();
    }
}
