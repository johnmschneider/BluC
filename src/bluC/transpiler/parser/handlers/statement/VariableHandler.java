package bluC.transpiler.parser.handlers.statement;

import java.util.ArrayList;
import bluC.Logger;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.transpiler.parser.Parser;
import bluC.transpiler.parser.exceptions.InvalidSizeModifier;
import bluC.transpiler.Statement.VarDeclaration;
import bluC.transpiler.Statement.VarDeclaration.SimplifiedType;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.transpiler.parser.handlers.expression.ExpressionHandler;

/**
 *
 * @author John Schneider
 */
public class VariableHandler
{
    private final Parser parser;
    private final StatementHandler statementHandler;
    private FunctionHandler funcHandler;
    private ExpressionHandler expressionHandler;
    private static long unresolvedVariableNamesCount = Long.MIN_VALUE;
    
    public class TypeAndClassID
    {
        private final SimplifiedType type;
        private final long classID;
        
        public TypeAndClassID(SimplifiedType type, long classID)
        {
            this.type = type;
            this.classID = classID;
        }
        
        public SimplifiedType getType()
        {
            return type;
        }
        
        public long getClassID()
        {
            return classID;
        }
    }
    
    public VariableHandler(Parser parser, StatementHandler statementHandler)
    {
        this.parser = parser;
        this.statementHandler = statementHandler;
    }
            
    
    public void initHandlers()
    {
        funcHandler = statementHandler.getFuncHandler();
        expressionHandler = statementHandler.getExpressionHandler();
    }
    
    public Statement handleVarDeclarationOrHigher()
    {
        int startToken           = parser.getCurTokIndex();
        String peekText          = parser.peek().getTextContent();
        VarDeclaration.Sign sign = getSign(peekText);
        
        TypeAndClassID typeAndClassID       = getTypeAndClassID();
        
        if (typeAndClassID != null)
        {
            if (parser.peekMatches(2, "("))
            {
                return funcHandler.handleFunctionOrMethod(sign,
                    typeAndClassID.getType());
            }
            else 
            {
                return handleVarDeclarationWithValidType(sign, 
                    typeAndClassID.getType(), typeAndClassID.getClassID());
            }
        }
        
        parser.setToken(startToken);
        return statementHandler.handleBlockStatementOrHigher();
    }
    
    private Statement handleVarDeclarationWithValidType(
        VarDeclaration.Sign sign, VarDeclaration.SimplifiedType type, 
        long classID)
    {
        Token varName;
        int pointerLevel = getPointerLevel();
        
        parser.nextToken();
        varName = parser.getCurToken();

        Token nextToken = parser.peek();
        String nextTokenText = nextToken.getTextContent();

        if (varName.isValidName())
        {
            return handleVarDeclarationWithValidTypeAndName(sign, type, 
                pointerLevel, varName, nextTokenText, classID);
        }
        else
        {
            return handleBadVarName(sign, type, pointerLevel, varName);
        }
    }
    
    public int getPointerLevel()
    {
        int pointerLevel = 0;
        
        String peekText = parser.peek().getTextContent();
        
        while(!parser.atEOF() && peekText.equals("*"))
        {
            pointerLevel++;
            parser.nextToken();
            peekText = parser.peek().getTextContent();
        }
        
        return pointerLevel;
    }
    
    private Statement handleVarDeclarationWithValidTypeAndName(
        VarDeclaration.Sign sign, VarDeclaration.SimplifiedType type, 
        int pointerLevel, Token varName, String nextTokenText, long classID)
    {
        if (nextTokenText.equals("="))
        {
            return handleVarDeclarationWithAssignment(sign, type, pointerLevel,
                varName, classID);
        }
        else if (nextTokenText.equals(";") || nextTokenText.equals(",") ||
            nextTokenText.equals(")"))
        {
            return handleVarDeclarationWithoutAssignment(sign, type, 
                pointerLevel, varName, classID);
        }
        else
        {
            Token curToken = parser.getCurToken();
            
            Logger.err(curToken, "Expected assignment operator or semicolon " +
                "after variable declaration");

            parser.gotoEndOfStatement();

            return handleVarDeclarationWithoutAssignment(sign, type, 
                pointerLevel, varName, classID);
        }
    }
    
    private Statement handleBadVarName(VarDeclaration.Sign sign, 
        VarDeclaration.SimplifiedType type,
        int pointerLevel, Token varName)
    {
        Logger.err(varName, "Expected variable name to follow \"" + 
            type.name().toLowerCase().replace("_", " ") + "\" (token \"" + 
            varName.getTextContent() + "\" is an invalid name)");

        //  synchronize parser
        if (varName.getTextContent().equals(";"))
        {
            //name missing
            parser.prevToken();
        }
        else
        {
            //invalid name
            parser.gotoEndOfStatement();
        }
        
        Token autoGeneratedName = getAutoGeneratedName(varName);
        Statement returnee =  new VarDeclaration(sign, type, 
            pointerLevel, autoGeneratedName,
            null, null);
        return returnee;
    }
    
