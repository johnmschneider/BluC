package bluC.transpiler;

import java.util.ArrayList;

/**
 *
 * @author John Schneider
 */
public class Scope
{
    private Statement scopeType;
    private Scope parent;
    private ArrayList<Statement.VarDeclaration> variablesInThisScope;
            
    public Scope(Scope parent, Statement scopeType)
    {
        this.parent = parent;
        this.scopeType = scopeType;
        variablesInThisScope = new ArrayList<>();
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
}
