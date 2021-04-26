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

/**
 *
 * @author John Schneider
 */
public class ScopeTestUtils
{
    public static Scope createRootScope()
    {
        /**
         * Parameters should be Scope.ROOT_SCOPE and Scope.NO_SCOPE_TYPE,
         *  respectively.
         * 
         * Not using the defined constants to ensure that changes to them are
         *  explicit.
         */
        Scope root = new Scope(null, null);
        return root;
    }
}
