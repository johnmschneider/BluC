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

import bluC.parser.handlers.expression.ExpressionHandler;
import bluC.transpiler.Expression;
import bluC.transpiler.TokenFileInfo;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class While extends Block
{
    private Expression exitCondition;

    public While(long startingLineIndex)
    {
        super(startingLineIndex);

        initDefaultExitCondition();
    }

    private void initDefaultExitCondition()
    {
        this.setExitCondition(
            ExpressionHandler.
                createNullLiteral(
                    TokenFileInfo.NO_FILEPATH, 
                    (int) this.getStartingLineIndex()));
    }

    public Expression getExitCondition()
    {
        return exitCondition;
    }

    public void setExitCondition(Expression exitCondition)
    {
        this.exitCondition = exitCondition;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.exitCondition);
        hash = 47 * hash + Objects.hashCode(this.getBody());
        hash = 47 * hash + (int) (this.getStartingLineIndex() ^ 
            (this.getStartingLineIndex() >>> 32));
        hash = 47 * hash + (int) (this.getEndingLineIndex() ^ 
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
        final While other = (While) obj;
        if (!Objects.equals(this.exitCondition, other.exitCondition))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitWhile(this);
    }
    // TODO - fix .equals and .hashCode to include the new vars as well
    
}
