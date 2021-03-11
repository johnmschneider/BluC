package bluC.parser.handlers.statement;

import bluC.Logger;
import bluC.transpiler.Scope;
import bluC.transpiler.Statement;
import bluC.transpiler.Token;
import bluC.parser.Parser;

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
    
    public Statement.Block handleBlock(Token openBrace)
    {
        Statement.Block newBlock = new Statement.Block(
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
     */
    public void addStatementsToBlock(
        Token openBrace, Statement.Block block)
    {
        boolean isSuccessful = false;
        
        while (!parser.atEOF())
        {
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
            Logger.err(next, "Expected \"}\" to close \"" + openBrace.
                getTextContent() + "\" on line " + 
                (openBrace.getLineIndex() + 1));
        }
    }
}
