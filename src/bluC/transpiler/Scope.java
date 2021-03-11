package bluC.transpiler;

import java.util.ArrayList;

/**
 *
 * @author John Schneider
 */
public class Scope
{
    public static final Scope       ROOT_SCOPE      = null;
    public static final Statement   NO_SCOPE_TYPE   = null;
    
    private final Statement scopeType;
    private final Scope     parent;
    private final ArrayList<Statement.VarDeclaration>
                            variablesInThisScope;
            
    public Scope(Scope parent, Statement scopeType)
    {
        this.parent             = parent;
        this.scopeType          = scopeType;
        variablesInThisScope    = new ArrayList<>();
    }
    
    public Statement getScopeType()
    {
        return scopeType;
    }
    
    public Scope getParent()
    {
        return parent;
    }
    
    public ArrayList<Statement.VarDeclaration> getVariablesInThisScope()
    {
        return variablesInThisScope;
    }
    
    public void addVariableToScope(Statement.VarDeclaration variable)
    {
        variablesInThisScope.add(variable);
    }
    
    @Override
    public String toString()
    {
        Scope       curParentNode   = parent;
        Statement   curScopeType    = getScopeType();
        String      curTypeName     = curScopeType.getClass().getTypeName();
        String      output          = curTypeName;
        
        while (curParentNode != ROOT_SCOPE)
        {
            curScopeType    = curParentNode.getScopeType();
            curTypeName     = curScopeType.getClass().getTypeName();
        
            output          = curTypeName + ".\n" + output;
            curParentNode   = curParentNode.getParent();
        }
        
        output = "ROOT_SCOPE.\n" + output;
        
        return output;
    }
}
