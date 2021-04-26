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

import bluC.transpiler.statements.blocks.Function;
import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.statements.ParameterList;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.Token;

/**
 * Builds a function.
 * 
 * Requires at least these parameters:
 *  - fileName
 *  - startingLineIndex
 *  - functionName
 * 
 * @author John Schneider
 */
public class FunctionBuilder
{
    private String          fileName;
    private VarDeclaration  returnType;
    private String          functionName;
    private ParameterList   params;
    private int             startingLineIndex;
    
    public FunctionBuilder()
    {
        initDefaultReturnType();
    }
    
    private void initDefaultReturnType()
    {
        VarDeclarationBuilder varBuilder;
        
        varBuilder = new VarDeclarationBuilder();
        returnType = varBuilder.
            setFileName         (fileName).
            setStartingLineIndex(startingLineIndex).
            setVarName          (VarDeclaration.RETURN_VAR_NAME).
            setSimplifiedType   (SimplifiedType.VOID). 
            build();
    }
    
    
    public String getFileName()
    {
        return fileName;
    }

    public FunctionBuilder setFileName(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    public VarDeclaration getReturnType()
    {
        return returnType;
    }

    public FunctionBuilder setReturnType(VarDeclaration returnType)
    {
        this.returnType = returnType;
        return this;
    }

    public String getFunctionName()
    {
        return functionName;
    }

    public FunctionBuilder setFunctionName(String functionName)
    {
        this.functionName = functionName;
        return this;
    }

    public ParameterList getParams()
    {
        return params;
    }

    public FunctionBuilder setParams(ParameterList params)
    {
        this.params = params;
        return this;
    }

    public int getStartingLineIndex()
    {
        return startingLineIndex;
    }

    public FunctionBuilder setStartingLineIndex(int startingLineIndex)
    {
        this.startingLineIndex = startingLineIndex;
        return this;
    }
    
    public Function build()
    {
        TokenBuilder    tokenBuilder;
        Token           funcNameToken;
        Function        result;
        
        tokenBuilder    = new TokenBuilder();
        funcNameToken   = tokenBuilder.
            setFileName     (fileName).
            setLineIndex    (startingLineIndex).
            setTextContent  (functionName).
            build();
        
        result = new Function(returnType, funcNameToken, startingLineIndex);
        result.setParameters(params);
        
        return result;
    }
}
