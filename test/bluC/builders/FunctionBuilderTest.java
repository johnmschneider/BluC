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
import bluC.transpiler.statements.ParameterList;
import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.Token;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author John Schneider
 */
public class FunctionBuilderTest
{
    private FunctionBuilder testThis;
    
    @Before
    public void setUp()
    {
        testThis = new FunctionBuilder();
    }
    
    @After
    public void tearDown()
    {
    }

    
    @Test
    public void testGetFileName()
    {
        String fileName = "FunctionBuilderTest_testGetFileName.bluc";
        testThis.setFileName(fileName);
        
        assertEquals((Object) fileName, (Object) testThis.getFileName());
    }

    @Test
    public void testSetFileName()
    {
        String fileName = "FunctionBuilderTest_testSetFileName.bluc";
        testThis.setFileName(fileName);
        
        assertEquals((Object) fileName, (Object) testThis.getFileName());
    }
    
    private VarDeclaration createReturnVar(Sign sign, SimplifiedType type)
    {
        String          fileName;
        int             startingLineIndex;
        String          varName;
        VarDeclarationBuilder 
                        varBuilder;
        VarDeclaration  returnVar;
        
        fileName            = "FunctionBuilderTest_createReturnVar.bluc";
        startingLineIndex   = 22;
        
        /**
         * Should be the same as VarDaclaration.RETURN_VAR_NAME. Purposely not
         *  using the constant to make sure any changes to it are explicit.
         */
        varName = "";
        
        varBuilder  = new VarDeclarationBuilder();
        returnVar   = varBuilder.
            setFileName         (fileName).
            setStartingLineIndex(startingLineIndex).
            setVarName          (varName).
            setSignedness       (sign).
            setSimplifiedType   (type).
            build();
        
        return returnVar;
    }
    
    @Test
    public void testGetReturnType()
    {
        Sign            sign;
        SimplifiedType  type;
        VarDeclaration  expected;
        
        sign        = Sign.UNSPECIFIED;
        type        = SimplifiedType.LONG_DOUBLE;
        expected    = createReturnVar(sign, type);
        testThis.setReturnType(expected);
        
        assertEquals(expected, testThis.getReturnType());
    }

    @Test
    public void testSetReturnType()
    {
        Sign            sign;
        SimplifiedType  type;
        VarDeclaration  expected;
        
        sign        = Sign.UNSIGNED;
        type        = SimplifiedType.LONG_LONG;
        expected    = createReturnVar(sign, type);
        testThis.setReturnType(expected);
        
        assertEquals(expected, testThis.getReturnType());
    }

    @Test
    public void testGetFunctionName()
    {
        String expected = "a_good_func_name";
        testThis.setFunctionName(expected);
        
        assertEquals((Object) expected, (Object) testThis.getFunctionName());
    }

    @Test
    public void testSetFunctionName()
    {
        String expected = "aNiceFuncName";
        testThis.setFunctionName(expected);
        
        assertEquals((Object) expected, (Object) testThis.getFunctionName());
    }

    @Test
    public void testGetParams()
    {
        ParameterList   expected;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration  param1;
        VarDeclaration  param2;
        int             mockLineIndex;
        
        mockLineIndex   = 264;
        varBuilder      = new VarDeclarationBuilder();
        
        param1          = varBuilder.
            setFileName         ("FunctionBuilderTest_testGetParams"). 
            setStartingLineIndex(mockLineIndex).
            setVarName          ("paramA").
            setSimplifiedType   (SimplifiedType.CHAR).
            build();
        param2          = varBuilder.
            setVarName          ("paramB").
            setSimplifiedType   (SimplifiedType.FLOAT).
            build();
        
        expected    = new ParameterList(mockLineIndex);
        expected.addParameter(param1);
        expected.addParameter(param2);
        
        testThis.setParams(expected);
        
        assertEquals(expected, testThis.getParams());
    }

