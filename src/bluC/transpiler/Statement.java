package bluC.transpiler;

import java.util.ArrayList;
import bluC.transpiler.Statement.VarDeclaration.Sign;
import bluC.transpiler.Statement.VarDeclaration.SimplifiedType;
import bluC.transpiler.parser.Parser;

/**
 * @author John Schneider
 */
public abstract class Statement
{
    public static interface Visitor<T>
    {
        //blocks
        T visitBlock(Statement.Block statement);
        
        T visitFunction(Statement.Function statement);
        T visitMethod(Statement.Method statement);
        T visitParameterList(Statement.ParameterList statement);
                    
        T visitIf(Statement.If statement);
        T visitClassDef(Statement.ClassDef statement);
        T visitStructDef(Statement.StructDef statement);
        T visitWhile(Statement.While statement);
        
        //misc
        T visitReturn(Statement.Return statement);
        T visitExpressionStatement(Statement.ExpressionStatement statement);
        T visitPackage(Statement.Package statement);
        
        //vars
        T visitVarDeclaration(Statement.VarDeclaration statement);
    }
    
    public static class Block extends Statement
    {
        private ArrayList<Statement> body;
        
        public Block()
        {
            this.body = new ArrayList<>();
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitBlock(this);
        }
        
        public final <T> T acceptBlock(Visitor<T> visitor)
        {
            return visitor.visitBlock(this);
        }
        
        public ArrayList<Statement> getBody()
        {
            return body;
        }
        
        public void addStatement(Statement statement)
        {
            body.add(statement);
        }
        
        @Override
        public boolean needsSemicolon()
        {
            return false;
        }
        
        public boolean needsExtraSpace()
        {
            return true;
        }
    }
    
    public static class Function extends Block
    {
        private Statement.VarDeclaration returnType;
        private Statement.ParameterList parameterList;
        private Token functionName;
        
        public Function(Statement.VarDeclaration returnType, Token functionName)
        {
            super();
            
            this.returnType = returnType;
            this.functionName = functionName;
        }
        
        public Statement.VarDeclaration getReturnType()
        {
            return returnType;
        }
        
        public boolean hasValidName()
        {
            return functionName.isValidName(); 
        }
        
        public Token getNameToken()
        {
            return functionName;
        }
        
        public String getNameText()
        {
            return functionName.getTextContent();
        }
        
        public void setName(Token newName)
        {
            functionName = newName;
        }
        
        public void setParameters(ParameterList parameters)
        {
            parameterList = parameters;
        }
        
        public Statement.ParameterList getParameters()
        {
            return parameterList;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitFunction(this);
        }
    }
    
    public static class ParameterList extends Statement
    {
        private final ArrayList<Statement.VarDeclaration> parameters;
        
        public ParameterList()
        {
            this.parameters = new ArrayList<>();
        }
        
        public void addParameter(Statement.VarDeclaration param)
        {
            parameters.add(param);
        }
        
        public ArrayList<Statement.VarDeclaration> getParameters()
        {
            return parameters;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitParameterList(this);
        }
    }
    
    public static class Method extends Function
    {
        private final Statement.ClassDef class_;
        private final String mangledName;
        private final Parser parser;
        
        public Method(Statement.ClassDef class_, VarDeclaration returnVar,
            Token methodName, String mangledName, Parser parser)
        {
            super(returnVar, methodName);
            this.class_ = class_;
            this.mangledName = mangledName;
            this.parser = parser;
        }
        
        @Override
        public void setParameters(ParameterList parameters)
        {
            ParameterList listWithThis = new ParameterList();
            
            // to determine what file and line the "this" is on
            Token param1Token = parameters.getParameters().get(0).getName();
            
            VarDeclaration this_ = new VarDeclaration(Sign.UNSPECIFIED, 
                SimplifiedType.CLASS, 1,
                
                new Token(
                   new TokenInfo("this", false),
                        
                   new TokenFileInfo(param1Token.getFilepath(), 
                       param1Token.getLineIndex())
                ), 
                    
                null, null);
            
            this_.setClassID(class_.getClassID());
            parser.getCurrentScope().addVariableToScope(this_);
                    
            listWithThis.addParameter(this_);
            
            for (VarDeclaration parameter : parameters.getParameters())
            {
                listWithThis.addParameter(parameter);
            }
            
            super.setParameters(listWithThis);
        }
        
        public Statement.ClassDef getClass_()
        {
            return class_;
        }
        
        public String getMangledName()
        {
            return mangledName;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitMethod(this);
        }
    }
    
    public static class If extends Block
    {
        public static class ElseIf extends Block
        {
            private Expression condition;
            
            public ElseIf(Expression condition)
            {
                super();
                this.condition = condition;
            }
            
            public Expression getCondition()
            {
                return condition;
            }
            
            @Override
            public boolean needsExtraSpace()
            {
                return false;
            }
        }
        
        public static class Else extends Block
        {
            public Else()
            {
                super();
            }
            
            @Override
            public boolean needsExtraSpace()
            {
                // the visitIf itself adds extra whitespace
                return false;
            }
        }
        
        private Expression condition;
        private ArrayList<ElseIf> elseIfs;
        private Else else_;
        
        public If(Expression condition)
        {
            super();
            this.condition = condition;
            elseIfs = new ArrayList<>();
            else_ = null;
        }
        
