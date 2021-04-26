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

package bluC.builders;

import bluC.transpiler.Expression;
import bluC.transpiler.Token;
import bluC.transpiler.statements.blocks.ClassDef;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.statements.vars.VarDeclaration;

/**
 * Builds a variable declaration based on the parameters.
 * 
 * At a minimum, these variables must be specified:
 *  - fileName
 *  - startingLineIndex
 *  - varName
 *  - simplifiedType
 * 
 * @author John Schneider
 */
public class VarDeclarationBuilder
{
    /**
     * (Absolute or relative) (file path + file name) of the file the statement
     *      is contained in.
     */
    private String          fileName;
    
    /**
     * The earliest line index that the first token of this statement can be
     *  found on.
     */
    private int             startingLineIndex;
    private String          varName;
    private boolean         wasEmittedByCompiler;
    
    private Sign            signedness;
    private SimplifiedType  simplifiedType;
    private int             pointerLevel;
    
    /** Token varName is stored in superclass */
    
    private Token           assignmentOperator;
    
    /**
     * Optional parameter, if no assignment is present then
     *  it's equal to VarDeclaration.NO_VALUE.
     * 
     * Represents the value assigned to a variable on declaration.
     */
    private Expression initialValue;
    
    /**
     * Optional parameter. ID of the class if this is a class, otherwise it's
  ClassDef.NOT_DEFINED
     */
    private String       classID;

    public VarDeclarationBuilder()
    {
        initTokenDefaults();
        initAssignmentDefaults();
        initClassDefaults();
        initVarDefaults();
    }
    
    private void initTokenDefaults()
    {
        wasEmittedByCompiler    = false;
    }
    
    private void initAssignmentDefaults()
    {
        initialValue        = VarDeclaration.NO_VALUE;
        assignmentOperator  = VarDeclaration.NO_ASSIGNMENT;
    }
    
    private void initClassDefaults()
    {
        classID = ClassDef.NOT_DEFINED;
    }
    
    private void initVarDefaults()
    {
        signedness      = Sign.UNSPECIFIED;
        pointerLevel    = 0;
    }
    
    
    public Expression getInitialValue()
    {
        return initialValue;
    }
    
    /**
     * Sets only the initial value
     */
    public VarDeclarationBuilder setJustInitialValue(Expression initialValue)
    {
        this.initialValue = initialValue;
        return this;
    }
    
    /**
     * Sets both the initial value, then creates an
     *  assignmentOperator token and assigns it to
     *  this builder automatically.
     */
    public VarDeclarationBuilder setInitialValue(Expression initialValue)
    {
        TokenBuilder tokBuilder;
        Token        assignmentOp;
        
        tokBuilder      = new TokenBuilder();
        assignmentOp    = tokBuilder.
            setFileName     (getFileName()).
            setLineIndex    (getStartingLineIndex()).
            setTextContent  ("=").
            build();
        this.setAssignmentOperator(assignmentOp);
        
        this.initialValue = initialValue;
        return this;
    }
    
    public String getFileName()
    {
        return fileName;
    }

    public VarDeclarationBuilder setFileName(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    public int getStartingLineIndex()
    {
        return startingLineIndex;
    }

    public VarDeclarationBuilder setStartingLineIndex(int startingLineIndex)
    {
        this.startingLineIndex = startingLineIndex;
        return this;
    }

    public String getVarName()
    {
        return varName;
    }

    public VarDeclarationBuilder setVarName(String varName)
    {
        this.varName = varName;
        return this;
    }

    public boolean getWasEmittedByCompiler()
    {
        return wasEmittedByCompiler;
    }

    public VarDeclarationBuilder setWasEmittedByCompiler(
        boolean wasEmittedByCompiler)
    {
        this.wasEmittedByCompiler = wasEmittedByCompiler;
        return this;
    }

    public Sign getSignedness()
    {
        return signedness;
    }

    public VarDeclarationBuilder setSignedness(Sign signedness)
    {
        this.signedness = signedness;
        return this;
    }

    public SimplifiedType getSimplifiedType()
    {
        return simplifiedType;
    }

    public VarDeclarationBuilder setSimplifiedType(
        SimplifiedType simplifiedType)
    {
        this.simplifiedType = simplifiedType;
        return this;
    }

    public int getPointerLevel()
    {
        return pointerLevel;
    }

    public VarDeclarationBuilder setPointerLevel(int pointerLevel)
    {
        this.pointerLevel = pointerLevel;
        return this;
    }

    public Token getAssignmentOperator()
    {
        return assignmentOperator;
    }

    public VarDeclarationBuilder setAssignmentOperator(Token assignmentOperator)
    {
        this.assignmentOperator = assignmentOperator;
        return this;
    }
    
    public String getClassID()
    {
        return classID;
    }

    public VarDeclarationBuilder setClassID(String classID)
    {
        this.classID = classID;
        return this;
    }
    
    public VarDeclaration build()
    {
        Token           varNameToken;
        TokenBuilder    varNameBuilder;
        
        varNameBuilder = new TokenBuilder();
        
        varNameBuilder.
            setFileName(fileName).
            setLineIndex(startingLineIndex).
            setWasEmittedByCompiler(wasEmittedByCompiler).
            setTextContent(varName);
        
        varNameToken = varNameBuilder.build();
        
        return new VarDeclaration(signedness, simplifiedType, pointerLevel,
            varNameToken, assignmentOperator, initialValue, startingLineIndex);
    }
}
