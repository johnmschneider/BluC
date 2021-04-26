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
package bluC.transpiler.statements.blocks;

import bluC.transpiler.statements.ParameterList;
import bluC.transpiler.Token;
import bluC.transpiler.statements.vars.VarDeclaration;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Function extends Block
{
    private VarDeclaration  returnType;
    private ParameterList   parameterList;
    private Token           functionName;

    public Function(
        VarDeclaration returnType, Token functionName, long startingLineIndex)
    {
        super(startingLineIndex);
        this.returnType = returnType;
        this.functionName = functionName;
    }

    public VarDeclaration getReturnType()
    {
        return returnType;
    }

    public boolean hasValidName()
    {
        return functionName.isValidName();
    }

    public Token getNameToken()
    {
        return functionName;
    }

    public String getNameText()
    {
        return functionName.getTextContent();
    }

    public void setName(Token newName)
    {
        functionName = newName;
    }

    public void setParameters(ParameterList parameters)
    {
        parameterList = parameters;
    }

    public ParameterList getParameters()
    {
        return parameterList;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.returnType);
        hash = 37 * hash + Objects.hashCode(this.parameterList);
        hash = 37 * hash + Objects.hashCode(this.functionName);
        hash = 37 * hash + (int) (this.getStartingLineIndex() ^
            (this.getStartingLineIndex() >>> 32));
        hash = 37 * hash + (int) (this.getEndingLineIndex() ^
            (this.getEndingLineIndex() >>> 32));
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
        final Function other = (Function) obj;
        if (!Objects.equals(this.returnType, other.returnType))
        {
            return false;
        }
        if (!Objects.equals(this.parameterList, other.parameterList))
        {
            return false;
        }
        if (!Objects.equals(this.functionName, other.functionName))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitFunction(this);
    }
    
}
