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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author John Schneider
 */
public class TokenBuilderTest
{
    private TokenBuilder testThis;
    
    @Before
    public void setUp()
    {
        testThis = new TokenBuilder();
    }
    
    @After
    public void tearDown()
    {
    }
    
    
    @Test
    public void testGetFileName()
    {
        String expectedFileName = "abC33_.bluc";
        testThis.setFileName(expectedFileName);
        
        assertEquals(expectedFileName, testThis.getFileName());
    }

    @Test
    public void testSetFileName()
    {
        String expectedFileName = "filename.bluc";
        testThis.setFileName(expectedFileName);
        
        assertEquals(expectedFileName, testThis.getFileName());
    }

    @Test
    public void testGetLineIndex()
    {
        int expectedLineIndex = 24;
        testThis.setLineIndex(expectedLineIndex);
        
        assertEquals(expectedLineIndex, testThis.getLineIndex());
    }

    @Test
    public void testSetLineIndex()
    {
        int expectedLineIndex = 31;
        testThis.setLineIndex(expectedLineIndex);
        
        assertEquals(expectedLineIndex, testThis.getLineIndex());
    }

    @Test
    public void testSetWasEmittedByCompiler()
    {
        testThis.setWasEmittedByCompiler(true);
        assertEquals(true, testThis.wasEmittedByCompiler());
                
        testThis.setWasEmittedByCompiler(false);
        assertEquals(false, testThis.wasEmittedByCompiler());
    }
    
    @Test
    public void testWasEmittedByCompiler()
    {
        testThis.setWasEmittedByCompiler(false);
        assertEquals(false, testThis.wasEmittedByCompiler());
        
        testThis.setWasEmittedByCompiler(true);
        assertEquals(true, testThis.wasEmittedByCompiler());
    }
    
    @Test
    public void testGetTextContent()
    {
        String expectedText = "int";
        testThis.setTextContent(expectedText);
        
        assertEquals((Object) expectedText, (Object) testThis.getTextContent());
    }

    @Test
    public void testSetTextContent()
    {
        String expectedText = "char";
        testThis.setTextContent(expectedText);
        
        assertEquals((Object) expectedText, (Object) testThis.getTextContent());
    }

    @Test
    public void testBuild()
    {
        String          mockFileName;
        int             mockLineIndex;
        String          mockTextContent;
        boolean         mockWasEmittedByCompiler;
        
        TokenFileInfo   expFileInfo;
        TokenInfo       expInfo;
        
        Token           expected;
        Token           actual;
        
        mockFileName                = "testBuild.bluc";
        mockLineIndex               = 25;
        mockTextContent             = "long";
        mockWasEmittedByCompiler    = false;
        
        expFileInfo = new TokenFileInfo(mockFileName, mockLineIndex);
        expInfo     = new TokenInfo(mockTextContent, mockWasEmittedByCompiler);
        expected    = new Token(expInfo, expFileInfo);
        
        actual = testThis.
            setFileName             (mockFileName). 
            setLineIndex            (mockLineIndex).
            setTextContent          (mockTextContent). 
            setWasEmittedByCompiler (mockWasEmittedByCompiler). 
            build();
        
        assertEquals(expected, actual);
    }
    
}
