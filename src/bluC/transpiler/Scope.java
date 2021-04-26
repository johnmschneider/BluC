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

package bluC.transpiler;

import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.vars.VarDeclaration;
import bluC.transpiler.statements.blocks.ClassDef;
import bluC.transpiler.statements.blocks.Function;
import bluC.transpiler.statements.blocks.StructDef;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class Scope
{
    public static final Scope       NO_PARENT      = null;
    public static final Statement   NO_SCOPE_TYPE  = null;
    
    private static final String     ROOT_SCOPE_TO_STRING = "ROOT_SCOPE";
            
    private final Statement scopeType;
    private final Scope     parent;
    private final ArrayList<VarDeclaration>
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
    
    public ArrayList<VarDeclaration> getVariablesInThisScope()
    {
        return variablesInThisScope;
    }
    
    public void addVariableToScope(VarDeclaration variable)
    {
        variablesInThisScope.add(variable);
    }
    
    public String getPrettyScopeName()
    {
        Statement type = getScopeType();
        
        if (type == NO_SCOPE_TYPE)
        {
            if (parent == NO_PARENT)
            {
                return ROOT_SCOPE_TO_STRING;
            }
            else
            {
                new IllegalArgumentException("Tried to print the state of an " +
                    "invalid scope (type == NO_SCOPE_TYPE but the parent " + 
                    "isn't NO_PARENT, indicating that this scope is not " +
                    "supposed to be a ROOT scope).").printStackTrace();
                
                /**
                 * I am pretty sure the JVM will terminate execution once a new
                 *  exception is created (even without throwing it) but return
                 *  this just in case we are able to continue and parse more
                 *  errors
                 */
                return "(?invalid child of \"" + parent.getPrettyScopeName() + 
                    "\"?)";
            }
        }
        else if (type instanceof Function)
        {
            return ((Function) type).getNameText();
        }
        else if (type instanceof ClassDef)
        {
            return ((ClassDef) type).getClassNameText();
        }
        else if (type instanceof StructDef)
        {
            new UnsupportedOperationException("getPrettyScopeName not " +
                "implemented for StructDef's yet").printStackTrace();
            return "(StructDef)";
        }
        else
        {
            return "(" + type.getClass().getName().replace(".", "_") + ")";
        }
    }
    
    @Override
    public String toString()
    {
        Scope       curParentNode;
        String      curScopeName;
        String      output;
        
        curParentNode = parent;
        
        if (parent == NO_PARENT)
        {
            // root scope
            return ROOT_SCOPE_TO_STRING;
        }
        
        curScopeName    = getPrettyScopeName();
        output          = curScopeName + ".\n";
        
        while (curParentNode != NO_PARENT)
        {
            curScopeName    = curParentNode.getPrettyScopeName();
            
            output          = curScopeName + ".\n" + output;
            curParentNode   = curParentNode.getParent();
        }
        
        return output;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.scopeType);
        hash = 59 * hash + Objects.hashCode(this.parent);
        hash = 59 * hash + Objects.hashCode(this.variablesInThisScope);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Scope other = (Scope) obj;
        if (!Objects.equals(this.scopeType, other.scopeType))
        {
            return false;
        }
        if (!Objects.equals(this.parent, other.parent))
        {
            return false;
        }
        if (!Objects.equals(this.variablesInThisScope, other.variablesInThisScope))
        {
            return false;
        }
        return true;
    }
    
    
}
