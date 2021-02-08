package bluC.transpiler;

import java.util.ArrayList;
import bluC.Logger;

public class CommentsRemover
{
    public static void run(String filePath, ArrayList<String> rawFileContents)
    {
        boolean multilineCommentIsOn = false;
        int multilineStartedAtLine = -1;
        int fileContentsSize = rawFileContents.size();
        for (int i = 0; i < fileContentsSize; i++)
        {
            String line = rawFileContents.get(i);
            int lineCommentIndex = line.indexOf("//");
            int multilineCommentIndex = line.indexOf("/*");
            
            if (multilineCommentIsOn)
            {
                int multilineEndIndex = line.indexOf("*/");
                if (multilineEndIndex != -1)
                {
                    //add two because "*/" is two chars long
                    rawFileContents.set(i, line.substring(
                        multilineEndIndex + 2));
                    multilineCommentIsOn = false;
                }
                else
                {
                    if (i == fileContentsSize - 1)
                    {
                        Logger.err(
                            new Token(
                                new TokenInfo(Token.EOF, false),

                                new TokenFileInfo(filePath, i)),

                            "expected \"*/\" to close \"/*\" at line " + 
                            multilineStartedAtLine);
                    }
                    //don't just delete the line so that
                    //  any errors during compilation will
                    //  align to the proper line of sourcecode
                    rawFileContents.set(i, "");
                }
            }
            else
            {
                //  handle line comment
                if (lineCommentIndex != -1)
                {
                    boolean lineCommentIsNotCommentedOut = true;

                    if (multilineCommentIndex != -1)
                    {
                        //see whether the line comment or
                        // multiline comment came first
                        if (lineCommentIndex > multilineCommentIndex)
                        {
                            //this line comment is commented out
                            lineCommentIsNotCommentedOut = false;
                        }
                    }

                    if (lineCommentIsNotCommentedOut)
                    {
                        //remove comment
                        rawFileContents.set(i, line.substring(
                            0, lineCommentIndex));
                    }
                }
                
                //  handle multiline comment
                if (multilineCommentIndex != -1)
                {
                    boolean multilineCommentIsNotCommentedOut = true;
                    
                    if (lineCommentIndex != -1)
                    {
                        //see whether the multilline comment or
                        // line comment came first
                        if (multilineCommentIndex > lineCommentIndex)
                        {
                            //this multiline comment is commented out
                            multilineCommentIsNotCommentedOut = false;
                        }
                    }
                    
                    if (multilineCommentIsNotCommentedOut)
                    {
                        //remove comment
                        rawFileContents.set(i, line.substring(
                            0, multilineCommentIndex));
                        multilineCommentIsOn = true;
                        multilineStartedAtLine = i + 1;
                    }
                }
            }//end (if (multilineCommentIsOn))-else
        } //end for loop
    }
}
