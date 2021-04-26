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
package bluC.parser.handlers.statement;

import bluC.parser.Parser;
import bluC.transpiler.statements.Statement;

/**
 *
 * @author John Schneider
 */
public class LoopHandler
{
    private final Parser            parser;
    private final StatementHandler  statementHandler;
    private final WhileHandler      whileHandler;
    private final BlockHandler      blockHandler;
    
    public LoopHandler(
        Parser parser, StatementHandler statementHandler, 
        BlockHandler blockHandler)
    {
        this.statementHandler   = statementHandler;
        this.parser             = parser;
        this.blockHandler       = blockHandler;
        this.whileHandler       = new WhileHandler(
            parser, statementHandler, blockHandler);
    }
    
    
    /**
     * Checks for any type of loop in the language (while, do-while, for,
     *  for each) and returns a Statement representing the AST of the loop,
     *  or a statement of higher precedence than all loops if the statement 
     *  isn't a loop.
     */
    public Statement handleLoopOrHigher()
    {
        return whileHandler.handleWhileOrHigher();
    }
    
    /* commented out to test while loop parsing
    *
    private Statement handleDoWhileOrHigher()
    {
        
    }
    
    private Statement handleForOrHigher()
    {
        
    }
    
    private Statement handleForEachOrHigher()
    {
        
    }*/
}
