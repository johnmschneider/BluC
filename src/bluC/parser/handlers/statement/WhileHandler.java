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
import bluC.Result;
import bluC.builders.TokenBuilder;
import bluC.parser.Parser;
import bluC.parser.handlers.statement.StatementHandler.JustParseExprResult;
import bluC.transpiler.Scope;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.statements.blocks.While;
import bluC.transpiler.Token;
import java.util.ArrayList;

/**
 *
 * @author John Schneider
 */
public class WhileHandler
{
    private final Parser            parser;
    private final StatementHandler  statementHandler;
    private final BlockHandler      blockHandler;
    private final LoopHandlerUtils  loopUtils;
    private final String            nameOfLoopType = "while";
    
    private static enum FoundStatementEndErrCode
    {
        UNEXPECTED_OPEN_BRACE,
        UNEXPECTED_CLOSE_BRACE,
        NOT_A_STATEMENT_END;
    }
    
    private static class FoundStatementEndResult extends
        Result<FoundStatementEndErrCode>
    {
        public FoundStatementEndResult(FoundStatementEndErrCode errCode)
        {
            super(errCode);
        }
    }
    
    public WhileHandler(
        Parser parser, StatementHandler statementHandler,
        BlockHandler blockHandler)
    {
        this.parser             = parser;
        this.statementHandler   = statementHandler;
        this.blockHandler       = blockHandler;
        loopUtils               = new LoopHandlerUtils(parser, blockHandler);
        loopUtils.setNameOfLoopType(nameOfLoopType);
    }
    
    /**
     * Leaves parser on the end of the loop (either the closing brace "}" 
     *  or ";" for single-statement loops)
     */
    public Statement handleWhileOrHigher()
    {
        int startIndex = parser.getCurTokIndex();
        
        if (parser.peekMatches("while"))
        {
            return handleWhileLoop();
        }
        
        parser.setToken(startIndex);
        return statementHandler.handlePackage();
    }
    
    /**
     * Leaves parser on the end of the while loop (either the closing brace "}" 
     *  or ";" for single-statement loops).
     * 
     * Expects to start at the token immediately before the while loop.
     */
    private Statement handleWhileLoop()
    {
        int startIndex;
        
        startIndex = parser.getCurTokIndex();
        
        // move to "while" token
        parser.nextToken();
        
        if (parser.peekMatches("("))
        {
            // consume "while" token
            parser.nextToken();
            return handleValidConditionStart(startIndex);
        }
        else
        {
            return handleMalformedConditionStart();
        }
    }
    
    /**
     * Expects to be on "while" token. Ends on the last token of the loop.
     */
    private Statement handleMalformedConditionStart()
    {
        While synchronizedLoop;
        
        synchronizedLoop = new While(parser.getCurTokLineIndex());
        
        Logger.err(parser.peek(), "Expected \"(\" to open condition for " +
            "while loop");
        loopUtils.synchronizeParserFromBadCondition(
            parser.getCurTokIndex(), synchronizedLoop);
        
        return synchronizedLoop;
    }
    
    /**
     * Expects to be on "while" token.  Ends on the last token of the loop.
     */
    private Statement handleValidConditionStart(int startTokenIndex)
    {
        ArrayList<Token> 
                tokensInCondition;
        boolean conditionalEndFound;
        long    startingLineIndex;
        
        /**
         * Conditional Start Index
         */
        int     condStartIndex;
        int     openParenCount;
        
        conditionalEndFound = false;
        condStartIndex      = startTokenIndex + 2;
        startingLineIndex   = parser.getCurTokLineIndex();
        tokensInCondition   = new ArrayList<>();
        openParenCount      = 1;
        
        // consume "(" token
        parser.nextToken();
        
        // look for closing ")" token
        while (!parser.atEOF())
        {
            if (parser.curTextMatches(")"))
            {
                openParenCount --;
                
                if (openParenCount == 0)
                {
                    conditionalEndFound = true;
                    break;
                }
            }
            else if (parser.curTextMatches("("))
            {
                openParenCount++;
            }
            
            tokensInCondition.add(parser.getCurToken());
            parser.nextToken();
        }
        
        parser.setToken(condStartIndex);
        if (conditionalEndFound)
        {
            return handleProperlyEnclosedConditional(startingLineIndex,
                startTokenIndex);
        }
        else
        {
            return new While(parser.getCurTokLineIndex());
        }
    }
    