    @Test
    public void testSetParams()
    {
        ParameterList   expected;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration  param1;
        VarDeclaration  param2;
        int             mockLineIndex;
        
        mockLineIndex   = 264;
        varBuilder      = new VarDeclarationBuilder();
        
        param1          = varBuilder.
            setFileName         ("FunctionBuilderTest_testSetParams"). 
            setStartingLineIndex(mockLineIndex).
            setVarName          ("parmesanA").
            setSimplifiedType   (SimplifiedType.DOUBLE).
            build();
        param2          = varBuilder.
            setVarName          ("parmesanB").
            setSimplifiedType   (SimplifiedType.DOUBLE).
            build();
        
        expected    = new ParameterList(mockLineIndex);
        expected.addParameter(param1);
        expected.addParameter(param2);
        
        testThis.setParams(expected);
        
        assertEquals(expected, testThis.getParams());
    }
    
    @Test
    public void testGetStartingLineIndex()
    {
        int expected = 242;
        testThis.setStartingLineIndex(expected);
        
        assertEquals(expected, testThis.getStartingLineIndex());
    }

    @Test
    public void testSetStartingLineIndex()
    {
        int expected = 425;
        testThis.setStartingLineIndex(expected);
        
        assertEquals(expected, testThis.getStartingLineIndex());
    }
    
    @Test
    public void testBuild()
    {
        String          fileName;
        int             lineIndex;
        String          funcName;
        
        VarDeclaration  returnType;
        ParameterList   params;
        Function        expected;
        Function        actual;
        
        lineIndex   = 1719;
        fileName    = "FunctionBuilderTest_testBuild";
        funcName    = "testBuild";
        
        returnType  = createReturnTypeForTestBuild  (fileName, lineIndex);
        params      = createParamsForTestBuild      (lineIndex);
        
        expected    = createFuncForTestBuild(
            fileName, lineIndex, funcName, returnType, params);
        actual      = testThis.
            setFileName         (fileName). 
            setStartingLineIndex(lineIndex).
            setFunctionName     (funcName).
            setReturnType       (returnType).
            setParams           (params).
            build();
        
        assertEquals(expected, actual);
    }
    
    private VarDeclaration createReturnTypeForTestBuild(
        String fileName, int lineIndex)
    {
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration  returnType;
        
        varBuilder      = new VarDeclarationBuilder();
        returnType      = varBuilder.
            setFileName         (fileName). 
            setStartingLineIndex(lineIndex).
            setSignedness       (Sign.SIGNED). 
            setPointerLevel     (2).
            build();
        
        return returnType;
    }
    
    private Function createFuncForTestBuild(
        String fileName, int lineIndex, String funcName, 
        VarDeclaration returnType, ParameterList params)
    {
        TokenBuilder    tokBuilder;
        Token           funcNameTok;
        
        Function        expected;
        
        tokBuilder  = new TokenBuilder();
        funcNameTok = tokBuilder.
            setFileName     (fileName).
            setLineIndex    (lineIndex). 
            setTextContent  (funcName). 
            build();
        
        expected = new Function(returnType, funcNameTok, lineIndex);
        expected.setParameters(params);
        
        return expected;
    }
    
    private ParameterList createParamsForTestBuild(int lineIndex)
    {
        ParameterList   params;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration  param1;
        VarDeclaration  param2;
        
        varBuilder      = new VarDeclarationBuilder();
        param1          = varBuilder.
            setVarName          ("oreganoA").
            setSimplifiedType   (SimplifiedType.CHAR).
            setSignedness       (Sign.UNSPECIFIED).
            build();
        param2          = varBuilder.
            setVarName          ("oreganoB").
            setSimplifiedType   (SimplifiedType.LONG_DOUBLE).
            build();
        
        params = new ParameterList(lineIndex);
        params.addParameter(param1);
        params.addParameter(param2);
        
        return params;
    }
}
