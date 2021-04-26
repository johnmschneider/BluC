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

import java.util.Objects;

/**
 *
 * @author John Schneider
 */
public class TokenInfo
{
    public static final String NO_TEXT_CONTENT = "<n/a>";
    
    private String  textContent;
    private boolean wasEmittedByCompiler;
    
    public TokenInfo(String textContent, boolean wasEmittedByCompiler)
    {
        this.textContent            = textContent;
        this.wasEmittedByCompiler   = wasEmittedByCompiler;
    }
    
    
    public String getTextContent()
    {
        return textContent;
    }

    public void setTextContent(String textContent)
    {
        this.textContent = textContent;
    }

    public boolean getWasEmittedByCompiler()
    {
        return wasEmittedByCompiler;
    }

    public void setWasEmittedByCompiler(boolean wasEmittedByCompiler)
    {
        this.wasEmittedByCompiler = wasEmittedByCompiler;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof TokenInfo)
        {
            TokenInfo otherInfo = (TokenInfo) other;
            
            return
                textContent.equals(otherInfo.getTextContent()) &&
                wasEmittedByCompiler == otherInfo.getWasEmittedByCompiler();
        }
        else 
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.textContent);
        hash = 67 * hash + (this.wasEmittedByCompiler ? 1 : 0);
        return hash;
    }
}