    /**
     * To be used with while loops whose conditionals have both opening paren 
     *  "(" and closing paren ")" but whose expression is not yet validated.
     * 
     * Expects to be on opening "(" token of the conditional. Ends on end of 
     *  loop.
     */
    private While handleProperlyEnclosedConditional(long startingLineIndex,
        long startingTokenIndex)
    {
        JustParseExprResult conditionalResult;
        conditionalResult = parser.justParseExpression();

        if (conditionalResult.getWasSuccessful())
        {
            return handleSuccessfulConditional(
                startingLineIndex, conditionalResult);
        }
        else
        {
            return handleBadConditional(startingTokenIndex, conditionalResult);
        }
    }
    
    /**
     * Expects to be on token immediately before the closing ")" token of the
     *  conditional. Ends on end of loop.
     */
    private While handleSuccessfulConditional(
        long startingLineIndex, JustParseExprResult conditionalResult)
    {
        While theLoop = new While(startingLineIndex);
        
        theLoop.setExitCondition(conditionalResult.getData());
        
        // set token to ")"
        parser.nextToken();
        
        makeSureLoopIsDesugared();
        
        // set token to "{" (loop is desugared so this must be there)
        parser.nextToken();
        
        Token openBrace = parser.getCurToken();
        
        parser.pushScope(new Scope(parser.getCurrentScope(), theLoop));
        blockHandler.addStatementsToBlock(openBrace, theLoop);
        parser.popScope(parser.peek());
        
        return theLoop;
    }
    
    private While handleBadConditional(
        long startingTokenIndex, JustParseExprResult conditionalResult)
    {
        switch (conditionalResult.getErrCode())
        {
            case MALFORMED_EXPRESSION:
                Logger.err(parser.getCurToken(), "while loop has malformed " +
                        "expression where the conditonal should have been");
                break;
            case UNEXPECTED_END_OF_STATEMENT:
                handleUnexpectedEOS(startingTokenIndex);
                break;
            default:
                /**
                 * This really shouldn't happen unless a new result type is
                 *  added and this function isn't updated appropriately
                 */
                Logger.err(parser.getCurToken(), "(FATAL error in compiler): " +
                    "conditional section of loop produced an unexpected " +
                    "parse error called \"" +
                    conditionalResult.getErrCode().name() + "\" which the " +
                    "compiler didn't recognize");
                break;
        }
        
        While synchronizedLoop = new While(parser.getCurTokLineIndex());
        loopUtils.synchronizeParserFromBadCondition(
            (int) startingTokenIndex, synchronizedLoop);
        
        return synchronizedLoop;
    }
    
    /**
     * Handle Unexpected End Of Statement.
     * 
     * Jumps to startingTokenIndex + 1.
     * 
     * Ends on the token before the opening parenthesis "(" of the conditional.
     */
    private void handleUnexpectedEOS(long startingTokenIndex)
    {
        boolean isBadExpression = false;
        
        parser.setToken((int) startingTokenIndex + 1);
        
        int openParens = 1;
        while (!parser.atEOF())
        {
            if (parser.peekMatches("("))
            {
                openParens++;
            }
            else if (parser.peekMatches(")"))
            {
                openParens--;
                
                if (openParens == 0)
                {
                    isBadExpression = true;
                    break;
                }
            }
            else if (parser.peekMatches(";", "{", "}"))
            {
                break;
            }
            
            parser.nextToken();
        }
        
        if (isBadExpression)
        {
            Logger.err(parser.getCurToken(), "while loop has an " + 
                "erroneous expression in the conditional");
        }
        else
        {
            Logger.err(parser.getCurToken(), "while loop has an " + 
                "unexpected end-of-statement in the conditional, which " + 
                "only accepts expressions");
        }
        
        parser.setToken((int) startingTokenIndex);
    }
    
