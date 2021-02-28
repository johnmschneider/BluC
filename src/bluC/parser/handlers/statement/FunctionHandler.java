package bluC.parser.handlers.statement;

import bluC.Logger;
import bluC.BluC;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.parser.Parser;
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
        function = new Statement.Function(returnType, functionName,
            functionName.getLineIndex());
        
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
            returnTypePointerLevel, varName, null, null,
            curToken.getLineIndex());
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
            parser, rawMethod.getStartingLineIndex());

        parser.pushScope(new Scope(parser.getCurrentScope(), method));
        handleMethodDeclaration(method);
        parser.popScope(parser.getCurToken());

        return method;
    }
    
    private String getMangledMethodName(Statement.ClassDef curClass,
        Statement.Function rawMethod)
    {
        return BluC.BLU_C_NAMESPACE_PREFIX + "_" + ClassHandler.
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
     *  on to report any additional errors (and are thus technically on a valid
     *  function name).
     */
    private void handleGlobalFunctionDeclarationWithValidReturnTypeAndName(
        Statement.Function functionWithRetTypeAndName)
    {
        Statement.ParameterList params;
        Token next;
        Token functionName = functionWithRetTypeAndName.getNameToken();
        
        params = getFunctionParameters(functionName);
        functionWithRetTypeAndName.setParameters(params);
        next = parser.peek();
        
        if (next.getTextContent().equals("{"))
        {
            parser.nextToken();
            blockHandler.addStatementsToBlock(next, functionWithRetTypeAndName);
            
            /** 
             * It seems we can't do this because the parser, by design, must
             *  automatically move on to the next token after the current 
             *  statement was processed. If that step is omitted in the parser
             *  then we end up with an infinite parse loop.
             * 
             * I left the code here to remind myself not to try and modify the
             *  parse tree in this way in the future.
             * 
             * As such, statement processors are now required to end on the last
             *  token of the statement, so that the parser's automatic call to
             *  nextToken will not miss any tokens. For instance, a function
             *  handler will end with parser's current token set to the closing
             *  curly brace.
             */
            //parser.nextToken(); // move to token after "}"
        }
        else
        {
            Logger.err(next, "Expected \"{\" to open body of function \"" +
                functionName.getTextContent() + "\"");
        }
    }
    
    private Statement.ParameterList getFunctionParameters(Token functionName)
    {
        Token expectedOpenParen = parser.peek();
        
        if (expectedOpenParen.getTextContent().equals("("))
        {
            //move parser to "(" token
            parser.nextToken();
            
            Statement.ParameterList returnee = new Statement.ParameterList(
                parser.peek().getLineIndex());
            
            addParametersToParameterList(returnee, functionName);
            
            return returnee;
        }
        else
        {
            Logger.err(expectedOpenParen, "Expected \"(\" to start parameter " +
                "list for function \"" + functionName.getTextContent() + "\"");
            return new Statement.ParameterList(parser.peek(2).getLineIndex());
        }
    }
    
    private void addParametersToParameterList(Statement.ParameterList params,
        Token functionName)
    {
        boolean closingParenFound   = false;
        Token curToken;
        
        /**
         * We should now be on the "(" token. 
         * 
         * VariableHandler.handleVarDeclarationOrHigher() requires the parser to
         *  be on the token immediately BEFORE the variable declaration starts.
         */
        curToken = parser.getCurToken();
        
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
