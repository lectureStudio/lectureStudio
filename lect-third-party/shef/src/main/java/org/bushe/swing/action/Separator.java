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
 * A class for separators in action lists.  Separators have weights to allow
 * their location to be specified relative to other actions in an action list
 * (and ths in the menus and toolbars created for them).
 *
 * @author Michael Bushe
 */
public class Separator {
    private String id;
    private Number weight;
    private boolean lineVisible;

    public Separator() {
        this(null, null, true);
    }

    public Separator(Number weight) {
        this(null, weight, true);
    }

    public Separator(String id, Number weight, boolean lineVisible) {
        this.id = id;
        this.weight = weight;
        this.lineVisible = lineVisible;
    }

    public String getId() {
        return id;
    }

    public Number getWeight() {
        return weight;
    }

    public void setWeight(Number weight) {
        this.weight = weight;
    }


    /**
     * Some UI Designers use two kinds of separators, thoe normal JSeparator
     * with a hard visible line, and other "grouping" separators with
     * no visible line.
     */
    public boolean isLineVisible() {
        return lineVisible;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Separator) {
            Separator s = (Separator) obj;
            if (id == null) {
                if (s.id != null) {
                    return false;
                }
            } else {
                if (!id.equals(s.id)) {
                     return false;
                }
            }
            if (weight == null) {
                if (s.weight != null) {
                    return false;
                }
            } else {
                if (!weight.equals(s.weight)) {
                     return false;
                }
            }
            return lineVisible == s.lineVisible;
        }
        return false;
    }
}
