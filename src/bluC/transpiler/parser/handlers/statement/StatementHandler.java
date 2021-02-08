package bluC.transpiler.parser.handlers.statement;

import bluC.transpiler.parser.handlers.expression.ExpressionHandler;
import bluC.Logger;
import bluC.transpiler.Expression;
import bluC.transpiler.Statement;
import bluC.transpiler.Statement.VarDeclaration.Sign;
import bluC.transpiler.Statement.VarDeclaration.SimplifiedType;
import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.transpiler.parser.Parser;

/**
 *
 * @author John Schneider
 */
public class StatementHandler
{
    private final Parser parser;
    private final ExpressionHandler expressionHandler;
    private final VariableHandler varHandler;
    private final FunctionHandler funcHandler;
    private final BlockHandler blockHandler;
    private final IfHandler ifHandler;
    private final ClassHandler classHandler;
    
    public StatementHandler(Parser parser)
    {
        this.parser = parser;
        
        varHandler = new VariableHandler(parser, this);
        expressionHandler = new ExpressionHandler(parser, this);
        blockHandler = new BlockHandler(parser, this);
        funcHandler = new FunctionHandler(parser, this, blockHandler);
        ifHandler = new IfHandler(parser, blockHandler, expressionHandler);
        classHandler = new ClassHandler(parser, varHandler, blockHandler,
            funcHandler);
        
        //due to circular references to varHandler we had to create a 
        //  reference to varHandler and THEN retrieve references to the other 
        //  handlers
        varHandler.initHandlers();
    }
    
    
    public FunctionHandler getFuncHandler()
    {
        return funcHandler;
    }
    
    public ExpressionHandler getExpressionHandler()
    {
        return expressionHandler;
    }
    
    public VariableHandler getVarHandler()
    {
        return varHandler;
    }
    
    public Statement handleStatement(boolean checkForSemicolon)
    {
        Statement returnee = varHandler.handleVarDeclarationOrHigher();
        
        if (checkForSemicolon &&
            !(returnee instanceof Statement.Block) && !parser.peekMatches(";"))
        {
            Token curToken = parser.getCurToken();
            Logger.err(curToken, "Expected \";\" to end statement");
        }
        
        parser.nextToken();
        return returnee;
    }
    
    public Statement handleBlockStatementOrHigher()
    {
        Token openBrace = parser.peek();
        
        if (openBrace.getTextContent().equals("{"))
        {
            Statement.Block block = blockHandler.handleBlock(openBrace);
            return block;
        }
        
        return handleIfStatementOrHigher();
    }
    
    private Statement handleIfStatementOrHigher()
    {
        Token potentialIf = parser.peek();
        
        if (potentialIf.getTextContent().equals("if"))
        {
            return ifHandler.handleIfStatement(potentialIf);
        }
        
        return handleExpressionStatementOrHigher();
    }
    
    private Statement handleExpressionStatementOrHigher()
    {
        Token next = parser.peek();
        
        //"this" can be used in an expression (class member access) [or 
        //  constructor declaration, but in this case it's to parse the "this" 
        //  expression]
        if (!next.isReservedWord() || next.getTextContent().equals("this")) 
        {
            Expression expression = expressionHandler.handleExpression();

            return new Statement.ExpressionStatement(expression);
        }
        
        return handleClassDefinitionOrHigher();
    }
    
    private Statement handleClassDefinitionOrHigher() 
    {
        Token next = parser.peek();
        
        if (next.getTextContent().equals("class"))
        {
            return classHandler.handleClass(parser.peek());
        }
        
        return handleReturnOrHigher();
    }
    
    private Statement handleReturnOrHigher()
    {
        Token next = parser.peek();
        
        if (next.getTextContent().equals("return"))
        {
            Statement returnedExpression;
            Statement.Return return_;
            
            parser.nextToken();
            returnedExpression = handleExpressionStatementOrHigher();
            
            return_ = new Statement.Return(returnedExpression);
            
            return return_;
        }
        
        return handlePackage();
    }
    
    private Statement handlePackage()
    {
        Token next = parser.peek();
        
        if (next.getTextContent().equals("package"))
        {
            return getPackage();
        }
        
        return handleInvalidStartOfStatement();
    }
    
    private Statement getPackage()
    {
        int startIndex = parser.getCurTokIndex();
        Token packageToken = parser.peek();
        String fullyQualifiedPackage = "";
        
        while (true)
        {
            parser.nextToken();

            if (parser.atEOF())
            {
                int qualifiedNameStartLineIndex = packageToken.getLineIndex();
                
                Logger.err(parser.peek(-1), "Expected \";\" to end " + 
                    "package declaration on line " + (packageToken.
                    getLineIndex() + 1));

                //  synchronize parser
                
                parser.setToken(startIndex);
                fullyQualifiedPackage = "";
                
                while (true)
                {
                    parser.nextToken();
                    
                    if (parser.getCurToken().getLineIndex() != 
                        qualifiedNameStartLineIndex)
                    {
                        return new Statement.Package(fullyQualifiedPackage);
                    }
                    
                    fullyQualifiedPackage += parser.peek().getTextContent();
                }
            }
            else if (parser.peekMatches(";"))
            {
                return new Statement.Package(fullyQualifiedPackage);
            }

            fullyQualifiedPackage += parser.peek().getTextContent();
        }
    }
    
    private Statement handleInvalidStartOfStatement()
    {
        Token next = parser.peek();
        Logger.err(next, "\"" + next.getTextContent() + "\" cannot be the " +
            "start of a new statement");
        
        parser.gotoEndOfStatement();
        
        if (parser.atEOF())
        {
            //this should never happen so dump stack trace
            new Exception("Fatal parse error: prematurely reached end of " +
                "file").printStackTrace();
            
            //exit because otherwise our parser goes into an infinite loop
            System.exit(1);
        }
        
        //try to synchronize parser
        return new Statement.VarDeclaration(Sign.UNSPECIFIED, 
            SimplifiedType.VOID, 0, 
                
            new Token(
                new TokenInfo("null", true),

                new TokenFileInfo(next.getFilepath(), next.getLineIndex())),
                
            null, null);
    }
}
