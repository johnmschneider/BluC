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

package bluC.builders;

import bluC.transpiler.Token;
import bluC.transpiler.TokenFileInfo;
import bluC.transpiler.TokenInfo;

/**
 * Builds a token.
 * 
 * Requires at least these parameters:
 * - fileName
 * - lineIndex
 * - textContent
 * 
 * @author John Schneider
 */
public class TokenBuilder
{
    private String  fileName;
    private int     lineIndex;
    private String  textContent;
    private boolean wasEmittedByCompiler;

    public TokenBuilder()
    {
        fileName                = TokenFileInfo.NO_FILEPATH;
        lineIndex               = TokenFileInfo.NO_LINE_INDEX;
        textContent             = TokenInfo.NO_TEXT_CONTENT;
        wasEmittedByCompiler    = false;
    }
    
    
    public String getFileName()
    {
        return fileName;
    }

    public TokenBuilder setFileName(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    public int getLineIndex()
    {
        return lineIndex;
    }

    public TokenBuilder setLineIndex(int lineIndex)
    {
        this.lineIndex = lineIndex;
        return this;
    }
    
    public TokenBuilder setWasEmittedByCompiler(
        boolean wasEmittedByCompiler)
    {
        this.wasEmittedByCompiler = wasEmittedByCompiler;
        return this;
    }
    
    public String getTextContent()
    {
        return textContent;
    }

    public TokenBuilder setTextContent(String textContent)
    {
        this.textContent = textContent;
        return this;
    }
    
    public boolean wasEmittedByCompiler()
    {
        return wasEmittedByCompiler;
    }
    
    /**
     * Builds the token represented by the current parameters.
     */
    public Token build()
    {
        TokenFileInfo fileInfo;
        TokenInfo     tokenInfo;
        
        fileInfo    = new TokenFileInfo(fileName, lineIndex);
        tokenInfo   = new TokenInfo(textContent, wasEmittedByCompiler);
        
        return new Token(tokenInfo, fileInfo);
    }
    
}
