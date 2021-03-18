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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Document;

/**
 * The ActionXMLReader reads sets of properties describing
 * <code>javax.swing.Action</code>s from an XML file.  This class is a
 * fork of the ActionManager from Mark Davidson of Sun's Swing team, though the
 * reader was stripped out from his ActionManager into this class to read an
 * extended format and allow the manager to be more flexible.
 * <p>
 * The Actions are specified in an XML configuration file. The schema for the
 * XML document contains three major elements.
 * <ul>
 *   <li><b>action</b> Represents the properties of an Action.
 *   <li><b>action-list</b> Represents lists and trees of actions which can be
 * used to construct user interface components like toolbars, menus and popups.
 *   <li><b>action-set</b> The document root which contains a set of
 * action-lists and actions.
 * </ul>
 * <p>
 * All of these elements have a unique id tag which is used by the
 * ActionXMLReader to reference the element. Refer to
 * <a href="res/action-set.dtd">action-set.dtd</a>
 * for details on the elements and attributes.
 * <p>
 * The order of an action in an action-list will reflect the order of the
 * Action based component in the container. A tree is represented as an
 * action-list that contains one or more action-lists.
 * <p>
 * An ActionAttributes instance is created for each set of Action properties
 * in the XML file.  The attributes must be registered as action prototypes
 * with the ActionManager to be useful.  ActionManager encompasses this behavior
 * directly like so:
 * <p>
 *  <pre>
 *   ActionManager.registerFromURL(myURL);
 *  </pre>
 * <p>
 * Once either is performed, the ActionManager can be used normally, like so:
 * <pre>
 *   ActionManager.getInstance().getAction(id).setActionCallback(this);
 *   // Change the state of the action:
 *   ActionManager.getInstance().getAction("new-action").setEnabled(newState);
 * </pre>
 * <p>
 * The ActionUIFactory can be used to create components from action id lists
 * registered from this class.
 *
 * @see ActionUIFactory
 * @see ActionManager
 * @see <a href="res/action-set.dtd">action-set.dtd</a>
 * @author Michael Bushe
 * @author Mark Davidson - stole his semi-working code from java.net
 */
public class ActionXMLReader {

    // Elements in the action-set.dtd
    public final static String ELEMENT_ACTION_SET = "action-set";
    public final static String ELEMENT_ACTION = "action";
    public final static String ELEMENT_ACTION_LIST = "action-list";
    public final static String ELEMENT_SEPARATOR = "separator";
    public final static String ELEMENT_GROUP = "group";
    public final static String ELEMENT_ROLE = "role";
    public final static String ELEMENT_NAME_VALUE_PAIR = "name-value-pair";
    public final static String ATTRIBUTE_DEFAULT_ACTION_CLASS = "defaultActionClass";
    public final static String ATTRIBUTE_TOOLBAR_BUTTON_PREF_WIDTH = "toolbarButtonPreferredWidth";
    public final static String ATTRIBUTE_TOOLBAR_BUTTON_PREF_HEIGHT = "toolbarButtonPreferredHeight";
    public final static String ATTRIBUTE_ACCEL = "accel";
    public final static String ATTRIBUTE_DESC = "desc";
    public final static String ATTRIBUTE_LONG_DESC = "longdesc";
    public final static String ATTRIBUTE_LGICON = "lgicon";
    public final static String ATTRIBUTE_ID = "id";
    public final static String ATTRIBUTE_COMMAND = "command";
    public final static String ATTRIBUTE_IDREF = "idref";
    public final static String ATTRIBUTE_MNEMONIC = "mnemonic";
    public final static String ATTRIBUTE_NAME = "name";
    public final static String ATTRIBUTE_ENABLED = "enabled";
    public final static String ATTRIBUTE_SELECTED = "selected";
    public final static String ATTRIBUTE_SMICON = "smicon";
    public final static String ATTRIBUTE_BUTTON_TYPE = "buttonType";
    public final static String ATTRIBUTE_TOOLBAR_SHOWS_TEXT = "toolbarShowsText";
    public final static String ATTRIBUTE_MENU_SHOWS_ICON = "menuShowsIcon";
    public final static String ATTRIBUTE_GROUP = "group";
    public final static String ATTRIBUTE_WEIGHT = "weight";
    public final static String ATTRIBUTE_ACTION_CLASS = "actionClass";
    public final static String ATTRIBUTE_ACTION_LIST_TRIGGER_ACTION_REF_ID = "triggerActionRefId";
    public final static String ATTRIBUTE_LINE_VISIBLE = "lineVisible";

