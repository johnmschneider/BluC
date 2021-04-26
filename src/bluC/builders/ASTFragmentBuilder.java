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

/*
 * Builds a part (fragment) of an AST.
 */
package bluC.builders;

import bluC.parser.Parser;
import bluC.transpiler.Lexer;
import bluC.transpiler.statements.Statement;
import bluC.transpiler.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author John Schneider
 */
public class ASTFragmentBuilder
{
    private Lexer   lexer;
    private Parser  parser;
    
    /**
     * The relative (or absolute) file path of the source code (can be a fake 
     *  file path for unit testing). Must also include the file name and
     *  extension at the end of the file path.
     */
    private String  filePath;
    
    /**
     * The BluC source code, each line separated by a newline character ("\n")
     */
    private String  fileContents;
    
    private ArrayList<Statement>
                    abstractSyntaxTree;
    
    public String getFilePath()
    {
        return filePath;
    }

    public ASTFragmentBuilder setFilePath(String filePath)
    {
        this.filePath = filePath;
        return this;
    }

    public String getFileContents()
    {
        return fileContents;
    }

    public ASTFragmentBuilder setFileContents(String fileContents)
    {
        this.fileContents = fileContents;
        return this;
    }
    
    /**
     * Builds and returns an AST created by the BluC source code in
     *  <b>fileContents</b>.
     * 
     * @return the AST created from parsing the source code in
     *      <b>fileContents</b>
     */
    public ArrayList<Statement> build()
    {
        String[]    contentsAsArray;
        List<String>
                    contentsAsList;
        ArrayList<String>
                    contentsAsArrayList;
        ArrayList<Token>
                    lexedTokens;
        
        contentsAsArray = fileContents.split("\n");
        contentsAsList  = Arrays.asList(contentsAsArray);
        
        contentsAsArrayList = new ArrayList<>();
        contentsAsArrayList.addAll(contentsAsList);
        
        lexer       = new Lexer(filePath, contentsAsArrayList);
        lexedTokens = lexer.lex();
        
        parser              = new Parser(lexedTokens);
        abstractSyntaxTree  = parser.parse();
        
        return abstractSyntaxTree;
    }
    
    public void injectCurrentASTIntoAnother(
        int injectionStartIndex, ArrayList<Statement> otherAst)
    {
        otherAst.addAll(injectionStartIndex, abstractSyntaxTree);
    }
    
    public Lexer getLexer()
    {
        return lexer;
    }

    public Parser getParser()
    {
        return parser;
    }
    
}
