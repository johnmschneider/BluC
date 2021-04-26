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

package bluC.parser.exceptions;

import bluC.transpiler.Token;

/**
 *
 * @author John Schneider
 */
public class InvalidSizeModifier extends Exception
{
    private Token sizeMod1;
    private Token sizeMod2;
    private Token typeAttemptedToModify;
    
    public InvalidSizeModifier(Token sizeMod1, Token typeAttemptedToModify)
    {
        super("Attempted to use size modifier \"" + 
            sizeMod1.getTextContent() + "\" which is invalid for type \"" +
            typeAttemptedToModify.getTextContent() + "\"");
        
        this.sizeMod1 = sizeMod1;
        this.sizeMod2 = null;
        this.typeAttemptedToModify = typeAttemptedToModify;
    }
    
    public InvalidSizeModifier(Token sizeMod1, Token sizeMod2,
        Token typeAttemptedToModify)
    {
        super("Attempted to use size modifier \"" + 
            sizeMod1.getTextContent() + " " + sizeMod2.getTextContent() +
            "\" which is invalid for type \"" +
            typeAttemptedToModify.getTextContent() + "\"");
        
        this.sizeMod1 = sizeMod1;
        this.sizeMod2 = sizeMod2;
        this.typeAttemptedToModify = typeAttemptedToModify;
    }
    
    
    public Token getSizeMod1()
    {
        return sizeMod1;
    }
    
    public Token getSizeMod2()
    {
        return sizeMod2;
    }
}
