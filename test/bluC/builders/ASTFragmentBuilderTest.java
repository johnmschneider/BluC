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

import bluC.builders.ASTFragmentBuilder;
import bluC.builders.VarDeclarationBuilder;
import bluC.parser.Parser;
import bluC.transpiler.Expression.Literal;
import bluC.transpiler.Lexer;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.Token;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.statements.vars.VarDeclaration;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author John Schneider
 */
public class ASTFragmentBuilderTest
{
    private ASTFragmentBuilder testThis;

    @Before
    public void setup()
    {
        testThis = new ASTFragmentBuilder();
    }

    @Test
    public void testGetFilePath()
    {
        String testPath = this.getClass().getTypeName() + ".java";
        testThis.setFilePath(testPath);
        
        /**
         * Explicitly cast the strings to Object because netbeans was
         *  having difficulty distinguishing between the
         *  (string, object, object) and (object, object) overload of this
         *  method.
         *
         * assertEquals(Object, Object) calls .equal on the objects, not ==.
         */
        assertEquals((Object) testPath, (Object) testThis.getFilePath());
    }

    @Test
    public void testSetFilePath()
    {
        String testPath = "Abc_Xyz.bluC";
        testThis.setFilePath(testPath);
        
        /**
         * Explicitly cast the strings to Object because netbeans was
         *  having difficulty distinguishing between the
         *  (string, object, object) and (object, object) overload of this
         *  method.
         *
         * assertEquals(Object, Object) calls .equal on the objects, not ==.
         */
        assertEquals((Object) testPath, (Object) testThis.getFilePath());
    }

    @Test
    public void testGetFileContents()
    {
        String testFileContents = 
            "int a = 2;\n" +
            "unsigned long long bigInt = 3;";
        
        testThis.setFileContents(testFileContents);
        assertEquals(
            (Object) testFileContents, (Object) testThis.getFileContents());
    }

    @Test
    public void testSetFileContents()
    {
        String testFileContents =
            "int a = 2;\n" + 
            "unsigned long long bigInt = 3;";
        testThis.setFileContents(testFileContents);
        
        assertEquals(
            (Object) testFileContents, (Object) testThis.getFileContents());
    }

    @Test
    public void testBuild_astLengthMatches()
    {
        String                  testFileName = "_42.bluc";
        String                  testFileContents;
        ArrayList<Statement>    ast;
        
        testFileContents    = "unsigned long long bigTest = 44;";
        ast                 = testThis.
            setFileContents(testFileContents).
            build();
        
        assertEquals(1, ast.size());
    }

    @Test
    public void testBuild_astSingleNodeTypeMatches()
    {
        String      testFilePath = "acdbaa.bluc";
        String      testFileContents;
        ArrayList<Statement> 
                    ast;
        VarDeclarationBuilder 
                    varBuilder;
        VarDeclaration 
                    expectedAstNode0;
        TokenBuilder
                    tokenBuilder;
        Token       charValue;
        Literal     charValueLit;
        
        testFileContents    = "char charTest = 'x';";
        ast                 = testThis.
            setFilePath(testFilePath).
            setFileContents(testFileContents).
            build();
        
        tokenBuilder    = new TokenBuilder();
        charValue       = tokenBuilder.
            setFileName     (testFilePath).
            setLineIndex    (0).
            setTextContent  ("'x'"). 
            build();
        charValueLit = new Literal(charValue);
        
        varBuilder          = new VarDeclarationBuilder();
        expectedAstNode0    = varBuilder.
            setFileName         (testFilePath).
            setStartingLineIndex(0).
            setVarName          ("charTest").
            setSimplifiedType   (SimplifiedType.CHAR).
            setInitialValue     (charValueLit).
            build();
        
        assertEquals(expectedAstNode0, ast.get(0));
    }

    @Test
    public void testGetLexer()
    {
        String  testFilePath = "a42dbc";
        String  fileContents;
        ArrayList<String> 
                fileContentsAsArrayList;
        Lexer   expectedResult;
        
        fileContents            = "long int c = 44;";
        fileContentsAsArrayList = new ArrayList<>();
        fileContentsAsArrayList.add(fileContents);
        
        expectedResult = new Lexer(testFilePath, fileContentsAsArrayList);
        expectedResult.lex();
        
        testThis.
            setFilePath     (testFilePath).
            setFileContents (fileContents).
            build();
        assertEquals(expectedResult, testThis.getLexer());
    }

    @Test
    public void testGetParser()
    {
        String  testFilePath = "a42dbc";
        String  fileContents;
        ArrayList<String> 
                fileContentsAsArrayList;
        Lexer   lexer;
        ArrayList<Token> 
                lexedTokens;
        Parser  expectedResult;
        
        fileContents            = "long int c = 44;";
        fileContentsAsArrayList = new ArrayList<>();
        fileContentsAsArrayList.add(fileContents);
        
        lexer       = new Lexer(testFilePath, fileContentsAsArrayList);
        lexedTokens = lexer.lex();
        
        expectedResult = new Parser(lexedTokens);
        expectedResult.parse();
        
        testThis.
            setFilePath(testFilePath).
            setFileContents(fileContents).
            build();
        
        assertEquals(expectedResult, testThis.getParser());
    }
    
}
