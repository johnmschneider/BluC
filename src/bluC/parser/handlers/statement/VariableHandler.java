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

package bluC.parser.handlers.statement;

import java.util.ArrayList;
import bluC.Logger;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.Token;
import bluC.parser.Parser;
import bluC.parser.exceptions.InvalidSizeModifier;
import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;
import bluC.parser.handlers.expression.ExpressionHandler;
import bluC.transpiler.Expression;
import bluC.transpiler.statements.ExpressionStatement;
import bluC.transpiler.statements.blocks.ClassDef;
import bluC.transpiler.statements.vars.Sign;
import bluC.transpiler.statements.vars.SimplifiedType;
import bluC.transpiler.statements.vars.SizeModifier;

/**
 *
 * @author John Schneider
 */
public class VariableHandler
{
    private final Parser            parser;
    private final StatementHandler  statementHandler;
    private FunctionHandler         funcHandler;
    private ExpressionHandler       expressionHandler;
    private static long             unresolvedVariableNamesCount = 
        Long.MIN_VALUE;
    
    public class TypeAndClassID
    {
        private final SimplifiedType type;
        private final String classID;
        
        public TypeAndClassID(SimplifiedType type, String classID)
        {
            this.type = type;
            this.classID = classID;
        }
        
        public SimplifiedType getType()
        {
            return type;
        }
        
        public String getClassID()
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
    
    /**
     * Handles a variable declaration or higher.
     * 
     * Expects the parser to be on the token immediately BEFORE a potential
     *  variable declaration.
     */
    public Statement handleVarDeclarationOrHigher()
    {
        int     startTokenIndex     = parser.getCurTokIndex();
        Sign
                sign                = getSign();
        TypeAndClassID 
                typeAndClassID      = getTypeAndClassID();
        
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
        
        parser.setToken(startTokenIndex);
        return statementHandler.handleBlockStatementOrHigher();
    }
    
    /**
     * This function expects the parser's currentToken to be on the variable
     *  name.
     * 
     * If the original program is erroneous and there is no such token,
     *  this function outputs a compile-level error (issue with compilation,
     *  not the compiler itself) and synchronizes the parser by returning an
     *  automatically generated valid variable name.
     * 
     * Since the compiler is flagged as having a fatal compile error, this
     *  program will not compile.
     */
    private Statement handleVarDeclarationWithValidType(
        Sign sign, SimplifiedType type, 
        String classID)
    {
        Token   varName;
        int     pointerLevel = getPointerLevel();
        
        // curToken *must* now be the variable's name or it is a syntax error
        parser.nextToken();
        varName = parser.getCurToken();

        Token   nextToken       = parser.peek();
        String  nextTokenText   = nextToken.getTextContent();

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
    
    /**
     * Returns the current pointerLevel of the variable/function return type.
     * 
     * Ends with the parser's curToken set to one of two things:
     *  1) the last asterisk of the pointer (if it exists)
     *  2) the token the parser was on before this function call (if there is no
     *      pointer level).
     */
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
        Sign sign, SimplifiedType type,
        int pointerLevel, Token varName, String nextTokenText, String classID)
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
            
            //  synchronize parser
            
            Expression nullExpr;
            
            /*
             * parser assumes we meant to have a VarDeclarationWithoutAssignment
             *  but forgot semicolon, so we're already on the correct token for
             *  synchronization.
             */
            nullExpr = ExpressionHandler.createNullLiteral(
                parser.getCurTokText(), (int) parser.getCurTokLineIndex());
            
            return new ExpressionStatement(
                nullExpr, parser.getCurTokLineIndex());
            
