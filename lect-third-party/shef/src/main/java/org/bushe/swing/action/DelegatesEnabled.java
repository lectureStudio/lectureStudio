/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bushe.swing.action;

/**
 * Interface that allows delegation of the EnabledUpdater interface.
 * <p>
 * An action that implements this interface typically calls shouldBeEnabled() on
 * all it's delegates.  If they any return true, it enables itself.
 * @author Michael Bushe
 * @todo this interface could be made stonger with AND/OR semantics, like:
 * "if just one says it should be enabled, enable" or
 * "if just one says it should be disabled, disable"
 * "if this one says it should be disbaled, then disable no matter what the
 * others say"
 */
public interface DelegatesEnabled {
    public void addShouldBeEnabledDelegate(ShouldBeEnabledDelegate shouldBeEnabledDelegate);
    public void removeShouldBeEnabledDelegate(ShouldBeEnabledDelegate shouldBeEnabledDelegate);
}
