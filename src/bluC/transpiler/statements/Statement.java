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

package bluC.transpiler.statements;

import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.statements.blocks.While;
import bluC.transpiler.statements.blocks.StructDef;
import bluC.transpiler.statements.blocks.ClassDef;
import bluC.transpiler.statements.blocks.If;
import bluC.transpiler.statements.blocks.Method;
import bluC.transpiler.statements.blocks.Function;
import bluC.transpiler.statements.blocks.Block;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.parser.handlers.expression.ExpressionHandler;
import bluC.transpiler.Expression;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import java.util.Objects;

/**
 * @author John Schneider
 */
public abstract class Statement
{
    public static long NO_STARTING_LINE_INDEX = -1;
    public static long NO_ENDING_LINE_INDEX   = -1;
    
    private long startingLineIndex  = NO_STARTING_LINE_INDEX;
    private long endingLineIndex    = NO_ENDING_LINE_INDEX;
    
    public static interface Visitor<T>
    {
        //blocks
        T visitBlock        (Block statement);
        
        T visitFunction     (Function statement);
        T visitMethod       (Method statement);
        T visitParameterList(ParameterList statement);
                    
        T visitIf           (If statement);
        T visitClassDef     (ClassDef statement);
        T visitStructDef    (StructDef statement);
        T visitWhile        (While statement);
        
        //misc
        T visitReturn               (Return statement);
        T visitExpressionStatement  (ExpressionStatement statement);
        T visitPackage              (Package statement);
        
        //vars
        T visitVarDeclaration       (VarDeclaration statement);
    }

    
    
    public Statement(long startingLineIndex)
    {
        this.startingLineIndex = startingLineIndex;
    }
    
    
    public abstract <T> T accept(Visitor<T> visitor);
    
    public boolean needsSemicolon()
    {
        return true;
    }
    
    /**
     * Returns the line the statement started on.
     */
    public long getStartingLineIndex()
    {
        return startingLineIndex;
    }
    
    public void setStartingLineIndex(long startingLineIndex)
    {
        this.startingLineIndex = startingLineIndex;
    }
    
    /**
     * Returns the line the statement ended on.
     */
    public long getEndingLineIndex()
    {
        return endingLineIndex;
    }
    
    public void setEndingLineIndex(long endingLineIndex)
    {
        this.endingLineIndex = endingLineIndex;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Statement)
        {
            Statement otherStmt = (Statement) other;
            return 
                getStartingLineIndex() == otherStmt.getStartingLineIndex() &&
                getEndingLineIndex() == otherStmt.getEndingLineIndex();
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + (int) (this.startingLineIndex ^ 
            (this.startingLineIndex >>> 32));
        hash = 83 * hash + (int) (this.endingLineIndex ^ 
            (this.endingLineIndex >>> 32));
        return hash;
    }
}