            // TODO - this synchronized the parser incorrectly. Leaving it 
            //  for a bit just in case the new synchronizer breaks things.
            //
            //parser.gotoEndOfStatement();
            //
            //return handleVarDeclarationWithoutAssignment(sign, type, 
            //    pointerLevel, varName, classID);
        }
    }
    
    private Statement handleBadVarName(Sign sign, 
        SimplifiedType type,
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
            null, null, autoGeneratedName.getLineIndex());
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
    
    /**
     * Handles a syntactically valid "variable declaration with assignment"
     *  statement.
     * 
     * On failure, it returns a VarDaclaration as-if it were successful, but
     *  flags and reports a compile error. This VarDeclaration is simply to
     *  synchronize the parser from the error.
     * 
     * The returned VarDeclaration is *not* added to the current scope's
     *  variable list -- the previous variable definition is used when parsing
     *  further.
     */        
    private Statement handleVarDeclarationWithAssignment(
        Sign sign, SimplifiedType type, 
        int pointerLevel, Token varName, String classID)
    {
        VarDeclaration  var;
        boolean         alreadyDeclared;
        long            startLineIndex = parser.getCurTokLineIndex();
        Token           assignmentOp;
        
        assignmentOp = parser.peek();
        
        var = new VarDeclaration(sign, type, pointerLevel, varName, 
            assignmentOp, expressionHandler.handleExpression(),
            startLineIndex);
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
        Sign sign, SimplifiedType type, 
        int pointerLevel, Token varName, String classID)
    {
        VarDeclaration var = new VarDeclaration(sign, type, 
            pointerLevel, varName, null, null, varName.getLineIndex());
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
    
    /**
     * Parses the current signed/unsigned specifier of the variable (if it
     *  exists). 
     * 
     * Ends the function with the current token being set to the signed/unsigned
     *  specifier (if it exists), or the token the function started on if
     *  there is no such specifier present.
     */
    private Sign getSign()
    {
        String              peekText    = parser.peek().getTextContent();
        Sign sign        = Sign.UNSPECIFIED;
        
        if (peekText.equals("signed"))
        {
            sign = Sign.SIGNED;
            
            //consume the peek
            parser.nextToken();
        }
        else if (peekText.equals("unsigned"))
        {
            sign = Sign.UNSIGNED;
            
            //consume the peek
            parser.nextToken();
        }
        
        return sign;
    }
    
    /**
     * Parses and returns the size modifier for a variable. Always leaves the
     *  parser on the last token of the size modifier, even if it is two or
     *  more words long.
     */
    private SizeModifier getSizeModifier() 
        throws InvalidSizeModifier
    {
        Token sizeMod1 = parser.peek();
        Token sizeMod2OrTypeOrName = parser.peek(2);
        
        String sizeMod1Text = sizeMod1.getTextContent();
        String sizeMod2OrTypeOrNameText = sizeMod2OrTypeOrName.getTextContent();
        
        SizeModifier sizeModifier = SizeModifier.
            UNSPECIFIED;
        
        if (sizeMod1Text.equals("short"))
        {
            // move parser from current token to "short" keyword
            parser.nextToken();
            
            if (sizeMod2OrTypeOrNameText.equals("int"))
            {
                sizeModifier = SizeModifier.SHORT;
            }
            else
            {
                // this is most likely (but not *definitely*) either an invalid
                //  data type base for this modifier (ex. short dounle) or a
                //  variable name that they forgot to specify the base,
                //  either way synchronize the parser
                throw new InvalidSizeModifier(sizeMod1, sizeMod2OrTypeOrName);
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
                    sizeModifier = SizeModifier.LONG_LONG;
                
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
                sizeModifier = SizeModifier.LONG;
            }
        }
        
        return sizeModifier;
    }
    
    private TypeAndClassID getTypeAndClassID()
    {
        SizeModifier sizeModifier;
        
        try
        {
            sizeModifier = getSizeModifier();
        } 
        catch (InvalidSizeModifier ex)
        {
            Token errAt = ex.getSizeMod1();
            Logger.err(errAt, ex.getMessage());
            
            //synchronize parser
            sizeModifier = SizeModifier.UNSPECIFIED;
        }
        
        if (sizeModifier == SizeModifier.SHORT)
        {
            if (parser.peekMatches("int"))
            {
                //consume size modifier token
                parser.nextToken();
            }
            else
            {
                return guessTypeFromSizeErrorAndLogError(sizeModifier);
            }
            
            return new TypeAndClassID(SimplifiedType.SHORT,
                ClassDef.NOT_DEFINED);
        }
        else if (sizeModifier == SizeModifier.LONG)
        {
            if (parser.peekMatches("double"))
            {
                //consume size modifier token
                parser.nextToken();
                
                return new TypeAndClassID(SimplifiedType.
                   LONG_DOUBLE, ClassDef.NOT_DEFINED);
            }
            else if (parser.peekMatches("int"))
            {
                //consume size modifier token
                parser.nextToken();
            }
            else 
            {
                return guessTypeFromSizeErrorAndLogError(sizeModifier);
            }
            
            return new TypeAndClassID(SimplifiedType.LONG,
                ClassDef.NOT_DEFINED);
        }
        else if (sizeModifier == SizeModifier.LONG_LONG)
        {
            //align tokens such that nextToken is variable type or varName
            parser.nextToken();
            
            Token   varTypeOrName       = parser.peek();
            String  varTypeOrNameText   = varTypeOrName.getTextContent();
            
            if (varTypeOrNameText.equals("int"))
            {
                //consume var type token
                parser.nextToken();
            }
            else if (!varTypeOrName.isValidName())
            {
                return guessTypeFromSizeErrorAndLogError(sizeModifier);
            }
            
            return new TypeAndClassID(SimplifiedType.LONG_LONG,
                ClassDef.NOT_DEFINED);
        }
        else
        {
            return getUnmodifiedType();
        }
    }
    
    /**
     * Parses a variable type without regards to the modifier used.
     */
    private TypeAndClassID getUnmodifiedType()
    {
        parser.nextToken();
        String type = parser.getCurTokText();

        if (type.equals("char"))
        {
            return new TypeAndClassID(SimplifiedType.CHAR,
                ClassDef.NOT_DEFINED);
        }
        else if (type.equals("int"))
        {
            return new TypeAndClassID(SimplifiedType.INT,
                ClassDef.NOT_DEFINED);
        }
        else if (type.equals("float"))
        {
            return new TypeAndClassID(SimplifiedType.FLOAT,
                ClassDef.NOT_DEFINED);
        }
        else if (type.equals("double"))
        {
            return new TypeAndClassID(SimplifiedType.DOUBLE,
                ClassDef.NOT_DEFINED);
        }
        else if (type.equals("void")) 
        {
            return new TypeAndClassID(SimplifiedType.VOID,
                ClassDef.NOT_DEFINED);
        }
        else if (ClassHandler.isClassDefined(parser.getCurToken()))
        {
            return new TypeAndClassID(SimplifiedType.CLASS,
                ClassHandler.getClassID(parser.getCurToken()));
        }

        //TODO : else check for structs

        //error! unrecognized symbol that we *think* is a variable but it
        //  has no type!
        //
        //attempt to synchronize parser
        return null;
    }
    
    private TypeAndClassID guessTypeFromSizeErrorAndLogError(
        SizeModifier sizeModifier)
    {
        Logger.err(parser.getCurToken(), "invalid size specifier \"" + 
            sizeModifier.getActualModifierText() + "\" for type \"" +
            parser.peek().getTextContent() + "\"");
        return getUnmodifiedType();
    }
}