    private Token getAutoGeneratedName(Token varName)
    {
        Token returnee = new Token(
            new TokenInfo("unresolvedVariableName" + 
                Long.toUnsignedString(unresolvedVariableNamesCount), true),
                
            new TokenFileInfo(varName.getFilepath(), varName.getLineIndex()));
        
        unresolvedVariableNamesCount++;
        
        return returnee;
    }
            
    private Statement handleVarDeclarationWithAssignment(
        VarDeclaration.Sign sign, VarDeclaration.SimplifiedType type, 
        int pointerLevel, Token varName, long classID)
    {
        VarDeclaration var;
        boolean alreadyDeclared;
        
        //special trick to get parser to handle both assignment *expressions*
        //  (such as "alreadyDeclaredVar = 1") and assignment *statements*
        //  (such as "int newlyDeclaredVar = 22").
        //
        //  set curToken to "="
        parser.nextToken();
        
        var = new VarDeclaration(sign, type, pointerLevel, varName, 
            parser.getCurToken(), expressionHandler.handleExpression());
        alreadyDeclared = isVarAlreadyDeclaredInThisScope(var);
        
        if (alreadyDeclared)
        {
            Logger.err(varName, "Variable \"" + varName.getTextContent() + 
                "\" already declared");
        }
        else
        {
            if (type == SimplifiedType.CLASS)
            {
                var.setClassID(classID);
            }
            
            parser.getCurrentScope().addVariableToScope(var);
        }
        
        return var;
    }
    
    private Statement handleVarDeclarationWithoutAssignment(
        VarDeclaration.Sign sign, VarDeclaration.SimplifiedType type, 
        int pointerLevel, Token varName, long classID)
    {
        VarDeclaration var = new VarDeclaration(sign, type, 
            pointerLevel, varName, null, null);
        boolean alreadyDeclared = isVarAlreadyDeclaredInThisScope(var);

        if (alreadyDeclared)
        {
            Logger.err(varName, "Variable \"" + varName.getTextContent() + 
                "\" already declared in this scope");
        }
        else
        {
            if (type == SimplifiedType.CLASS)
            {
                var.setClassID(classID);
            }
            
            parser.getCurrentScope().addVariableToScope(var);
        }

        return var;
    }
    
    public boolean isVarNameTheSame(VarDeclaration var, Token potentialVarName)
    {
        return var.getNameText().equals(potentialVarName.getTextContent());
    }
    
    private VarDeclaration getVarAlreadyDeclaredInThisScope(Scope scope, 
        Token var)
    {
        ArrayList<VarDeclaration> variablesInThisScope = 
            scope.getVariablesInThisScope();
        
        for (VarDeclaration var2 : variablesInThisScope)
        {
            if (isVarNameTheSame(var2, var))
            {
                return var2;
            }
        }

        return null;
    }
    
    public VarDeclaration getVarAlreadyDeclaredInThisScope(Token var)
    {
        return getVarAlreadyDeclaredInThisScope(parser.getCurrentScope(), var);
    }
    
    public VarDeclaration getVarAlreadyDeclaredInThisScope(
        VarDeclaration var)
    {
        return getVarAlreadyDeclaredInThisScope(var.getName());
    }
    
    public VarDeclaration getVarAlreadyDeclaredInThisScopeOrHigher(
        Token potentialVarName)
    {
        Scope curSearchScope = parser.getCurrentScope();
        
        while (curSearchScope != null)
        {
            VarDeclaration variable = getVarAlreadyDeclaredInThisScope
                (curSearchScope, potentialVarName);
            
            if (variable != null)
            {
                return variable;
            }
            
            curSearchScope = curSearchScope.getParent();
        }
        
        return null;
    }
    
    public boolean isVarAlreadyDeclaredInThisScope(VarDeclaration var)
    {
        return getVarAlreadyDeclaredInThisScope(var) != null;
    }
    
    private VarDeclaration.Sign getSign(String text)
    {
        VarDeclaration.Sign sign = VarDeclaration.Sign.UNSPECIFIED;
        
        if (text.equals("signed"))
        {
            sign = VarDeclaration.Sign.SIGNED;
            
            //consume the peek
            parser.nextToken();
        }
        else if (text.equals("unsigned"))
        {
            sign = VarDeclaration.Sign.UNSIGNED;
            
            //consume the peek
            parser.nextToken();
        }
        
        return sign;
    }
    
