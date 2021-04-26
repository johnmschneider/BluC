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
package bluC.parser.handlers.expression;

import bluC.Result;
import bluC.parser.Parser;

/**
 * Contains utility classes for verifying if the operands of an equality
 *  comparison are valid to be used with the operator.
 *  
 * @author John Schneider
 */
public class EqualityOperandsValidator
{
    private final Parser parser;
    
    public static enum IsLeftOperandValidErrCode
    {
        
    }
    
    /**
     * Checks a statement that's already confirmed to be comparison. Expects to
     *  be on the start token of the expression
     */
    public static class IsLeftOperandValidResult extends
        Result<IsLeftOperandValidErrCode>
    {
        public IsLeftOperandValidResult(IsLeftOperandValidErrCode errCode)
        {
            super(errCode);
        }
    }
    
    public EqualityOperandsValidator(Parser parser)
    {
        this.parser = parser;
    }
    
    
    /**
     * Doesn't typecheck, just makes sure the type of the expression itself is
     *  valid.
     * 
     * Expects to be on the starting index of the equality expression 
     *  (i.e. the start of the left-hand expression of the equality
     *  operator).
     * 
     * Leaves parser on the equality operator.
     */
    public IsLeftOperandValidResult isLeftExprTypeValid()
    {
        IsLeftOperandValidResult res;
        new UnsupportedOperationException("not supported yet").printStackTrace();
        return null;
    }
}