    /**
     * Expects to be on the token immediately after the end of the conditional
     *  ")".
     * 
     * Leaves parser on the token before the opening brace "{", which should be
     *  the end of the conditional ")", and also the same location that the
     *  parser was at when this function was called.
     */
    private void makeSureLoopIsDesugared()
    {
        int     conditionalEndIndex     = parser.getCurTokIndex() - 1;
        boolean loopNeedsDesugared      = false;
        boolean isSyntacticSugarValid   = false;
        
        if (!parser.peekMatches("{"))
        {
            loopNeedsDesugared      = true;
            isSyntacticSugarValid   = isSyntacticSugarValid(
                conditionalEndIndex);
        }
        
        parser.setToken(conditionalEndIndex + 1);
        
        if (loopNeedsDesugared && isSyntacticSugarValid)
        {
            desugarSingleStatementWhileLoop(conditionalEndIndex);
        }
        // else it's either already desugared or a parse error that is
        //  already synchronized
    }
    
    private boolean isSyntacticSugarValid(int conditionalEndIndex)
    {
        FoundStatementEndResult 
                foundEnd;
        boolean errCodeWasParseError = false;
        
        while (!parser.atEOF())
        {
            foundEnd = foundStatementEnd(conditionalEndIndex);

            if (foundEnd.getWasSuccessful())
            {
                break;
            }
            else
            {
                errCodeWasParseError = handleEndErrCode(
                    foundEnd, conditionalEndIndex);
                
                if (errCodeWasParseError)
                {
                    break;
                }
            }
        }

        return errCodeWasParseError;
    }
    
    private boolean handleEndErrCode(
        FoundStatementEndResult foundEnd, int conditionalEndIndex)
    {
        boolean errCodeWasParseError = true;
        
        switch (foundEnd.getErrCode())
        {
            case UNEXPECTED_OPEN_BRACE:
            case UNEXPECTED_CLOSE_BRACE:
                loopUtils.synchronizeParserFromBadBlock(conditionalEndIndex);
                break;

            case NOT_A_STATEMENT_END:
                /**
                 * No error handling required, we just didn't hit a
                 *  valid statement end yet. I explicitly put this case
                 *  in, however, to document that this is expected
                 *  behavior. 
                 */
                errCodeWasParseError = false;
                break;
        }
        
        return errCodeWasParseError;
    }
    
    /**
     * Checks if the parser's current token indicates that this while loop
     *  has ended and was a single statement. Expects to be on a token after
     *  the end of the conditional ")" but before the end of a single-statement
     *  loop ";".
     * 
     * Doesn't change the token index the parser is on.
     */
    private FoundStatementEndResult foundStatementEnd(int conditionalEndIndex)
    {
        FoundStatementEndResult result = new FoundStatementEndResult(
            FoundStatementEndErrCode.NOT_A_STATEMENT_END);

        if (parser.peekMatches("{"))
        {
            /**
             * We've already confirmed that the opening brace isn't
             *  where it should be, and you can't have a block on a
             *  single-line statement, so this has to be an error.
             */
            loopUtils.
                synchronizeParserFromBadBlock(conditionalEndIndex);
            result.setErrCode(
                FoundStatementEndErrCode.UNEXPECTED_OPEN_BRACE);
        }
        else if (parser.peekMatches("}"))
        {
            loopUtils.synchronizeParserFromBadBlock(conditionalEndIndex);
            result.setErrCode(
                FoundStatementEndErrCode.UNEXPECTED_CLOSE_BRACE);
        }
        else if (parser.peekMatches(";"))
        {
            result.setWasSuccessful(true);
        }
        
        return result;
    }
    
    private void desugarSingleStatementWhileLoop(int conditionalEndIndex)
    {
        // insert artificial "{" and "} so the block handler can
        //  properly add the statement to the while block
        TokenBuilder    tokBuilder;
        Token           openBrace;
        Token           closeBrace;

        // grab line data from this token
        parser.setToken(conditionalEndIndex);

        tokBuilder = new TokenBuilder();
        openBrace = tokBuilder.
            setFileName     (parser.getCurToken().getFilepath()). 
            setLineIndex    ((int) parser.getCurTokLineIndex()).
            setTextContent  ("{"). 
            build();
        closeBrace = tokBuilder. 
            setTextContent("}").
            build();

        parser.addToken(closeBrace, parser.getCurTokIndex() + 1);
        parser.addToken(openBrace, conditionalEndIndex + 1);
    }
}
