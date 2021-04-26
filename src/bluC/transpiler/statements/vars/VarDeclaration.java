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
package bluC.transpiler.statements.vars;

import bluC.transpiler.Expression;
import bluC.transpiler.Token;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.blocks.ClassDef;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class VarDeclaration extends Statement
{
    public static final Token       NO_ASSIGNMENT = null;
    public static final Expression  NO_VALUE = null;
    
    /**
     * The ID of a variable that holds plain-old-data.
     */
    public static final String  RETURN_VAR_NAME = "";
    
    /**
     * How many indirections (asterisks) are declared for this variable
     */
    private final int            pointerLevel;
    private final Sign           sign;
    private final SimplifiedType simplifiedType;
    private final Token          varName;
    private final Token          assignmentOperator;
    private final Expression     value;
    
    /**
     * If the SimplifiedType is CLASS, then this is set to the classID,
  otherwise it's ClassDef.NOT_DEFINED
     */
    private String classID = ClassDef.NOT_DEFINED;
    
    public VarDeclaration(Sign sign, SimplifiedType simplifiedType, 
        int pointerLevel, Token varName, Token assignmentOperator,
        Expression value, long startingLineIndex)
    {
        super(startingLineIndex);
        this.sign               = sign;
        this.simplifiedType     = simplifiedType;
        this.pointerLevel       = pointerLevel;
        this.varName            = varName;
        this.value              = value;
        this.assignmentOperator = assignmentOperator;
    }

    public Sign getSign()
    {
        return sign;
    }

    public SimplifiedType getSimplifiedType()
    {
        return simplifiedType;
    }

    public int getPointerLevel()
    {
        return pointerLevel;
    }

    public Token getName()
    {
        return varName;
    }

    public String getNameText()
    {
        return varName.getTextContent();
    }

    public Expression getValue()
    {
        return value;
    }

    public Token getAssignmentOperator()
    {
        return assignmentOperator;
    }

    public boolean isReturnVar()
    {
        return getNameText().equals(RETURN_VAR_NAME);
    }

    public void setClassID(String classID)
    {
        this.classID = classID;
    }

    public String getClassID()
    {
        return classID;
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
        final VarDeclaration other = (VarDeclaration) obj;
        if (this.pointerLevel != other.pointerLevel)
        {
            return false;
        }
        if (this.classID != other.classID)
        {
            return false;
        }
        if (this.sign != other.sign)
        {
            return false;
        }
        if (this.simplifiedType != other.simplifiedType)
        {
            return false;
        }
        if (!Objects.equals(this.varName, other.varName))
        {
            return false;
        }
        if (!Objects.equals(this.assignmentOperator, other.assignmentOperator))
        {
            return false;
        }
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 23 * hash + this.pointerLevel;
        hash = 23 * hash + Objects.hashCode(this.sign);
        hash = 23 * hash + Objects.hashCode(this.simplifiedType);
        hash = 23 * hash + Objects.hashCode(this.varName);
        hash = 23 * hash + Objects.hashCode(this.assignmentOperator);
        hash = 23 * hash + Objects.hashCode(this.value);
        hash = 23 * hash + Objects.hashCode(this.classID);
        hash = 23 * hash + (int) (this.getStartingLineIndex() ^
            (this.getStartingLineIndex() >>> 32));
        hash = 23 * hash + (int) (this.getEndingLineIndex() ^
            (this.getEndingLineIndex() >>> 32));
        return hash;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitVarDeclaration(this);
    }
    
}
