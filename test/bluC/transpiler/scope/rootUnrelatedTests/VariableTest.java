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
package bluC.transpiler.scope.rootUnrelatedTests;

import bluC.builders.VarDeclarationBuilder;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.statements.vars.VarDeclaration;
import static bluC.transpiler.scope.ScopeTestUtils.createRootScope;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author John Schneider
 */
public class VariableTest
{
    @Test
    public void testGetVariablesInThisScope_varCountIsCorrect()
    {
        Scope           root;
        String          mockFileName;
        int             mockLineIndex;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration 
                        mockVarDecl;
        VarDeclaration 
                        mockVarDecl2;

        root            = createRootScope();
        mockFileName    = "ScopeTest_RootUnrelatedTests.java";
        mockLineIndex   = 2;

        varBuilder      = new VarDeclarationBuilder();
        varBuilder.
            setFileName(mockFileName).
            setStartingLineIndex(mockLineIndex);

        mockVarDecl     = varBuilder. 
            setVarName("varTest").
            setSimplifiedType(SimplifiedType.LONG).
            setSignedness(Sign.UNSIGNED).
            build();

        mockVarDecl2    = varBuilder.
            setVarName("varTest2").
            setSimplifiedType(SimplifiedType.CHAR).
            setSignedness(Sign.UNSPECIFIED).
            build();

        root.addVariableToScope(mockVarDecl);
        root.addVariableToScope(mockVarDecl2);

        int             variableCount;
        ArrayList<VarDeclaration>
                        variables;

        variables       = root.getVariablesInThisScope();
        variableCount   = variables.size();

        assertEquals(2, variableCount);
    }

    @Test
    public void testGetVariablesInThisScope_varTypeIsCorrect()
    {
        Scope           root;
        String          mockFileName;
        int             mockLineIndex;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration 
                        mockVarDecl;
        VarDeclaration 
                        mockVarDecl2;

        root            = createRootScope();
        mockFileName    = "ScopeTest_RootUnrelatedTests.java";
        mockLineIndex   = 2;

        varBuilder      = new VarDeclarationBuilder();
        varBuilder.
            setFileName(mockFileName).
            setStartingLineIndex(mockLineIndex);

        mockVarDecl     = varBuilder. 
            setVarName("typeTest").
            setSimplifiedType(SimplifiedType.CHAR).
            setSignedness(Sign.UNSIGNED).
            build();

        mockVarDecl2    = varBuilder.
            setVarName("typeTestNo2").
            setSimplifiedType(SimplifiedType.DOUBLE).
            setSignedness(Sign.UNSPECIFIED).
            build();

        root.addVariableToScope(mockVarDecl);
        root.addVariableToScope(mockVarDecl2);

        int             variableCount;
        ArrayList<VarDeclaration>
                        variables;

        variables       = root.getVariablesInThisScope();

        assertEquals(mockVarDecl, variables.get(0));
        assertEquals(mockVarDecl2, variables.get(1));
    }

    @Test
    public void testAddVariableToScope()
    {
        Scope           root;
        String          mockFileName;
        int             mockLineIndex;
        VarDeclarationBuilder
                        varBuilder;
        VarDeclaration 
                        mockVarDecl;
        VarDeclaration 
                        mockVarDecl2;

        root            = createRootScope();
        mockFileName    =
            "ScopeTest_RootUnrelatedTests_testAddVariableToScope" +
            ".java";
        mockLineIndex   = 2;

        varBuilder      = new VarDeclarationBuilder();
        varBuilder.
            setFileName(mockFileName).
            setStartingLineIndex(mockLineIndex);

        mockVarDecl     = varBuilder. 
            setVarName("scopeTest01").
            setSimplifiedType(SimplifiedType.LONG).
            setSignedness(Sign.UNSIGNED).
            build();

        mockVarDecl2    = varBuilder.
            setVarName("scopeTest02").
            setSimplifiedType(SimplifiedType.CHAR).
            setSignedness(Sign.SIGNED).
            build();

        root.addVariableToScope(mockVarDecl);
        root.addVariableToScope(mockVarDecl2);

        int             variableCount;
        ArrayList<VarDeclaration>
                        variables;

        variables       = root.getVariablesInThisScope();

        assertEquals(mockVarDecl, variables.get(0));
        assertEquals(mockVarDecl2, variables.get(1));
    }
}