    private static SAXParserFactory parserfactory;
    private ActionHandler handler;

    // A mapping between the action-set id and a list of action ids.
    private Map actionSetMap;

    //default action class of current set, if specified
    private String defaultActionClass;

    private boolean debug = false;
    private boolean printValidationErrors;

    private ActionManager actionManager;

    /**
     * Creates an action XML reader
     */
    public ActionXMLReader(ActionManager manager) {
        this.actionManager = manager;
    }

    /**
     * @param debug whether to not to output debug messages
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Sets whether XML validation errors should be printed.  Particularly
     * useful to pass false when merging multiple files using idref's
     * (though this can be avoided with XML's '&' entity includes)
     * @param printValidationErrors boolean
     */
    public void setPrintValidationErrors(boolean printValidationErrors) {
        this.printValidationErrors = printValidationErrors;
    }

    /**
     * Most commonly used for Swing apps.  Has the added benefit of setting
     * the parser's SystemId to the directory of the file, allowing references
     * to the DTD and other files to be resolved.
     * @param f the file to parser
     * @throws IOException
     * @throws SAXException
     */
    public void loadActions(File f) throws IOException, SAXException {
        if (debug) {
            System.out.println("loadActions(" + f.getAbsolutePath() + ")");
        }
        SAXParser parser = getParser();
        FileInputStream fis = new FileInputStream(f);
        InputSource is = new InputSource(fis);
        is.setSystemId(new File(f.getParent()).toURI().toURL().toExternalForm());
        loadActions(is, parser);
    }

    /**
     * Adds the set of actions and action-lists
     * from an action-set document into the ActionXMLReader.
     * A call to this method usually takes the form:
     * xmlReader.loadActions(getClass().getResource("myActions.xml"));
     *
     * @param url URL pointing to an actionSet document
     * @throws IllegalArgumentException If url param is null
     * @throws IOException If there is an error in parsing
     */
    public void loadActions(URL url) throws IOException, SAXException {
        if (debug) {
            System.out.println("loadActions(" + url + ")");
        }
        if (url == null) {
            throw new IllegalArgumentException("URL is null.");
        }
        InputStream stream = url.openStream();
        try {
            loadActions(new InputSource(stream), getParser());
        } finally {
            stream.close();
        }
    }

    /**
     * Adds the set of actions and action-lists
     * from an action-set document into the ActionXMLReader.
     *
     * @param stream InputStream containing an actionSet document
     * @throws IOException If there is an error in parsing
     */
    public void loadActions(InputStream stream) throws IOException, SAXException {
        SAXParser parser = getParser();
        loadActions(new InputSource(stream), parser);
    }

    public void loadActions(InputSource is, SAXParser parser) throws IOException {
        try {
            if (handler == null) {
                handler = new ActionHandler();
            }
            parser.parse(is, handler);
        } catch (SAXException ex) {
            printException("SAXException: " + ex.getMessage(), ex);
            throw new IOException("Error parsing: " + ex.getMessage());
        } catch (IOException ex2) {
            printException("IOException: " + ex2.getMessage(), ex2);
            throw ex2;
        }
    }

    public SAXParser getParser() throws SAXException, IOException {
        if (parserfactory == null) {
            parserfactory = SAXParserFactory.newInstance();
            parserfactory.setValidating(true);
        }
        try {
            return parserfactory.newSAXParser();
        } catch (ParserConfigurationException ex3) {
            printException("ParserConfigurationException: " + ex3.getMessage(), ex3);
            throw new SAXException("Error parsing: " + ex3.getMessage());
        }
    }

