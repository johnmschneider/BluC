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

import bluC.transpiler.Expression.Literal;
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
public class VarDeclarationBuilderTest
{
    private VarDeclarationBuilder testThis;
    
    @Before
    public void setUp()
    {
        testThis = new VarDeclarationBuilder();
    }
    
    @After
    public void tearDown()
    {
    }

    
    @Test
    public void testSetJustInitialValue_returnsSelf()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        
        TokenBuilder    tokenBuilder;
        Token           litToken;
        Literal         expected;
        
        mockFileName    = "VarDeclarationBuilderTest_" +
            "testSetJustInitialValue_returnsSelf.bluc";
        mockLineIndex   = 179;
        mockTextContent = "45";
                
        tokenBuilder    = new TokenBuilder();
        litToken        = tokenBuilder. 
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (mockTextContent).
            build();
        expected = new Literal(litToken);
        
        assertEquals(testThis, testThis.setJustInitialValue(expected));
    }
    
    @Test
    public void testSetJustInitialValue_valueIsCorrect()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        
        TokenBuilder    tokenBuilder;
        Token           litToken;
        Literal         expected;
        
        mockFileName    = "VarDeclarationBuilderTest_" + 
            "testSetJustInitialValue_valueIsCorrect.bluc";
        mockLineIndex   = 173;
        mockTextContent = "\"abc\"";
                
        tokenBuilder    = new TokenBuilder();
        litToken        = tokenBuilder. 
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (mockTextContent).
            build();
        expected = new Literal(litToken);
        testThis.setJustInitialValue(expected);
        
        assertEquals(expected, testThis.getInitialValue());
    }
    
    @Test
    public void testGetInitialValue()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        
        TokenBuilder    tokenBuilder;
        Token           litToken;
        Literal         expected;
        
        mockFileName    = "VarDeclarationBuilderTest_testGetInitialValue1.bluc";
        mockLineIndex   = 325;
        mockTextContent = "44";
                
        tokenBuilder    = new TokenBuilder();
        litToken        = tokenBuilder. 
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (mockTextContent).
            build();
        expected = new Literal(litToken);
        testThis.setInitialValue(expected);
        
        assertEquals(expected, testThis.getInitialValue());
    }

    @Test
    public void testSetInitialValue_valueSet()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        
        TokenBuilder    tokenBuilder;
        Token           litToken;
        Literal         expected;
        
        mockFileName    = "VarDeclarationBuilderTest_testSetInitialValue1.bluc";
        mockLineIndex   = 4123;
        mockTextContent = "55";
                
        tokenBuilder    = new TokenBuilder();
        litToken        = tokenBuilder. 
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (mockTextContent).
            build();
        expected = new Literal(litToken);
        testThis.setInitialValue(expected);
        
        assertEquals(expected, testThis.getInitialValue());
    }
    
    @Test
    public void testSetInitialValue_assignmentOpSet()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        
        TokenBuilder    tokenBuilder;
        Token           litToken;
        Literal         literal;
        Token           expected;
        
        mockFileName    = "VarDeclarationBuilderTest_testSetInitialValue2.bluc";
        mockLineIndex   = 19237;
        mockTextContent = "'b'";
                
        tokenBuilder    = new TokenBuilder();
        litToken        = tokenBuilder. 
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (mockTextContent).
            build();
        literal = new Literal(litToken);
        
        /**
         * Make sure the .equals comparison of the two tokens matches
         */
        testThis.
            setFileName(mockFileName).
            setStartingLineIndex(mockLineIndex).
            setInitialValue(literal);
        
        expected        = tokenBuilder.
            setTextContent("=").
            build();
        
        assertEquals(expected, testThis.getAssignmentOperator());
    }

    @Test
    public void testGetFileName()
    {
        String mockFileName = "VarDeclarationBuilderTest_testGetFileName.bluc";
        testThis.setFileName(mockFileName);
        
        assertEquals(mockFileName, testThis.getFileName());
    }

    @Test
    public void testSetFileName()
    {
        String mockFileName = "A2BNaH__.bluc";
        testThis.setFileName(mockFileName);
        
        assertEquals(mockFileName, testThis.getFileName());
    }

    @Test
    public void testGetStartingLineIndex()
    {
        int mockStartIndex = 2352;
        testThis.setStartingLineIndex(mockStartIndex);
        
        assertEquals(mockStartIndex, testThis.getStartingLineIndex());
    }

    @Test
    public void testSetStartingLineIndex()
    {
        int mockStartIndex = 41261;
        testThis.setStartingLineIndex(mockStartIndex);
        
        assertEquals(mockStartIndex, testThis.getStartingLineIndex());
    }

    @Test
    public void testGetVarName()
    {
        String expected = "a_var";
        testThis.setVarName(expected);
        
        assertEquals(expected, testThis.getVarName());
    }

    @Test
    public void testSetVarName()
    {
        String expected = "var_the_sequel";
        testThis.setVarName(expected);
        
        assertEquals(expected, testThis.getVarName());
    }

    @Test
    public void testGetWasEmittedByCompiler()
    {
        testThis.setWasEmittedByCompiler(true);
        assertEquals(true, testThis.getWasEmittedByCompiler());
        
        testThis.setWasEmittedByCompiler(false);
        assertEquals(false, testThis.getWasEmittedByCompiler());
    }

    @Test
    public void testSetWasEmittedByCompiler()
    {
        testThis.setWasEmittedByCompiler(false);
        assertEquals(false, testThis.getWasEmittedByCompiler());
        
        testThis.setWasEmittedByCompiler(true);
        assertEquals(true, testThis.getWasEmittedByCompiler());
    }

    @Test
    public void testGetSignedness()
    {
        Sign expected = Sign.SIGNED;
        testThis.setSignedness(expected);
        
        assertEquals(expected, testThis.getSignedness());
    }

    @Test
    public void testSetSignedness()
    {
        Sign expected = Sign.UNSPECIFIED;
        testThis.setSignedness(expected);
        
        assertEquals(expected, testThis.getSignedness());
    }

    @Test
    public void testGetSimplifiedType()
    {
        SimplifiedType expected = SimplifiedType.CHAR;
        testThis.setSimplifiedType(expected);
        
        assertEquals(expected, testThis.getSimplifiedType());
    }

    @Test
    public void testSetSimplifiedType()
    {
        SimplifiedType expected = SimplifiedType.LONG_LONG;
        testThis.setSimplifiedType(expected);
        
        assertEquals(expected, testThis.getSimplifiedType());
    }

    @Test
    public void testGetPointerLevel()
    {
        // a 23-deep pointer is a bit much but I guess it's possible
        int expected = 23;
        testThis.setPointerLevel(expected);
        
        assertEquals(expected, testThis.getPointerLevel());
    }

    @Test
    public void testSetPointerLevel()
    {
        int expected = 5;
        testThis.setPointerLevel(expected);
        
        assertEquals(expected, testThis.getPointerLevel());
    }

    @Test
    public void testGetAssignmentOperator()
    {
        String          fileName;
        TokenBuilder    tokenBuilder;
        Token           expected;
        
        fileName = "VarDeclarationBuilderTest_testGetAssignmentOperator.bluc";
        
        tokenBuilder = new TokenBuilder();
        expected = tokenBuilder.
            setFileName     (fileName).
            setLineIndex    (22).
            setTextContent  ("=").
            build();
        testThis.setAssignmentOperator(expected);
        
        assertEquals(expected, testThis.getAssignmentOperator());
    }

    /**
     * Test of setAssignmentOperator method, of class VarDeclarationBuilder.
     */
    @Test
    public void testSetAssignmentOperator()
    {
        String          fileName;
        TokenBuilder    tokenBuilder;
        Token           expected;
        
        fileName = "VarDeclarationBuilderTest_testSetAssignmentOperator.bluc";
        
        tokenBuilder = new TokenBuilder();
        expected = tokenBuilder.
            setFileName     (fileName).
            setLineIndex    (3124).
            setTextContent  ("=").
            build();
        testThis.setAssignmentOperator(expected);
        
        assertEquals(expected, testThis.getAssignmentOperator());
    }

    @Test
    public void testGetClassID()
    {
        String expected = "Class22";
        testThis.setClassID(expected);
        
        assertEquals(expected, testThis.getClassID());
    }

    @Test
    public void testSetClassID()
    {
        String expected = "Class5217";
        testThis.setClassID(expected);
        
        assertEquals(expected, testThis.getClassID());
    }

    @Test
    public void testBuild()
    {
        String          fileName;
        int             lineIndex;
        String          varName;
        TokenBuilder    tokBuilder;
        Token           varNameToken;
        Sign            sign;
        SimplifiedType  type;
        
        VarDeclaration  expected;
        VarDeclaration  actual;
        
        fileName    = "VarDeclarationBuilderTest_testBuild.bluc";
        lineIndex   = 51256;
        varName     = "name_for_a_var";
        sign        = Sign.SIGNED;
        type        = SimplifiedType.INT;
        
        tokBuilder      = new TokenBuilder();
        varNameToken    = tokBuilder.
            setFileName     (fileName). 
            setLineIndex    (lineIndex).
            setTextContent  (varName).
            build();
        expected    = new VarDeclaration(
            sign, type, 0, varNameToken, VarDeclaration.NO_ASSIGNMENT,
            VarDeclaration.NO_VALUE, lineIndex);
        
        testThis  = new VarDeclarationBuilder();
        actual = testThis.
            setFileName         (fileName).
            setStartingLineIndex(lineIndex).
            setSignedness       (sign).
            setVarName          (varName).
            setSimplifiedType   (type).
            build();
        
        assertEquals(expected, actual);
    }
    
}
