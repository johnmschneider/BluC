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

import bluC.transpiler.statements.Statement;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Block extends Statement
{
    private ArrayList<Statement> body;

    public Block(long startingLineIndex)
    {
        super(startingLineIndex);
        this.body = new ArrayList<>();
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitBlock(this);
    }

    public final <T> T acceptBlock(Visitor<T> visitor)
    {
        return visitor.visitBlock(this);
    }

    public ArrayList<Statement> getBody()
    {
        return body;
    }

    public void addStatement(Statement statement)
    {
        body.add(statement);
    }

    @Override
    public boolean needsSemicolon()
    {
        return false;
    }

    public boolean needsExtraSpace()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.body);
        hash = 79 * hash + (int) (this.getStartingLineIndex() ^
            (this.getStartingLineIndex() >>> 32));
        hash = 79 * hash + (int) (this.getEndingLineIndex() ^
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
        final Block other = (Block) obj;
        if (!Objects.equals(this.body, other.body))
        {
            return false;
        }
        return true;
    }
    
}
