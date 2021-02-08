package bluC.transpiler.parser.handlers.statement;

import bluC.Logger;
import bluC.BluC;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.transpiler.parser.Parser;
import bluC.transpiler.Statement.VarDeclaration;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;

/**
 *
 * @author John Schneider
 */
public class FunctionHandler
{
    public static final String METHOD_NAMESPACE_PREFIX = "methods";
    
    private final Parser parser;
    private final StatementHandler statementHandler;
    private final BlockHandler blockHandler;
    private final VariableHandler varHandler;
    private static long unresolvedFunctionNamesCount = Long.MIN_VALUE;
    
    public FunctionHandler(Parser parser, StatementHandler statementHandler,
        BlockHandler blockHandler)
    {
        this.parser = parser;
        this.statementHandler = statementHandler;
        this.blockHandler = blockHandler;
        varHandler = statementHandler.getVarHandler();
    }
    
    public Statement handleFunctionOrMethod(VarDeclaration.Sign returnSign, 
        VarDeclaration.SimplifiedType returnSimplifiedType)
    {
        if (parser.isInAClass())
        {
            return handleMethod(returnSign, returnSimplifiedType);
        }

        return handleFunction(returnSign, returnSimplifiedType);
    }
    
    private Statement handleFunction(VarDeclaration.Sign returnSign, 
        VarDeclaration.SimplifiedType returnSimplifiedType)
    {
        Statement.Function function = getFuncOrMethod(returnSign, 
            returnSimplifiedType);
        
        parser.pushScope(new Scope(parser.getCurrentScope(), function));
        handleGlobalFunctionDeclaration(function);
        parser.popScope(parser.getCurToken());

        return function;
    }
    
    private Statement.Function getFuncOrMethod(VarDeclaration.Sign returnSign, 
        VarDeclaration.SimplifiedType returnSimplifiedType) 
    {
        int returnPointerLevel = varHandler.getPointerLevel();
        Statement.VarDeclaration returnType;
        Statement.Function function;
        Token functionName;

        parser.nextToken();

        functionName = parser.getCurToken();
        returnType = getReturnTypeVar(returnSign, returnSimplifiedType, 
            returnPointerLevel);
        function = new Statement.Function(returnType, functionName);
        
        return function;
    }
    
    private Statement.VarDeclaration getReturnTypeVar(
        VarDeclaration.Sign returnTypeSign, 
        VarDeclaration.SimplifiedType returnTypeType, 
        int returnTypePointerLevel)
    {
        Token curToken = parser.getCurToken();
        Token varName = new Token(
            new TokenInfo(VarDeclaration.RETURN_VAR_NAME, false),
                
            new TokenFileInfo(curToken.getFilepath(), 
                curToken.getLineIndex()));

        return new Statement.VarDeclaration(returnTypeSign, returnTypeType, 
            returnTypePointerLevel, varName, null, null);
    }
    
    public Statement handleMethod(VarDeclaration.Sign returnSign, 
        VarDeclaration.SimplifiedType returnSimplifiedType)
    {
        Statement.Function rawMethod = 
            getFuncOrMethod(returnSign, returnSimplifiedType);
        
        Statement.ClassDef curClass = (Statement.ClassDef) (parser.
            getCurrentScope().getScopeType());
        
        Statement.Method method = new Statement.Method(curClass, 
            rawMethod.getReturnType(), rawMethod.getNameToken(), 
            getMangledMethodName(curClass, rawMethod),
            parser);

        parser.pushScope(new Scope(parser.getCurrentScope(), method));
        handleMethodDeclaration(method);
        parser.popScope(parser.getCurToken());

        return method;
    }
    
    private String getMangledMethodName(Statement.ClassDef curClass,
        Statement.Function rawMethod)
    {
        return BluC.OXY_C_NAMESPACE_PREFIX + "_" + ClassHandler.
            CLASS_NAMESPACE_PREFIX + "_" + curClass.getClassName().
            getTextContent() + "_" + METHOD_NAMESPACE_PREFIX + "_" +
            rawMethod.getNameText();
        //TODO : add package support in mangling
    }
    
    private Statement handleMethodDeclaration(Statement.Method method)
    {
        handleGlobalFunctionDeclaration(method);
        
        return method;
    }
    
    private void handleGlobalFunctionDeclaration(
        Statement.Function function)
    {
        if (function.hasValidName())
        {
            handleGlobalFunctionDeclarationWithValidReturnTypeAndName(
                function);
        }
        else
        {
            handleBadGlobalFunctionName(function);
        }
    }
    
