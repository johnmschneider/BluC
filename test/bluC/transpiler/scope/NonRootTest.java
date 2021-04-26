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
package bluC.transpiler.scope;

import bluC.builders.FunctionBuilder;
import bluC.builders.TokenBuilder;
import bluC.builders.VarDeclarationBuilder;
import bluC.transpiler.statements.blocks.Block;
import bluC.transpiler.Expression;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.blocks.If;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.blocks.Function;
import bluC.transpiler.Token;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.statements.vars.VarDeclaration;
import static bluC.transpiler.scope.ScopeTestUtils.createRootScope;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author John Schneider
 */
public class NonRootTest
{
    @Test
    public void testGetScopeType()
    {
        System.out.println("ScopeTest.getScopeType:");

        Scope           root;
        Scope           nextScope;
        Block expectedType;
        String          expectedTypeString;
        Statement       actualType;
        String          actualTypeString;

        root                = createRootScope();
        expectedType        = new Block(-1);
        expectedTypeString  = expectedType.getClass().getTypeName();

        nextScope           = new Scope(root, new Block(-1));
        actualType          = nextScope.getScopeType();
        actualTypeString    = actualType.getClass().getTypeName();

        assertEquals(expectedTypeString, actualTypeString);
    }

    @Test
    public void testGetParent()
    {
        Scope root;

        /**
         * These variables below could be anything, but set them to
         * something specific so we have a clear reason why the test might
         * fail.
         */
        int                 arbitraryLineNumber;
        If                  arbitraryBlockForScope;
        Expression.Unary    arbitraryExpressionForIf;

        Scope nextScope;
        Scope expectedParent;
        Scope actualParent;

        arbitraryLineNumber         = 1;
        arbitraryExpressionForIf    = createMockUnaryExpression(
                "++", false, "parentTest");
        arbitraryBlockForScope      = new If(
                arbitraryExpressionForIf, arbitraryLineNumber);

        root        = createRootScope();
        nextScope   = new Scope(root, arbitraryBlockForScope);

        assertEquals(root, nextScope.getParent());
    }

    private Expression.Unary createMockUnaryExpression(
        String operator, boolean operatorIsOnRight, 
        String operandVariableName)
    {
        String          mockFileName    = "ScopeTest_NonRootTest.java";
        int             mockLineIndex   = 2;

        Token           mockOp;
        TokenBuilder    tokenBuilder;
        VarDeclarationBuilder 
                        varBuilder;
        VarDeclaration
                        mockVarDecl;

        tokenBuilder    = new TokenBuilder();
        mockOp          = tokenBuilder.
            setFileName     (mockFileName).
            setLineIndex    (mockLineIndex).
            setTextContent  (operator).
            build();

        varBuilder  = new VarDeclarationBuilder();
        mockVarDecl = varBuilder.
            setFileName         (mockFileName).
            setStartingLineIndex(mockLineIndex).
            setSignedness       (Sign.SIGNED).
            setVarName          (operandVariableName).
            setSimplifiedType   (SimplifiedType.INT).
            build();

        Expression.Variable mockVarUsage        = new Expression.Variable(
            mockVarDecl);
        Expression.Unary mockUnaryExpression    = new Expression.Unary(
            mockOp, mockVarUsage, operatorIsOnRight);

        return mockUnaryExpression;
    }
    
    @Test
    public void testToString()
    {
        Scope           root;
        Scope           oneLevelDown;
        String          expectedToString;
        
        String          fileName;
        int             lineIndex;
        FunctionBuilder funcBuilder;
        Function        func;
        
        fileName    = "VariableUnrelatedTest_testToString_notRootScope";
        lineIndex   = 5543;
        
        funcBuilder = new FunctionBuilder();
        func        = funcBuilder.
            setFileName(fileName).
            setStartingLineIndex(lineIndex).
            setFunctionName("scopeToStringTest").
            build();
        
        expectedToString    = "ROOT_SCOPE.\nscopeToStringTest.\n";
        root                = createRootScope();
        oneLevelDown        = new Scope(root, func);
        
        assertEquals(
            (Object) expectedToString, (Object)  oneLevelDown.toString());
    }  
}