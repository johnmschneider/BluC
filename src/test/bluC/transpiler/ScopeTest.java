package bluC.transpiler;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 *
 * @author John Schneider
 */
@RunWith(Enclosed.class)
public class ScopeTest
{

    public static class RootTest
    {

        @Test
        public void testRootScopeGetScopeType()
        {
            Scope root = createRootScope();

            /**
             * Second parameter should be Scope.NO_SCOPE_TYPE. Not using the
             * defined constants to ensure changes to them are explicit.
             */
            assertEquals(root.getScopeType(), null);
        }

        @Test
        public void testRootGetParent()
        {
            Scope rootParentScope = createRootScope().getParent();
            assertEquals(rootParentScope, null);
        }
    }

    public static class NonRootTest
    {

        @Test
        public void testGetScopeTypeNonRoot()
        {
            System.out.println("ScopeTest.getScopeType:");

            Scope           root;
            Scope           nextScope;
            Statement.Block expectedType;
            String          expectedTypeString;
            Statement       actualType;
            String          actualTypeString;

            root                = createRootScope();
            expectedType        = new Statement.Block(-1);
            expectedTypeString  = expectedType.getClass().getTypeName();

            nextScope           = new Scope(root, new Statement.Block(-1));
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
            Statement.If        arbitraryBlockForScope;
            Expression.Unary    arbitraryExpressionForIf;

            Scope nextScope;
            Scope expectedParent;
            Scope actualParent;

            arbitraryLineNumber         = 1;
            arbitraryExpressionForIf    = createMockUnaryExpression(
                    "++", false, "parentTest");
            arbitraryBlockForScope      = new Statement.If(
                    arbitraryExpressionForIf, arbitraryLineNumber);

            root        = createRootScope();
            nextScope   = new Scope(root, arbitraryBlockForScope);

            assertEquals(nextScope.getParent(), root);
        }

        private Expression.Unary createMockUnaryExpression(
            String operator, boolean operatorIsOnRight, 
            String operandVariableName)
        {
            String          mockFileName = "ScopeTest_NonRootTest.java";
            int             mockLineIndex = 2;
            TokenBuilder    tokenBuilder = new TokenBuilder();
            Token           mockOp;
            Token           mockVarName;

            tokenBuilder.
                    setFileName(mockFileName).
                    setLineIndex(mockLineIndex);

            tokenBuilder.setTextContent(operator);
            mockOp      = tokenBuilder.build();

            tokenBuilder.setTextContent(operandVariableName);
            mockVarName = tokenBuilder.build();

            Statement.VarDeclaration mockVarDecl
                = new Statement.VarDeclaration(
                    Statement.VarDeclaration.Sign.SIGNED,
                    Statement.VarDeclaration.SimplifiedType.INT,
                    0, mockVarName,
                    Statement.VarDeclaration.NO_ASSIGNMENT,
                    Statement.VarDeclaration.NO_VALUE,
                    1);

            Expression.Variable mockVarUsage
                    = new Expression.Variable(mockVarDecl);
            Expression.Unary mockUnaryExpression
                    = new Expression.Unary(
                        mockOp, mockVarUsage, operatorIsOnRight);

            return mockUnaryExpression;
        }
    }

    /**
     * For tests in which it doesn't matter whether the Scope is the root scope
     * or not.
     */
    @RunWith(Enclosed.class)
    public static class RootUnrelatedTests
    {
        @RunWith(Enclosed.class)
        public static class VariableTests
        {
            public static class MultiVarTests
            {
                @Test
                public void testGetVariablesInThisScope_varCountIsCorrect()
                {
                    Scope           root;

                    String          mockFileName;
                    int             mockLineIndex;
                    TokenBuilder    tokenBuilder;

                    root            = createRootScope();
                    mockFileName    = "ScopeTest_RootUnrelatedTests.java";
                    mockLineIndex   = 2;
                    tokenBuilder    = new TokenBuilder();

                    tokenBuilder.
                        setFileName(mockFileName).
                        setLineIndex(mockLineIndex);

                    Token mockVarName;
                    Token mockVarName2;

                    tokenBuilder.setTextContent("varTest");
                    mockVarName     = tokenBuilder.build();

                    tokenBuilder.setTextContent("varTest2");
                    mockVarName2    = tokenBuilder.build();

                    Statement.VarDeclaration mockVarDecl;
                    Statement.VarDeclaration mockVarDecl2;

                    mockVarDecl
                        = new Statement.VarDeclaration(
                            Statement.VarDeclaration.Sign.UNSIGNED,
                            Statement.VarDeclaration.SimplifiedType.LONG,
                            0, mockVarName,
                            Statement.VarDeclaration.NO_ASSIGNMENT,
                            Statement.VarDeclaration.NO_VALUE,
                            1);

                    mockVarDecl2
                        = new Statement.VarDeclaration(
                            Statement.VarDeclaration.Sign.UNSPECIFIED,
                            Statement.VarDeclaration.SimplifiedType.CHAR,
                            0, mockVarName2,
                            Statement.VarDeclaration.NO_ASSIGNMENT,
                            Statement.VarDeclaration.NO_VALUE,
                            1);

                    root.addVariableToScope(mockVarDecl);

                    int             variableCount;
                    ArrayList<Statement.VarDeclaration>
                                    variables;

                    variables       = root.getVariablesInThisScope();
                    variableCount   = variables.size();

                    assertEquals(variableCount, 2);
                }

                /**
                 * Test of addVariableToScope method, of class Scope.
                 */
                @Test
                public void testAddVariableToScope()
                {
                    System.out.println("addVariableToScope");
                    Statement.VarDeclaration variable = null;
                    Scope instance = null;
                    instance.addVariableToScope(variable);
                    // TODO review the generated test code and remove the default call to fail.
                    fail("The test case is a prototype.");
                }
            }
        }
        
        public static class VariableUnrelatedTests
        {
            /**
             * Test of toString method, of class Scope.
             */
            @Test
            public void testToString()
            {
                System.out.println("toString");
                Scope instance = null;
                String expResult = "";
                String result = instance.toString();
                assertEquals(expResult, result);
                // TODO review the generated test code and remove the default call to fail.
                fail("The test case is a prototype.");
            }
        }
    }
    
    private static Scope createRootScope()
    {
        /**
         * Parameters should be Scope.ROOT_SCOPE and Scope.NO_SCOPE_TYPE,
         *  respectively.
         * 
         * Not using the defined constants to ensure that changes to them are
         *  explicit.
         */
        Scope root = new Scope(null, null);
        return root;
    }
}