    /**
     * This is when we are either: (1) on a valid function name, or (2) the
     *  parser was able to synchronize a bad function name and we are carrying
     *  on to report any additional errors (and are still on a valid function
     *  name).
     */
    private void handleGlobalFunctionDeclarationWithValidReturnTypeAndName(
        Statement.Function function)
    {
        Statement.ParameterList params;
        Token next;
        Token functionName = function.getNameToken();
        
        params = getFunctionParameters(functionName);
        function.setParameters(params);
        next = parser.peek();
        
        if (next.getTextContent().equals("{"))
        {
            parser.nextToken();
            blockHandler.addStatementsToBlock(next, function);
        }
        else
        {
            Logger.err(next, "Expected \"{\" to open body of function \"" +
                functionName.getTextContent() + "\"");
        }
    }
    
    private Statement.ParameterList getFunctionParameters(Token functionName)
    {
        Token next = parser.peek();
        
        if (next.getTextContent().equals("("))
        {
            Statement.ParameterList returnee = new Statement.ParameterList();
            
            //consume "("
            parser.nextToken();
            addParametersToParameterList(returnee, functionName);
            
            return returnee;
        }
        else
        {
            Logger.err(next, "Expected \"(\" to start parameter list for " + 
                "function \"" + functionName.getTextContent() + "\"");
            return new Statement.ParameterList();
        }
    }
    
    private void addParametersToParameterList(Statement.ParameterList params,
        Token functionName)
    {
        boolean closingParenFound = false;
        Token curToken = parser.getCurToken();
        
        while (!parser.atEOF())
        {
            if (curToken.getTextContent().equals(")"))
            {
                closingParenFound = true;
                break;
            }
            
            params.addParameter((Statement.VarDeclaration) 
                varHandler.handleVarDeclarationOrHigher());
            
            parser.nextToken();
            curToken = parser.getCurToken();
        }
        
        if (!closingParenFound)
        {
            Logger.err(curToken, "Expected \")\" to end parameter list for " +
                "function \"" + functionName.getTextContent() + "\"");
        }
    }
    
    /*private void addBodyToFunc(Statement.Function func)
    {
        Token openBrace = parser.getCurToken();
        
        // make peek() peek at the next token after "{"
        parser.nextToken();
        Token next = parser.peek(); 
        
        boolean braceMatched = false;
        
        while (!parser.atEOF())
        {
            /**
             * We don't have to worry about matching inner braces because our 
             *  handleStatement method will automatically match any block
             *  statement braces for us
             */
            /*if (next.getTextContent().equals("}"))
            {
                braceMatched = true;
                break;
            }
            else
            {
                func.addStatement(statementHandler.handleStatement(true));
                next = parser.peek();
            }
        }
        
        if (!braceMatched)
        {
            Logger.err(next, "Expected \"}\" to end body of function \"" + 
                func.getNameText() + "\" on line " + 
                (openBrace.getLineIndex() + 1));
        }
    }*/
    
    private void handleBadGlobalFunctionName(
        Statement.Function function)
    {
        Token funcName = function.getNameToken();
        
        Logger.err(funcName,"Expected function name to follow \"" + 
            parser.peek(-1).getTextContent() + "\" (token \"" + 
            funcName.getTextContent() + "\" is an invalid name)");

        //  synchronize parser
        if (funcName.getTextContent().equals(";"))
        {
            //name missing
            parser.prevToken();
        }
        else
        {
            //invalid name
            parser.gotoEndOfStatement();
        }
        
        Token autoGeneratedName = getAutoGeneratedName(funcName);
        
        //add token so that 
        //  handleGlobalFunctionDeclarationWithValidReturnTypeAndName will see 
        //  the auto generated name
        parser.addToken(autoGeneratedName, parser.getCurTokIndex() + 1);
        function.setName(autoGeneratedName);
        
        handleGlobalFunctionDeclarationWithValidReturnTypeAndName(
            function);
    }
    
    private Token getAutoGeneratedName(Token funcName)
    {
        Token returnee = new Token(
            new TokenInfo("unresolvedFunctionName" +
                Long.toUnsignedString(unresolvedFunctionNamesCount), true), 
                
            new TokenFileInfo(funcName.getFilepath(), funcName.getLineIndex()));
        
        unresolvedFunctionNamesCount++;
        
        return returnee;
    }
}
