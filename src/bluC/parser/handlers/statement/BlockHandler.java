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

import bluC.Logger;
import bluC.transpiler.Scope;
import bluC.transpiler.Token;
import bluC.parser.Parser;
import bluC.transpiler.statements.blocks.Block;

/**
 *
 * @author John Schneider
 */
public class BlockHandler
{
    private final Parser parser;
    private final StatementHandler statementHandler;
            
    public BlockHandler(Parser parser, StatementHandler statementHandler)
    {
        this.parser = parser;
        this.statementHandler = statementHandler;
    }
    
    /**
     * Handles a block.
     * 
     * Expects to be on the token immediately before the opening brace "{".
     * 
     * Ends on token immediately before the closing brace "}" of the block. 
     *  
     * This differs from most other handlers because most blocks' last token
     *  is in fact the closing brace, and StatementHandler automatically moves
     *  to the next token after a statement end, and the parser operates on the
     *  *peeked* token.
     */
    public Block handleBlock(Token openBrace)
    {
        Block newBlock = new Block(
            openBrace.getLineIndex());
        
        //set cur token to "{"
        parser.nextToken();
        parser.pushScope(new Scope(parser.getCurrentScope(), newBlock));
        
        addStatementsToBlock(openBrace, newBlock);
        newBlock.setEndingLineIndex(parser.getCurTokLineIndex());
        
        parser.popScope(parser.peek());
        
        return newBlock;
    }
    
    /**
     * Parses all of the statements, adding them to <b>block</b>, until the
     *  closing brace of the Block is reached.
     * 
     * Expects the parser's current token to be the opening brace of the block
     *  ("{").
     * 
     * Ends on token immediately before the closing brace "}" of the block, or 
     *  the best guess for said token when there is a parsing error.
     */
    public void addStatementsToBlock(
        Token openBrace, Block block)
    {
        boolean isSuccessful = false;
        
        if (parser.peekMatches("}"))
        {
            Logger.warn(openBrace, "Empty block");
        }
        else
        {
            // add each token until end of brace
            while (!parser.atEOF())
            {
                // since block is also a statement type, this should handle any 
                //  nested blocks, ergo we don't have to worry about brace 
                //  matching here
                block.addStatement(statementHandler.handleStatement(true));

                if (parser.peekMatches("}"))
                {
                    isSuccessful = true;
                    break;
                }
            }

            if (!isSuccessful)
            {
                Token next = parser.peek();
                Logger.err(next, "Expected \"}\" to close block opening " +
                    "\"" + openBrace.getTextContent() + "\" on line " + 
                    (openBrace.getLineIndex() + 1));
            }
        }
    } // end function
    
}
