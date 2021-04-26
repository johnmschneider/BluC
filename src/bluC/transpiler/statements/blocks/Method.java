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

import bluC.parser.Parser;
import bluC.transpiler.statements.ParameterList;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.transpiler.statements.vars.VarDeclaration;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Method extends Function
{
    private final ClassDef  class_;
    private final String    mangledName;
    private final Parser    parser;

    public Method(ClassDef class_, VarDeclaration returnVar, Token methodName,
        String mangledName, Parser parser, long startingLineIndex)
    {
        super(returnVar, methodName, startingLineIndex);
        this.class_ = class_;
        this.mangledName = mangledName;
        this.parser = parser;
    }

    @Override
    public void setParameters(ParameterList parameters)
    {
        long            startingLineIndex;
        ParameterList   listWithThis;
        ArrayList<VarDeclaration> 
                        rawParameters;
        
        startingLineIndex   = parameters.getStartingLineIndex();
        listWithThis        = new ParameterList(startingLineIndex);
        rawParameters       = parameters.getParameters();
        
        // to determine what file and line the "this" is on
        Token tokenBeforeTheThisKeyword;
        if (rawParameters.isEmpty())
        {
            // the "(" token
            tokenBeforeTheThisKeyword = parser.getCurToken();
        } else
        {
            tokenBeforeTheThisKeyword = rawParameters.get(0).getName();
        }
        
        VarDeclaration this_ = new VarDeclaration(Sign.UNSPECIFIED, 
            SimplifiedType.CLASS, 1, 
                new Token(
                    new TokenInfo("this", false), 
                    new TokenFileInfo(
                        tokenBeforeTheThisKeyword.getFilepath(), 
                        tokenBeforeTheThisKeyword.getLineIndex())),
                        null, null, tokenBeforeTheThisKeyword.getLineIndex());
        
        this_.setClassID(class_.getClassID());
        parser.getCurrentScope().addVariableToScope(this_);
        listWithThis.addParameter(this_);
        
        for (VarDeclaration parameter : parameters.getParameters())
        {
            listWithThis.addParameter(parameter);
        }
        super.setParameters(listWithThis);
    }

    public ClassDef getClass_()
    {
        return class_;
    }

    public String getMangledName()
    {
        return mangledName;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.class_);
        hash = 53 * hash + Objects.hashCode(this.mangledName);
        hash = 53 * hash + Objects.hashCode(this.parser);
        hash = 53 * hash + Objects.hashCode(this.getReturnType());
        hash = 53 * hash + Objects.hashCode(this.getParameters());
        hash = 53 * hash + Objects.hashCode(this.getNameToken());
        hash = 53 * hash + (int) (this.getStartingLineIndex() ^
            (this.getStartingLineIndex() >>> 32));
        hash = 53 * hash + (int) (this.getEndingLineIndex() ^
            (this.getEndingLineIndex() >>> 32));
        hash = 53 * hash + Objects.hashCode(this.getBody());
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
        final Method other = (Method) obj;
        if (!Objects.equals(this.mangledName, other.mangledName))
        {
            return false;
        }
        if (!Objects.equals(this.class_, other.class_))
        {
            return false;
        }
        if (!Objects.equals(this.parser, other.parser))
        {
            return false;
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor)
    {
        return visitor.visitMethod(this);
    }
    
}
