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

import bluC.transpiler.Expression;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class If extends Block
{
    private final Expression        condition;
    private final ArrayList<ElseIf> elseIfs;
    private Else else_;
    
    public static class ElseIf extends Block
    {
        Expression condition;

        public ElseIf(Expression condition, long startingLineIndex)
        {
            super(startingLineIndex);
            this.condition = condition;
        }

        public Expression getCondition()
        {
            return condition;
        }

        @Override
        public boolean needsExtraSpace()
        {
            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.condition);
            hash = 59 * hash + (int) (this.getStartingLineIndex() ^ 
                (this.getStartingLineIndex() >>> 32));
            hash = 59 * hash + (int) (this.getEndingLineIndex() ^ 
                (this.getEndingLineIndex() >>> 32));
            hash = 59 * hash + Objects.hashCode(this.getBody());
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
            final ElseIf other = (ElseIf) obj;
            if (!Objects.equals(this.condition, other.condition))
            {
                return false;
            }
            return true;
        }
    }

    public static class Else extends Block
    {

        public Else(long startingLineIndex)
        {
            super(startingLineIndex);
        }

        @Override
        public boolean needsExtraSpace()
        {
            // the visitIf itself adds extra whitespace
            return false;
        }
        //.equals and .hashCode from Block should still work for this class
    }

    public If(Expression condition, long startingLineIndex)
    {
        super(startingLineIndex);
        this.condition  = condition;
        elseIfs         = new ArrayList<>();
        else_           = null;
    }

    public Expression getCondition()
    {
        return condition;
    }

    public void addElseIf(ElseIf elseIf)
    {
        elseIfs.add(elseIf);
    }

    public ArrayList<ElseIf> getElseIfs()
    {
        return elseIfs;
    }

    public void setElse(Else else_)
    {
        this.else_ = else_;
    }

    public Block getElse()
    {
        return else_;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + (int) (this.getStartingLineIndex() ^ 
            (this.getStartingLineIndex() >>> 32));
        hash = 53 * hash + (int) (this.getEndingLineIndex() ^ 
            (this.getEndingLineIndex() >>> 32));
        hash = 53 * hash + Objects.hashCode(this.getBody());
        hash = 53 * hash + Objects.hashCode(this.condition);
        hash = 53 * hash + Objects.hashCode(this.elseIfs);
        hash = 53 * hash + Objects.hashCode(this.else_);
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
        final If other = (If) obj;
        if (!Objects.equals(this.condition, other.condition))
        {
            return false;
        }
        if (!Objects.equals(this.elseIfs, other.elseIfs))
        {
            return false;
        }
        if (!Objects.equals(this.else_, other.else_))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitIf(this);
    }

    @Override
    public boolean needsExtraSpace()
    {
        // the visitIf itself adds extra whitespace
        return false;
    }
    
}