    /**
     * Adds the values represented in the SAX Attrributes structure
     * to a lightweight internal data strucure.
     *
     * @param attrs the Attributes for an action
     * @param actionset the parent action-set id for the action
     */
    private void addAttributes(Attributes attrs, String actionset) {
        String id = attrs.getValue(ATTRIBUTE_ID);

        actionManager.registerActionPrototype(id, xmlAttrsToActionAttrs(attrs));

        addActionIdToActionSet(attrs.getValue(ATTRIBUTE_ID), actionset);
    }

    /**
     * Create ActionAttributes from parsed XML Attributes, basically converting
     * the XML keys to Action keys.
     * @param attrs the XML Attributes of the action tag.
     * @return a new set of action attrbiutes
     */
    public ActionAttributes xmlAttrsToActionAttrs(Attributes attrs) {
        ActionAttributes actionAttrs = new ActionAttributes();
        updateAttributes(actionAttrs, attrs);
        return actionAttrs;
    }

    /**
     * Updates the actionAttrs by mapping the XML attributes to the action
     * attributes.  If an XML attribute value is null, then that value is
     * not updated (this is how inheritence works).  Roles are inherited
     * additively (new roles add to old roles, they do not replace them).
     * @param actionAttrs the attributes to update
     * @param attrs the XML to use to change the actionAttrs
     */
    public void updateAttributes(ActionAttributes actionAttrs, Attributes attrs) {
        Object value = attrs.getValue(ActionXMLReader.ATTRIBUTE_NAME);
        if (value != null) {
            actionAttrs.putValue(Action.NAME, value);
        }

        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_ID);
        if (value != null) {
            actionAttrs.putValue(ActionManager.ID, value);
            //Use the id as the command key unless it was explicitly set
            actionAttrs.putValue(Action.ACTION_COMMAND_KEY, value);
        }
        String commandKey = (String) attrs.getValue(ActionXMLReader.ATTRIBUTE_COMMAND);
        if (commandKey != null) {
            actionAttrs.putValue(Action.ACTION_COMMAND_KEY, commandKey);
        }

        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_DESC);
        if (value != null) {
            // BasicAction.getValue(LONG_DESCRIPTION) will fallback to SHORT_DESCRIPTION if needed
            actionAttrs.putValue(Action.SHORT_DESCRIPTION, value);
        }
        String longDesc = (String) attrs.getValue(ActionXMLReader.ATTRIBUTE_LONG_DESC);
        if (longDesc != null) {
            actionAttrs.putValue(Action.LONG_DESCRIPTION, longDesc);
        }

        String mnemonic = (String) attrs.getValue(ActionXMLReader.ATTRIBUTE_MNEMONIC);
        if (mnemonic != null && !mnemonic.equals("")) {
            actionAttrs.putValue(Action.MNEMONIC_KEY, Integer.valueOf(mnemonic.charAt(0)));
        }
        String accel = (String) attrs.getValue(ActionXMLReader.ATTRIBUTE_ACCEL);
        if (accel != null && !accel.equals("")) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(accel.trim());
            actionAttrs.putValue(Action.ACCELERATOR_KEY, keyStroke);
        }

        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_SMICON);
        if (value != null) {
            actionAttrs.putValue(Action.SMALL_ICON, actionManager.resolveIcon((String) value));
        }
        //note: ICON_ATTRIBUTE is not an standard action property
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_LGICON);
        if (value != null) {
            actionAttrs.putValue(ActionManager.LARGE_ICON, actionManager.resolveIcon((String) value));
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_TOOLBAR_SHOWS_TEXT);
        if (value != null) {
            actionAttrs.putValue(ActionManager.TOOLBAR_SHOWS_TEXT, Boolean.valueOf(value.toString()));
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_MENU_SHOWS_ICON);
        if (value != null) {
            actionAttrs.putValue(ActionManager.MENU_SHOWS_ICON, Boolean.valueOf(value.toString()));
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_GROUP);
        if (value != null) {
            actionAttrs.putValue(ActionManager.GROUP, value);
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_BUTTON_TYPE);
        if (value != null) {
            actionAttrs.putValue(ActionManager.BUTTON_TYPE, value);
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_WEIGHT);
        if (value != null) {
            actionAttrs.putValue(ActionManager.WEIGHT, value);
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_SELECTED);
        if (value != null) {
           actionAttrs.putValue(ActionManager.SELECTED, Boolean.valueOf(value.toString()));
        }
        value = attrs.getValue(ActionXMLReader.ATTRIBUTE_ACTION_CLASS);
        if (value == null) {
            if (defaultActionClass != null) {
                value = defaultActionClass;
            }
        }
        if (value != null) {
            actionAttrs.putValue(ActionManager.ACTION_CLASS, value);
        }
    }

    public void addRoles(String action, List roles) {
        ActionAttributes actionAttrs = actionManager.getPrototype(action);
        if (actionAttrs == null) {
            return;
        }
        actionAttrs.putValue(ActionManager.ACTION_ROLES, roles);
    }

    /**
     * Stores a new set of ActionAttributes internally by creating them from
     * the inherited values, then overriding those values with XML
     * attributes.  Adds the result to the given action set.
     *
     * @param actionId the id of the action to create
     * @param inheritedAttrs the values to start with
     * @param overridenAttrs the SAX Attributes to overwrite with
     * @param actionset the parent action-set id for the action
     */
    private void addAttributes(String actionId, ActionAttributes inheritedAttrs,
                               Attributes overridenAttrs, String actionset) {
        String id = overridenAttrs.getValue(ATTRIBUTE_ID);
        ActionAttributes attrs = new ActionAttributes(inheritedAttrs);
        attrs.putValue(ActionManager.ID, id);
        //by default the command matches the id, the xml can override command
        //if necessary, the derived action can redefine the command.
        attrs.putValue(Action.ACTION_COMMAND_KEY, id);

        // Apply redefined attributes
        updateAttributes(attrs, overridenAttrs);

        actionManager.registerActionPrototype(id, attrs);

        addActionIdToActionSet(id, actionset);
    }

    /**
     * Adds the id of the action set to an internal structure.
     * @param id the id of the action set
     * @param actionset the action set to remember.
     */
    private void addActionIdToActionSet(String id, String actionset) {
        // Add this action id to the actionset
        if (actionset != null && !actionset.equals("")) {
            List list = getActionSet(actionset);
            if (list == null) {
                list = new ArrayList();
            }
            list.add(id);
            addActionSet(actionset, list);
        }
    }

    /**
     * Retrieve the ids for all the managed actions-sets.
     * <p>
     * An action set is an association between an action-set id and the
     * action ids that it contains. For example, the actions-core.xml
     * action-set document has the action-set id: "core-actions" that
     * contains the actions: new-action, open-action, save-action, etc...
     *
     * @return a set which represents all the action-set ids
     */
    public Set getActionSetIDs() {
        if (actionSetMap == null) {
            actionSetMap = new HashMap();
        }
        return actionSetMap.keySet();
    }

    /**
     * Return an action set for an action-set id.
     * @param id the action-set id
     * @return a List of action ids in the set
     */
    private List getActionSet(Object id) {
        if (actionSetMap == null) {
            actionSetMap = new HashMap();
        }
        return (List) actionSetMap.get(id);
    }

    /**
     * Adds an action set for an action-set id.
     * @param key the action-set id
     * @param set the action set for the key
     */
    private void addActionSet(String key, List set) {
        if (actionSetMap == null) {
            actionSetMap = new HashMap();
        }
        actionSetMap.put(key, set);
    }

    private void printException(String message, Exception ex) {
        System.out.println(message);
        if (debug) {
            ex.printStackTrace();
        }
    }


    /**
     * A diagnostic which prints the Attributes of an action
     * on the printStream
     * @param stream the stream to print to
     * @param actionAttributes the attribtues to print
     */
    static void printActionAttributes(PrintStream stream, ActionAttributes actionAttributes) {
        stream.println("Attributes for " + actionAttributes.getValue(Action.ACTION_COMMAND_KEY)
                       + actionAttributes);
    }

    /**
     * Implemenation of the SAX event handler which acts on elements
     * and attributes defined in the action-set.dtd.
     *
     * This class creates the lightweight data structures which encapsulate
     * the parsed xml data that can be used to contruct Actions
     * and UI elements from Actions.
     */
    class ActionHandler extends DefaultHandler {

        private String element;

        private Stack actionListStack; // keep track of nested action-lists.
        private Stack actionSetStack; // keep track of nested action-sets.

        private String actionset; // current action-set id
        private ActionList actionlist; // current action-list id
        private String action; // current action id
        private String group; // current group id
        private List roles; //the list of role names defined for the action

        public void startDocument() {
            element = "";
            actionListStack = new Stack();
            actionSetStack = new Stack();

            actionset = null;
            actionlist = null;
            action = null;
            group = null;
            roles = null;
        }

        public void startElement(String nameSpace, String localName,
                                 String name, Attributes attributes) {
            if (debug) {
                System.out.print("startElement(" + nameSpace + ","
                                 + localName + "," + name + ",...)");
                String id = attributes.getValue(ATTRIBUTE_ID);
                System.out.println("id=" + id);
                if (id != null && id.equals("inherit-action")) {
                    System.out.println("in");
                }
            }
            element = name;

            if (ELEMENT_ACTION_SET.equals(name)) {
                String newSet = attributes.getValue(ATTRIBUTE_ID);
                if (actionset != null) {
                    actionSetStack.push(actionset);
                }
                actionset = newSet;

                String strWidth = attributes.getValue(ATTRIBUTE_TOOLBAR_BUTTON_PREF_WIDTH);
                String strHeight = attributes.getValue(ATTRIBUTE_TOOLBAR_BUTTON_PREF_HEIGHT);
                if (strWidth != null && strHeight != null) {
                    int width = Integer.parseInt(strWidth);
                    int height = Integer.parseInt(strHeight);
                    if (width > -1 && height > -1) {
                        ActionUIFactory.getInstance().setToolbarButtonPreferredSize(new Dimension(width, height));
                    }
                }
                defaultActionClass = attributes.getValue(ATTRIBUTE_DEFAULT_ACTION_CLASS);
            } else if (ELEMENT_ACTION_LIST.equals(name)) {
//                <action-list id="main-menu">
//                 <action-list id="file-menu" triggerActionRefId="file-menu-action">
//                   <action idref="new-action"/>
//                   <action idref="open-action" mnemonic="P"/>
//                   <action idref="save-action"/>
//                   <separator/>
//                   <action idref="exit-action"/>
//                 </action-list>
                //checkForId
                String listId = attributes.getValue(ATTRIBUTE_ID);
                String actionListRefId = attributes.getValue(ATTRIBUTE_IDREF);
                if (listId == null && actionListRefId == null) {
                    throw new IllegalArgumentException("Action lists require either an id or an idref.");
                }
                if (listId != null && actionListRefId != null) {
                    throw new UnsupportedOperationException("Error with action-list named:"+listId +" The idref attribute in action-list elements would refer to inherited lists, but they are not yet supported.  If the idref is meant to refering to the action that triggers the list (menu/toolbar), then use triggerActionRefId instead (this differs from the original JDNC XML).");
                }
                String triggerActionRefId = attributes.getValue(ATTRIBUTE_ACTION_LIST_TRIGGER_ACTION_REF_ID);
                if (actionListRefId != null && triggerActionRefId != null) {
                    throw new IllegalArgumentException("Action lists cannot use triggerActionRefId and idref, i.e. a list cannot tell another action-list what it's trigger is.");
                }
                if (triggerActionRefId == null && listId != null) {
                    //No, the action-list can define attributes for it's own
                    //action "in-line."
                    //Make a new action for the action list's triggering action
                    //whose id is the id of the action-list
                    triggerActionRefId = listId;
                    ActionAttributes actionAtts = actionManager.getPrototype(listId);
                    if (actionAtts == null) {
                        addAttributes(attributes, actionset);
                    }
                }

                //Does the action list refer to a pre-defined action list?
                ActionList newOrReferredToList = null;
                if (actionListRefId == null) {
                    //No, just create a new ActionList
                    newOrReferredToList = new ActionList(listId, triggerActionRefId);
                    String weight = attributes.getValue(ATTRIBUTE_WEIGHT);
                    if (weight != null) {
                        newOrReferredToList.setWeight(Double.valueOf(weight));
                    }
                    actionManager.registerActionIdList(newOrReferredToList);
                    if (actionlist != null) {
                        //Add current action list to it's parent
                        actionlist.add(newOrReferredToList);
                    }
                } else {
                    //Yes, get the reference from the action manager and
                    //use that as our list reference
                    newOrReferredToList = actionManager.getActionIdList(actionListRefId);
                    if (newOrReferredToList == null) {
                        throw new IllegalArgumentException("Action-list for "+
                            "idref "+actionListRefId+" does not exist.  "+
                            "Note: You may need to migrate your XML if "+
                            "you are changing ActionManager implementations.  "+
                            "Mark Davidson's original ActionManager uses the "+
                            "action-lists idref to point to actions.  Use "+
                            "triggerActionIdRef to point to actions.  Action-list "+
                            "idrefs are used to merge action-lists.");
                    }
                }

               //Push parent list down the stack, and make this list the top
               if (actionlist != null) {
                   actionListStack.push(actionlist);
               }
               actionlist = newOrReferredToList;
            } else if (ELEMENT_ACTION.equals(name)) {
                // If this action is not within an action-list element then
                // handle its attributes.
                action = attributes.getValue(ATTRIBUTE_ID);
                String inheritedAction = attributes.getValue(ATTRIBUTE_IDREF);
                validateAction(attributes, inheritedAction);
                //Add attributes for the new action
                if (action != null) {
                    addAttributesFromInheritedAction(attributes, inheritedAction);
                } else {
                    action = inheritedAction;
                }
                if (actionlist != null) {
                    // If this action is within an action-list element then add
                    // it to the list.
                    if (!actionlist.contains(action)) {
                        actionlist.add(action);
                    }
                    if (group != null) {
                        ActionAttributes actionAttrs = actionManager.getPrototype(action);
                        if (actionAttrs != null) {
                            actionAttrs.putValue(ActionManager.GROUP, group);
                        }
                    }
                }
            } else if (ELEMENT_NAME_VALUE_PAIR.equals(name)) {
                ActionAttributes actionAttrs = actionManager.getPrototype(action);
                if (actionAttrs != null) {
                    actionAttrs.putValue(attributes.getValue("name"), attributes.getValue("value"));
                }
            } else if (ELEMENT_ROLE.equals(name)) {
                if (roles == null) {
                    roles = new ArrayList(3);
                }
                roles.add(attributes.getValue("name"));
            } else if (ELEMENT_GROUP.equals(name)) {
                group = attributes.getValue(ATTRIBUTE_ID);
            } else if (ELEMENT_SEPARATOR.equals(name)) {
                if (actionlist != null) {
                    Number weight = null;
                    String weightStr = null;
                    try {
                        //try integers first
                        weightStr = attributes.getValue(ATTRIBUTE_WEIGHT);
                        if (weightStr != null) {
                            weight = Integer.valueOf(weightStr);
                        }
                    } catch (NumberFormatException ex) {
                        try {
                            weight = Double.valueOf(weightStr);
                        } catch (NumberFormatException ex2) {
                        }
                    }
                    boolean isLineVisible = true;
                    if (attributes.getValue(ATTRIBUTE_LINE_VISIBLE) != null) {
                        isLineVisible = Boolean.getBoolean(attributes.getValue(ATTRIBUTE_LINE_VISIBLE));
                    }
                    Separator separator = new Separator(attributes.getValue(ATTRIBUTE_ID), weight, isLineVisible);
                    actionlist.add(separator);
                }
            }
        }

        private void addAttributesFromInheritedAction(Attributes attributes, String inheritedAction) throws
            RuntimeException {
            if (inheritedAction == null) {
                addAttributes(attributes, actionset);
            } else {
                ActionAttributes inheritedActionAtts = actionManager.getPrototype(inheritedAction);
                if (inheritedActionAtts == null) {
                    throw new RuntimeException("Action " + action
                        + " inherits from action " + inheritedAction + " but it is not yet defined.  Actions that inherit from others must be declared after the action they inherit from.");
                }
                addAttributes(action, inheritedActionAtts, attributes, actionset);
            }
        }

        private void validateAction(Attributes attributes, String inheritedAction) throws RuntimeException {
            if (action == null && inheritedAction == null) {
                throw new RuntimeException("Actions must have an id or an idref defined. Element name:"+element+"Attributes:"+stringForAttributes(attributes));
            }
            //If there are any attributes on an action
            //besides idref, then id must be defined
            int numOtherAtts = attributes.getLength();
            if (action != null) {
                numOtherAtts--;
            }
            if (inheritedAction != null) {
                numOtherAtts--;
            }
            if (numOtherAtts > 0 && action == null) {
                throw new RuntimeException(
                    "An action that defines an attribute must declare an id.  Inherited action:"
                    + inheritedAction);
            }
        }

        private String stringForAttributes(Attributes attributes) {
            if (attributes == null) {
                return "null";
            }
            String result = "";
            for (int i = 0; i < attributes.getLength(); i++) {
                result = result + attributes.getLocalName(i) + "="+ attributes.getValue(i)+" ";
            }
            return result;
        }

        public void endElement(String nameSpace, String localName, String name) {
            if (debug) {
                System.out.println("endElement(" + nameSpace + ","
                                   + localName + "," + name + ")");
            }

            if (ELEMENT_ACTION_SET.equals(name)) {
                try {
                    actionset = (String) actionSetStack.pop();
                } catch (EmptyStackException ex) {
                    actionset = null;
                }
            } else if (ELEMENT_ACTION_LIST.equals(name)) {
                try {
                    if (roles != null) {
                        actionlist.setRoles(roles);
                    }
                    roles = null;
                    actionlist = (ActionList) actionListStack.pop();
                } catch (EmptyStackException ex) {
                    actionlist = null;
                }
            } else if (ELEMENT_ACTION.equals(name)) {
                if (roles != null) {
                    addRoles(action, roles);
                }
                roles = null;
            } else if (ELEMENT_GROUP.equals(name)) {
                group = null;
            }
        }

        public void endDocument() {
            element = "";
            actionListStack = new Stack();
            actionSetStack = new Stack();

            actionset = null;
            actionlist = null;
            action = null;
            group = null;
        }

        //
        // Overloaded ErrorHandler methods for Validating parser.
        //

        public void error(SAXParseException ex) throws SAXException {
            if (printValidationErrors) {
                System.out.println("**** validation error");
                reportException(ex);
            }
        }

        public void warning(SAXParseException ex) throws SAXException {
            if (printValidationErrors) {
                System.out.println("**** validation warning");
                reportException(ex);
            }
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            if (printValidationErrors) {
                System.out.println("**** validation fatalError");
                reportException(ex);
            }
        }

        private void reportException(SAXParseException ex) {
            if (printValidationErrors) {
                System.out.println(ex.getLineNumber() + ":" + ex.getColumnNumber() + " "
                                   + ex.getMessage());
                System.out.println("Public ID: " + ex.getPublicId() + "\t" +
                                   "System ID: " + ex.getSystemId());
            }
            if (debug) {
                ex.printStackTrace();
            }
        }

    } // end class ActionXMLReader

   /**
    * Parses any file into a Document
    * @param file the file to parse
    * @return a Document
    * @throws ParserConfigurationException
    * @throws IOException
    * @throws SAXException
    */
   public static Document parseXMLDocument(File file) throws ParserConfigurationException, IOException, SAXException {
       // Create a builder factory
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       factory.setValidating(true);

       // Create the builder and parse the file
       return factory.newDocumentBuilder().parse(file);
   }
}
