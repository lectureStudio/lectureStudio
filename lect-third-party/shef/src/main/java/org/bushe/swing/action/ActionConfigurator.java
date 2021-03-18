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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * This class takes an action and configures it to make prototypes of the
 * actions according to a look and feel.  By default, the Java Look and Feel
 * Design Guidelines are used.
 * @deprecated Use ActionLookAndFeels instead.  This was depecated so as
 * not to confuse xml-action's ActionManager's configureXXX methods, which
 * are very different in function.
 * @author Michael Bushe
 * @version 1.0
 */
public class ActionConfigurator {

    public static final String ACTION_NEW = "New";
    public static final String ACTION_SAVE = "Save";
    public static final String ACTION_SAVE_AS = "Save As...";
    public static final String ACTION_EXIT = "Exit";
    public static final String ACTION_PREFERENCES = "Preferences";
    public static final String ACTION_PRINT = "Print";
    public static final String ACTION_COPY = "Copy";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_ABOUT = "About";
    public static final String ACTION_USER_GUIDE = "User's Guide";
    public static final String ACTION_REFRESH = "Refresh";

    /** Map of Maps, each one keyed by action name, */
    protected static Map sLookAndFeel = new HashMap();

    /** Same as above, except a member variable, settable in the constructor.*/
    static Map mLookAndFeel;

    static {
        addLookAndFeelActionEntry(ACTION_NEW, "New", "New", "New",
                                  new ImageIcon(ActionConfigurator.class.
                                                getResource(
            "/toolbarButtonGraphics/general/New16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_N));
        addLookAndFeelActionEntry(ACTION_SAVE, "Save", "Save", "Save",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Save16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_S));
        addLookAndFeelActionEntry(ACTION_SAVE_AS, "Save As", "Save As", "Save As",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/SaveAs16.gif")),
                                  null,
                                  new Integer(KeyEvent.VK_A));
        addLookAndFeelActionEntry(ACTION_EXIT, "Exit", "Exit", "Exit",
                                  null,
                                  null,
                                  new Integer(KeyEvent.VK_X));
        addLookAndFeelActionEntry(ACTION_PREFERENCES, "Preferences", "Preferences", "Preferences",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Preferences16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_R));
        addLookAndFeelActionEntry(ACTION_PRINT, "Print", "Print", "Print",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Print16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_P));
        addLookAndFeelActionEntry(ACTION_COPY, "Copy", "Copy", "Copy",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Copy16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_C));
        addLookAndFeelActionEntry(ACTION_DELETE, "Delete", "Delete", "Delete",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Delete16.gif")),
                                  KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK),
                                  new Integer(KeyEvent.VK_D));
        addLookAndFeelActionEntry(ACTION_ABOUT, "About", "About", "About",
                                  null,
                                  null,
                                  new Integer(KeyEvent.VK_A));
        addLookAndFeelActionEntry(ACTION_REFRESH, "Refresh", "Refresh", "Refresh",
                                  new ImageIcon(ActionConfigurator.class.getResource(
            "/toolbarButtonGraphics/general/Refresh16.gif")),
                                  KeyStroke.getKeyStroke((char) KeyEvent.VK_F5),
                                  new Integer(KeyEvent.VK_R));
    }

    /**
     * Add an entry in the static look and feel entry set.  This allows
     * the configurator to have a sort of "look and feel" defined for action -
     * which ones are available, their icons, names, etc.
     * @param name the action name
     * @param cmd the caction command name
     * @param desc the short description of the action
     * @param longDesc the long description of the action
     * @param icon the icon description of the action
     * @param acc the action's accelerator key (shortcut)
     * @param mnemonic the action's mnemonic (underlined letter in a menu)
     */
    public static void addLookAndFeelActionEntry(String name, String cmd, String desc,
                                                 String longDesc,
                                                 ImageIcon icon, KeyStroke acc, Integer mnemonic) {
        HashMap valuesMap = new HashMap();
        valuesMap.put(Action.NAME, name);
        valuesMap.put(Action.ACTION_COMMAND_KEY, cmd);
        valuesMap.put(Action.SHORT_DESCRIPTION, desc);
        valuesMap.put(Action.LONG_DESCRIPTION, longDesc);
        valuesMap.put(Action.SMALL_ICON, icon);
        valuesMap.put(Action.ACCELERATOR_KEY, acc);
        valuesMap.put(Action.MNEMONIC_KEY, mnemonic);
        sLookAndFeel.put(name, valuesMap);
    }


    /**
     * Constructor that uses the standard look and feel.  This is not necessary,
     * since the static methods of this class are equavalent to using an instance
     * with the null, or standard look and feels.  Added for consistency.
     */
    public ActionConfigurator() {
        mLookAndFeel = sLookAndFeel;
    }

    /**
     * Constructor that uses a look and feel other than the standard look and
     * feel.
     * @param lookAndFeel a Map of Maps.  The Map is keyed by action name,
     * the values are Maps - name-value pairs that are used to configure a
     * action by calling putValue with the name and the value of the action.
     */
    public ActionConfigurator(Map lookAndFeel) {
        mLookAndFeel = lookAndFeel;
    }

    /**
     * Configures an action by setting the properties (putValue) for the look
     * and feel of this Configurator.  The default L&F is the Java look and
     * feel, but another look and feel can be set during construction.
     * Essentially turns a raw action into a prototypical one.
     * @param actionConstant one of the constants defined for the configurator's
     * look and feel.  By default, use a constant defined in this class with the
     * default look and feel.
     * @param action the action to modify.
     * @return true is successful, false if no values were specifed in the
     * look and feel for the actionConstant
     */
    public boolean configureActionRespectLookAndFeel(String actionConstant, Action action) {
        return configureAction(mLookAndFeel, actionConstant, action);
    }


    /**
     * Same as {@link #configureAction(Map, String, Action)
     * configureAction(Map, String, Action)} except the Map is
     * null, therefore it uses the static look and feel.
     * @param actionConstant the name of the action
     * @param action and action to modify
     * @return an action configured to the look and feel set statically
     */
    public static boolean configureAction(String actionConstant, Action action) {
        return configureAction(sLookAndFeel, actionConstant, action);
    }

    /**
     * Essentially turns a raw action into a prototypical one.
     * Configures an action by setting the properties (putValue) for the look
     * and feel of this Configurator.  The default L&F is the Java look and
     * feel, but another look and feel can be set during construction.
     * Essentially turns a raw action into a prototypical one.
     * @param lookAndFeel see constuctor for description of structure.
     * @param actionConstant one of the constants defined for the configurator's
     * look and feel.  By default, use a constant defined in this class with the
     * default look and feel.
     * @param action the action to modify.
     * @return true is successful, false if no values were specifed in the
     * look and feel for the actionConstant
     */
    public static boolean configureAction(Map lookAndFeel, String actionConstant, Action action) {
        if (lookAndFeel == null) {
            lookAndFeel = sLookAndFeel;
        }
        Map values = (Map) lookAndFeel.get(actionConstant);
        if (values == null) {
            return false;
        } else {
            Iterator iter = values.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = values.get(key);
                action.putValue(key, value);
            }
            return true;
        }
    }

}