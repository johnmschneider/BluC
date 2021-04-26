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
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class ClassDef extends Block
{
    /**
     * Represents a class that wasn't yet defined by the parser.
     *  Used as an error flag -- if we find a class whose name is
     *  this, then it's an error in the compiler.
     * 
     * Stands for Blu C No Class
     */
    public static final String      NOT_DEFINED      = "__blucNC";
    public static final ClassDef    OBJECT_BASE_CLASS  =
        new ClassDef("Object", "bluc.lang", Statement.NO_STARTING_LINE_INDEX);
    
    private Token   className;
    private Token   baseClass;
    private String  classID;
    
    // TODO - why didn't I just make this constructor a private method?
    /**
     * Helper constructor for other constructors.
     */
    private ClassDef(long startingLineIndex)
    {
        super(startingLineIndex);
        this.baseClass  = null;
        this.classID    = NOT_DEFINED;
    }

    // TODO - change all ClassDef instances over to use this constructor
    //  (after packages are implemented)
    private ClassDef(Token className, String package_, long startingLineIndex)
    {
        this(startingLineIndex);
        this.className = 
            new Token(
                new TokenInfo(className.getTextContent(), 
                    false), 
                new TokenFileInfo(className.getFilepath(), 
                    className.getLineIndex()), 
                package_);
        
        this.classID = package_ + "_" + className.getTextContent();
    }
    
    // For use for base class object
    private ClassDef(String className, String package_, long startingLineIndex)
    {
        this(new Token(
                new TokenInfo(package_ + "_" + className, 
                    false),
                new TokenFileInfo("ClassDef.java", 
                    (int) startingLineIndex), 
                package_),
            package_,
            startingLineIndex);
    }
    
    // TODO - this constructor is to be deprecated as soon as packages are
    //      implemented.
    @Deprecated
    public ClassDef(Token className, long startingLineIndex)
    {
        this(startingLineIndex);
        this.className = className;
        this.classID = className.getTextContent();
    }

    public Token getClassName()
    {
        return className;
    }

    public String getClassNameText()
    {
        return className.getTextContent();
    }

    public void setBaseClass(Token baseClass)
    {
        this.baseClass = baseClass;
    }

    public String getClassID()
    {
        return classID;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitClassDef(this);
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
        final ClassDef other = (ClassDef) obj;
        if (!this.classID.equals(other.classID))
        {
            return false;
        }
        if (!Objects.equals(this.className, other.className))
        {
            return false;
        }
        if (!Objects.equals(this.baseClass, other.baseClass))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.className);
        hash = 19 * hash + Objects.hashCode(this.baseClass);
        hash = 19 * hash + Objects.hashCode(this.classID);
        hash = 19 * hash + (int) (this.getStartingLineIndex() ^ 
            (this.getStartingLineIndex() >>> 32));
        hash = 19 * hash + (int) (this.getEndingLineIndex() ^ 
            (this.getEndingLineIndex() >>> 32));
        return hash;
    }
    
}
