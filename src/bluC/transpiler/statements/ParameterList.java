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
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class ParameterList extends Statement
{
    private final ArrayList<VarDeclaration> parameters;

    public ParameterList(long startingLineIndex)
    {
        super(startingLineIndex);
        this.parameters = new ArrayList<>();
    }

    public void addParameter(VarDeclaration param)
    {
        parameters.add(param);
    }

    public ArrayList<VarDeclaration> getParameters()
    {
        return parameters;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.parameters);
        hash = 13 * hash + (int) (this.getStartingLineIndex() ^ (this.getStartingLineIndex() >>> 32));
        hash = 13 * hash + (int) (this.getEndingLineIndex() ^ (this.getEndingLineIndex() >>> 32));
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
        final ParameterList other = (ParameterList) obj;
        if (!Objects.equals(this.parameters, other.parameters))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitParameterList(this);
    }
    
}