        public Expression getCondition()
        {
            return condition;
        }
        
        public void addElseIf(ElseIf elseIf)
        {
            elseIfs.add(elseIf);
        }
        
        public ArrayList<ElseIf> getElseIfs()
        {
            return elseIfs;
        }
        
        public void setElse(Else else_)
        {
            this.else_ = else_;
        }
        
        public Statement.Block getElse()
        {
            return else_;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitIf(this);
        }
        
        @Override
        public boolean needsExtraSpace()
        {
            // the visitIf itself adds extra whitespace
            return false;
        }
    }
    
    public static class ClassDef extends Block
    {
        private Token className;
        private Token baseClass;
        private long classID;
        private static long nextClassID = Long.MIN_VALUE;
        public static final ClassDef objectBaseClass = new ClassDef("Object", 
            "bluc.lang");
        
        /**
         * Helper constructor for other constructors.
         */
        private ClassDef() 
        {
            super();
            
            this.baseClass = null;
            this.classID = nextClassID;
            
            nextClassID++;
        }
        
        private ClassDef(String className, String package_)
        {
            this();
            
            this.className = new Token(
                new TokenInfo(className, true),
                    
                new TokenFileInfo("Statement.java", -1),
                package_);
        }
        
        public ClassDef(Token className) 
        {
            this();
            
            this.className = className;
        }
        
        public Token getClassName()
        {
            return className;
        }
        
        public void setBaseClass(Token baseClass) 
        {
            this.baseClass = baseClass;
        }
        
        public long getClassID()
        {
            return classID;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitClassDef(this);
        }
    }
    
    public static class StructDef extends Block
    {
        public StructDef()
        {
            super();
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitStructDef(this);
        }
        
        @Override
        public boolean needsSemicolon()
        {
            return true;
        }
    }
    
    public static class While extends Block
    {
        public While()
        {
            super();
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitWhile(this);
        }
    }
    
    public static class Return extends Statement
    {
        private Statement returnedStatement;
        
        public Return(Statement returnedStatement)
        {
            this.returnedStatement = returnedStatement;
        }
        
        public Statement getReturnedStatement()
        {
            return returnedStatement;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitReturn(this);
        }
    }
    
    public static class ExpressionStatement extends Statement
    {
        private final Expression expression;
        
        public ExpressionStatement(Expression expression)
        {
            this.expression = expression;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitExpressionStatement(this);
        }
        
        public Expression getExpression()
        {
            return expression;
        }
    }
    
    public static class VarDeclaration extends Statement
    {
        public static enum Sign
        {
            SIGNED,
            UNSIGNED,
            UNSPECIFIED
        }
        
        public static enum SizeModifier
        {
            SHORT,
            LONG,
            LONG_LONG,
            UNSPECIFIED
        }
        
        public static enum SimplifiedType
        {
            CHAR,
            SHORT,
            INT,
            LONG,
            LONG_LONG,
            FLOAT,
            DOUBLE,
            LONG_DOUBLE,
            VOID,
            STRUCT,
            CLASS
        }
        
        public static final String RETURN_VAR_NAME = "";
        
        /**
         * How many indirections (asterisks) are declared for this variable
         */
        private int pointerLevel;
        private Sign sign;
        private SimplifiedType simplifiedType;
        private Token varName;
        private Token assignmentOperator;
        private Expression value;
        
        /**
         * If the SimplifiedType is CLASS, then this is set to the classID, 
         *  otherwise it's Long.MIN_VALUE.
         */
        private long classID = Long.MIN_VALUE;
        
        public VarDeclaration(Sign sign, SimplifiedType simplifiedType, 
            int pointerLevel, Token varName, Token assignmentOperator, 
            Expression value)
        {
            this.sign = sign;
            this.simplifiedType = simplifiedType;
            this.pointerLevel = pointerLevel;
            this.varName = varName;
            this.value = value;
            this.assignmentOperator = assignmentOperator;
        }
        
        public Sign getSign()
        {
            return sign;
        }
        
        public SimplifiedType getSimplifiedType()
        {
            return simplifiedType;
        }
        
        public int getPointerLevel()
        {
            return pointerLevel;
        }
        
        public Token getName()
        {
            return varName;
        }
        
        public String getNameText()
        {
            return varName.getTextContent();
        }
        
        public Expression getValue()
        {
            return value;
        }
        
        public Token getAssignmentOperator()
        {
            return assignmentOperator;
        }
        
        public boolean isReturnVar()
        {
            return getNameText().equals(RETURN_VAR_NAME);
        }
        
        public void setClassID(long classID)
        {
            this.classID = classID;
        }

        public long getClassID()
        {
            return classID;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitVarDeclaration(this);
        }
    }
    
    public static class Package extends Statement
    {
        private String fullyQualifiedPackageName;
        
        public Package(String fullyQualifiedPackageName)
        {
            this.fullyQualifiedPackageName = fullyQualifiedPackageName;
        }
        
        public String getFullyQualifiedPackageName()
        {
            return fullyQualifiedPackageName;
        }
        
        @Override
        <T> T accept(Visitor<T> visitor)
        {
            return visitor.visitPackage(this);
        }
    }
    
    abstract <T> T accept(Visitor<T> visitor);
    
    public boolean needsSemicolon()
    {
        return true;
    }
}