    private VarDeclaration.SizeModifier getSizeModifier() 
        throws InvalidSizeModifier
    {
        Token sizeMod1 = parser.peek();
        Token sizeMod2OrTypeOrName = parser.peek(2);
        
        String sizeMod1Text = sizeMod1.getTextContent();
        String sizeMod2OrTypeOrNameText = sizeMod2OrTypeOrName.getTextContent();
        
        VarDeclaration.SizeModifier sizeModifier = VarDeclaration.SizeModifier.
            UNSPECIFIED;
        
        if (sizeMod1Text.equals("short"))
        {
            if (sizeMod2OrTypeOrNameText.equals("char")  || 
                sizeMod2OrTypeOrNameText.equals("short") ||
                sizeMod2OrTypeOrNameText.equals("long")  ||
                sizeMod2OrTypeOrNameText.equals("float") ||
                sizeMod2OrTypeOrNameText.equals("double"))
            {
                parser.nextToken();
                throw new InvalidSizeModifier(sizeMod1, sizeMod2OrTypeOrName);
            }
            else
            {
                parser.nextToken();
                sizeModifier = VarDeclaration.SizeModifier.SHORT;
            }
        }
        else if (sizeMod1Text.equals("long"))
        {
            if (sizeMod2OrTypeOrNameText.equals("long"))
            {
                Token typeOrName = parser.peek(3);
                String typeOrNameText = 
                    typeOrName.getTextContent();
                
                if (typeOrNameText.equals("char")  || 
                      typeOrNameText.equals("short") || 
                      typeOrNameText.equals("long")  ||
                      typeOrNameText.equals("float") ||
                      typeOrNameText.equals("double"))
                {
                    //  synchronize parser
                    parser.nextToken();
                    parser.nextToken();
                    
                    throw new InvalidSizeModifier(sizeMod1, 
                        sizeMod2OrTypeOrName, typeOrName);
                }
                else
                {
                    sizeModifier = VarDeclaration.SizeModifier.LONG_LONG;
                
                    //  align tokens such that nextToken() is the variable type
                    parser.nextToken();
                    
                    if (typeOrNameText.equals("int"))
                    {
                        parser.nextToken();
                    }
                }
            }
            else if (sizeMod2OrTypeOrNameText.equals("char") ||
                    sizeMod2OrTypeOrNameText.equals("short") ||
                    sizeMod2OrTypeOrNameText.equals("float"))
            {
                parser.nextToken();
                throw new InvalidSizeModifier(sizeMod1, 
                    sizeMod2OrTypeOrName);
            }
            else
            {
                parser.nextToken();
                sizeModifier = VarDeclaration.SizeModifier.LONG;
            }
        }
        
        return sizeModifier;
    }
    
    private TypeAndClassID getTypeAndClassID()
    {
        VarDeclaration.SizeModifier sizeModifier;
        
        try
        {
            sizeModifier = getSizeModifier();
        } catch (InvalidSizeModifier ex)
        {
            Token errAt = ex.getSizeMod1();
            Logger.err(errAt, ex.getMessage());
            
            //synchronize parser
            sizeModifier = VarDeclaration.SizeModifier.UNSPECIFIED;
        }
        
        if (sizeModifier == VarDeclaration.SizeModifier.SHORT)
        {
            if (parser.peekMatches("int"))
            {
                //consume size modifier token
                parser.nextToken();
            }
            
            return new TypeAndClassID(VarDeclaration.SimplifiedType.SHORT,
                ClassHandler.CLASS_UNSPECIFIED);
        }
        else if (sizeModifier == VarDeclaration.SizeModifier.LONG)
        {
            
            if (parser.peekMatches("double"))
            {
                //consume size modifier token
                parser.nextToken();
                
                return new TypeAndClassID(VarDeclaration.SimplifiedType.
                   LONG_DOUBLE, ClassHandler.CLASS_UNSPECIFIED);
            }
            else if (parser.peekMatches("int"))
            {
                //consume size modifier token
                parser.nextToken();
            }
            
            return new TypeAndClassID(VarDeclaration.SimplifiedType.LONG,
                ClassHandler.CLASS_UNSPECIFIED);
        }
        else if (sizeModifier == VarDeclaration.SizeModifier.LONG_LONG)
        {
            //align tokens such that nextToken is variable type
            parser.nextToken();
            
            if (parser.peekMatches("int"))
            {
                //consume size modifier token
                parser.nextToken();
            }
            
            return new TypeAndClassID(VarDeclaration.SimplifiedType.LONG_LONG,
                ClassHandler.CLASS_UNSPECIFIED);
        }
        else
        {
            parser.nextToken();
            String type = parser.getCurTokText();
            
            if (type.equals("char"))
            {
                return new TypeAndClassID(VarDeclaration.SimplifiedType.CHAR,
                    ClassHandler.CLASS_UNSPECIFIED);
            }
            else if (type.equals("int"))
            {
                return new TypeAndClassID(VarDeclaration.SimplifiedType.INT,
                    ClassHandler.CLASS_UNSPECIFIED);
            }
            else if (type.equals("float"))
            {
                return new TypeAndClassID(VarDeclaration.SimplifiedType.FLOAT,
                    ClassHandler.CLASS_UNSPECIFIED);
            }
            else if (type.equals("double"))
            {
                return new TypeAndClassID(VarDeclaration.SimplifiedType.DOUBLE,
                    ClassHandler.CLASS_UNSPECIFIED);
            }
            else if (ClassHandler.isClassDefined(parser.getCurToken()))
            {
                return new TypeAndClassID(VarDeclaration.SimplifiedType.CLASS,
                    ClassHandler.getClassID(parser.getCurToken()));
            }

            //TODO : else check for structs
            
            //error! unrecognized symbol that we *think* is a variable but it
            //  has no type!
            //
            //attempt to synchronize parser
            return null;
        }
    }
}
