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
package org.bushe.swing;

import javax.swing.Icon;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Swing Utilities not in SwingUtilities
 * @author Michael Bushe michael@bushe.com
 */
public class SwingUtils {
    /**
     * Returns the Icon associated with the name of the resource.
     */
    public static Icon getIcon(String imagePath) {
        if (imagePath != null && !imagePath.equals("")) {
            URL url = SwingUtils.class.getResource(imagePath);
            if (url != null) {
                return new ImageIcon(url);
            }
        }
        return null;
    }
}
