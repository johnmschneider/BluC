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
package bluC;

/**
 * Basically a Result Type without a data element.
 * 
 * @author John Schneider
 */
public class Result<ErrorType>
{
    private boolean     wasSuccessful;
    private ErrorType   errCode;
    
    /**
     * Constructs the Result as if it failed. To make it a success, use
     *  .setWasSuccessful(true)
     */
    public Result(ErrorType errCode)
    {
        wasSuccessful   = false;
        this.errCode    = errCode;
    }
    

    public boolean getWasSuccessful()
    {
        return wasSuccessful;
    }

    public void setWasSuccessful(boolean wasSuccessful)
    {
        this.wasSuccessful = wasSuccessful;
    }

    public ErrorType getErrCode()
    {
        return errCode;
    }

    public void setErrCode(ErrorType errCode)
    {
        this.errCode = errCode;
    }
}
