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

import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Package extends Statement
{
    public static final String NO_PACKAGE = null;
    
    /**
     * JS 3/24/2021 : I don't think this is used anywhere but I'm leaving it
     *  in until the package class has been implemented, just in case this
     *  was a planned future refactoring.
     */
    //public static final int     NO_LINE_INDEX = -1;
    private String fullyQualifiedPackageName;

    public Package(String fullyQualifiedPackageName, long startingLineIndex)
    {
        super(startingLineIndex);
        this.fullyQualifiedPackageName = fullyQualifiedPackageName;
    }

    public String getFullyQualifiedPackageName()
    {
        return fullyQualifiedPackageName;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.fullyQualifiedPackageName);
        hash = 41 * hash + (int) (this.getStartingLineIndex() ^
            (this.getStartingLineIndex() >>> 32));
        hash = 41 * hash + (int) (this.getEndingLineIndex() ^
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
        final Package other = (Package) obj;
        if (!Objects.equals(this.fullyQualifiedPackageName,
            other.fullyQualifiedPackageName))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitPackage(this);
    }
    
}
