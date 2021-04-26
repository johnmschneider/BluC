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
package bluC.transpiler.scope;

import bluC.transpiler.Scope;
import static bluC.transpiler.scope.ScopeTestUtils.createRootScope;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author John Schneider
 */
public class RootTest
{
    @Test
    public void testRootScopeGetScopeType()
    {
        Scope root = createRootScope();

        /**
         * Second parameter should be Scope.NO_SCOPE_TYPE. Not using the
         * defined constants to ensure changes to them are explicit.
         */
        assertEquals(null, root.getScopeType());
    }

    @Test
    public void testRootGetParent()
    {
        Scope rootParentScope = createRootScope().getParent();
        assertEquals(null, rootParentScope);
    }
    
    @Test
    public void testToString()
    {
        Scope root;
        String expectedToString;

        root                = createRootScope();
        expectedToString    = "ROOT_SCOPE";

        assertEquals(
            (Object) expectedToString, (Object) root.toString());
    }
}
