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
public class TokenFileInfo
{
    public static final String  NO_FILEPATH = "n/a";
    public static final int     NO_LINE_INDEX = -1;
    
    private String  filePath;
    private int     lineIndex;
    
    public TokenFileInfo(String filePath, int lineIndex)
    {
        this.filePath   = filePath;
        this.lineIndex  = lineIndex;
    }

    public String getFilePath()
    {
        return filePath;
    }
    
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    
    public int getLineIndex()
    {
        return lineIndex;
    }
    
    public void setLineIndex(int lineIndex)
    {
        this.lineIndex = lineIndex;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof TokenFileInfo)
        {
            TokenFileInfo otherInfo = (TokenFileInfo) other;
            
            return 
                filePath.equals(otherInfo.getFilePath()) &&
                lineIndex == otherInfo.getLineIndex();
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.filePath);
        hash = 83 * hash + this.lineIndex;
        return hash;
    }
}
