package net.atlanticbb.tantlinger.ui.text;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SizeRequirements;
import javax.swing.event.DocumentEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ObjectView;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.actions.DecoratedTextAction;
import net.atlanticbb.tantlinger.ui.text.actions.EnterKeyAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction;
import net.atlanticbb.tantlinger.ui.text.actions.RemoveAction;
import net.atlanticbb.tantlinger.ui.text.actions.TabAction;

/**
 * An HTML Wysiwyg editor kit which can properly draw borderless tables
 * and allows for resizing of tables and images.
 *
 * @author Bob Tantlinger
 *
 */
public class WysiwygHTMLEditorKit extends HTMLEditorKit
{
	private static final long serialVersionUID = 2287124068677845845L;

    private String SEPARATOR = System.getProperty("line.separator");

	private ViewFactory wysFactory = new WysiwygHTMLFactory();
    private ArrayList monitoredViews = new ArrayList();
    private MouseInputAdapter resizeHandler = new ResizeHandler();

    private Map editorToActionsMap = new HashMap();
    private KeyStroke tabBackwardKS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);

    public WysiwygHTMLEditorKit()
    {
        super();
    }

    public Document createDefaultDocument()
    {
        HTMLDocument doc = (HTMLDocument)super.createDefaultDocument();

        //Unless the following property is set, the HTML parser will throw a
        //ChangedCharSetException every time a char set tag is encountered.
        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);

        return doc;
    }

    public void install(JEditorPane ed)
    {
        super.install(ed);
        if(editorToActionsMap.containsKey(ed))
            return; //already installed

        ed.addMouseListener(resizeHandler);
        ed.addMouseMotionListener(resizeHandler);

        //install wysiwyg actions into the ActionMap for the editor being installed
        Map actions = new HashMap();
        InputMap inputMap = ed.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = ed.getActionMap();

        Action delegate = actionMap.get("insert-break");
        Action action = new EnterKeyAction(delegate);
        actions.put("insert-break", action);
        actionMap.put("insert-break", action);

        delegate = actionMap.get("delete-previous");
        action = new RemoveAction(RemoveAction.BACKSPACE, delegate);
        actions.put("delete-previous", action);
        actionMap.put("delete-previous", action);

        delegate = actionMap.get("delete-next");
        action = new RemoveAction(RemoveAction.DELETE, delegate);
        actions.put("delete-next", action);
        actionMap.put("delete-next", action);

        delegate = actionMap.get("insert-tab");
        action = new TabAction(TabAction.FORWARD, delegate);
        actions.put("insert-tab", action);
        actionMap.put("insert-tab", action);

        delegate = actionMap.get("paste-from-clipboard");
        HTMLTextEditAction hteAction = new net.atlanticbb.tantlinger.ui.text.actions.PasteAction();
        hteAction.putContextValue(HTMLTextEditAction.EDITOR, ed);
        actions.put("paste-from-clipboard", delegate);
        actionMap.put("paste-from-clipboard", hteAction);

        inputMap.put(tabBackwardKS, "tab-backward");//install tab backwards keystroke
        action = new TabAction(TabAction.BACKWARD, delegate);
        actions.put("tab-backward", action);
        actionMap.put("tab-backward", action);

        editorToActionsMap.put(ed, actions);
    }

    public void deinstall(JEditorPane ed)
    {
        super.deinstall(ed);
        if(!editorToActionsMap.containsKey(ed))
            return; //not installed installed
        ed.removeMouseListener(resizeHandler);
        ed.removeMouseMotionListener(resizeHandler);

        //restore actions to their original state
        ActionMap actionMap = ed.getActionMap();
        Map actions = (Map)editorToActionsMap.get(ed);

        Action curAct = actionMap.get("insert-break");
        if(curAct == actions.get("insert-break"))
        {
            actionMap.put("insert-break", ((DecoratedTextAction)curAct).getDelegate());
        }

        curAct = actionMap.get("delete-previous");
        if(curAct == actions.get("delete-previous"))
        {
            actionMap.put("delete-previous", ((DecoratedTextAction)curAct).getDelegate());
        }

        curAct = actionMap.get("delete-next");
        if(curAct == actions.get("delete-next"))
        {
            actionMap.put("delete-next", ((DecoratedTextAction)curAct).getDelegate());
        }

        curAct = actionMap.get("insert-tab");
        if(curAct == actions.get("insert-tab"))
        {
            actionMap.put("insert-tab", ((DecoratedTextAction)curAct).getDelegate());
        }

        curAct = actionMap.get("paste-from-clipboard");
        if(curAct instanceof net.atlanticbb.tantlinger.ui.text.actions.PasteAction)
        {
            actionMap.put("paste-from-clipboard", (Action)actions.get("paste-from-clipboard"));
        }

        curAct = actionMap.get("tab-backward");
        if(curAct == actions.get("insert-tab"))
        {
            actionMap.remove("tab-backward");
            //inputMap.remove(tabBackwardKS);//remove backwards keystroke
        }

        editorToActionsMap.remove(ed);
    }

    /**
     * Fetch a factory that is suitable for producing views
     * of any models that are produced by this kit.
     * @return the factory
     */
    public ViewFactory getViewFactory()
    {
        return wysFactory;
    }

    /**
     * Factory to build views of the html elements. This simply extends the behavior
     * of the default html factory to draw borderless tables, etc
     */
    public class WysiwygHTMLFactory extends HTMLFactory
    {
        public View create(Element elem)
        {
            Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if(o instanceof HTML.Tag)
            {
                HTML.Tag kind = (HTML.Tag)o;
                if(kind == HTML.Tag.TABLE)
                {
                    ResizableView v = new ResizableView(
                        new BorderlessTableView(super.create(elem)));
                    monitoredViews.add(v);
                    return v;
                }
                else if(kind == HTML.Tag.IMG)
                {
                    ResizableView v = new ResizableView(super.create(elem));
                    monitoredViews.add(v);
                    return v;
                }
                else if(kind == HTML.Tag.COMMENT)
                {
                    return new UnknownElementView((elem));
                }
                else if(kind == HTML.Tag.OBJECT)
                {
                    ObjectView ov = new ObjectView(elem)
                    {
                        //make a nicer looking representation for <object>.
                        //The default is a crappy red JLabel with "??" as the text
                        protected Component createComponent()
                        {
                            Component comp = super.createComponent();
                            if(comp instanceof JLabel)
                            {
                                JLabel l = (JLabel)comp;
                                if(l.getText().equals("??") &&
                                    l.getForeground().equals(Color.red))
                                {
                                    l.setIcon(UIUtils.getIcon("cogs.png"));
                                    l.setText(null);
                                    l.setBackground(Color.YELLOW);
                                    l.setOpaque(true);
                                    l.setBorder(BorderFactory.createRaisedBevelBorder());
                                    l.setToolTipText("<object></object>");
                                }
                            }
                            return comp;
                        }
                    };
                    return ov;
                }
                else if((kind instanceof HTML.UnknownTag) ||
                   (kind == HTML.Tag.TITLE) ||
                   (kind == HTML.Tag.META) ||
                   (kind == HTML.Tag.LINK) ||
                   (kind == HTML.Tag.STYLE) ||
                   (kind == HTML.Tag.SCRIPT) ||
                   (kind == HTML.Tag.AREA) ||
                   (kind == HTML.Tag.MAP) ||
                   (kind == HTML.Tag.PARAM) ||
                   (kind == HTML.Tag.APPLET))
                {
                    return new UnknownElementView(elem);
                }
            }

            View v = super.create(elem);

            if (v instanceof InlineView){
                return new InlineView(elem){
                    public int getBreakWeight(int axis, float pos, float len) {
                        //return GoodBreakWeight;
                        if (axis == View.X_AXIS) {
                            checkPainter();
                            int p0 = getStartOffset();
                            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                            if (p1 == p0) {
                                // can't even fit a single character
                                return View.BadBreakWeight;
                            }
                            try {
                                //if the view contains line break char return forced break
                                if (getDocument().getText(p0, p1 - p0).indexOf(SEPARATOR) >= 0) {
                                    return View.ForcedBreakWeight;
                                }
                            }
                            catch (BadLocationException ex) {
                                //should never happen
                            }

                        }
                        return super.getBreakWeight(axis, pos, len);
                    }
                    public View breakView(int axis, int p0, float pos, float len) {
                        if (axis == View.X_AXIS) {
                            checkPainter();
                            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                            try {
                                //if the view contains line break char break the view
                                int index = getDocument().getText(p0, p1 - p0).indexOf(SEPARATOR);
                                if (index >= 0) {
                                    GlyphView v = (GlyphView) createFragment(p0, p0 + index + 1);
                                    return v;
                                }
                            }
                            catch (BadLocationException ex) {
                                //should never happen
                            }

                        }
                        return super.breakView(axis, p0, pos, len);
                    }
                };
            }
            else if (v instanceof ParagraphView) {
                return new ParagraphView(elem) {
                    protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
                        if (r == null) {
                            r = new SizeRequirements();
                        }
                        float pref = layoutPool.getPreferredSpan(axis);
                        float min = layoutPool.getMinimumSpan(axis);
                        // Don't include insets, Box.getXXXSpan will include them.
                        r.minimum = (int)min;
                        r.preferred = Math.max(r.minimum, (int) pref);
                        r.maximum = Integer.MAX_VALUE;
                        r.alignment = 0.5f;
                        return r;
                    }

                };
            }

            return v;
        }
    }

    /**
     * Handle the resizing of images and tables
     *
     */
    private class ResizeHandler extends MouseInputAdapter
    {
        boolean dragStarted;
        //need down flag for Java 1.4 - mouseMoved gets called during a drag
        boolean mouseDown;
        int dragDir = -1;

        public void mousePressed(MouseEvent e)
        {
            mouseDown = true;
            boolean selected = false;
            //iterate thru the list backwards to select
            //most recently added views so nested tables get selected properly
            for(int i = monitoredViews.size() - 1; i >= 0; i--)
            {
                ResizableView v = (ResizableView)monitoredViews.get(i);
                Rectangle r = v.getBounds();
                if(r != null && r.contains(e.getPoint()) && !selected)
                {
                    v.setSelectionEnabled(true);
                    dragDir = v.getHandleForPoint(e.getPoint());
                    setCursorForDir(dragDir, e.getComponent());
                    selected = true;
                }
                else
                    v.setSelectionEnabled(false);
            }

            e.getComponent().repaint();
        }

        public void mouseMoved(MouseEvent e)
        {
            if(!mouseDown)
            {
                ResizableView v = getSelectedView();
                if(v == null)
                    return;

                Component c = e.getComponent();
                setCursorForDir(v.getHandleForPoint(e.getPoint()), c);
            }
        }

        public void mouseDragged(MouseEvent e)
        {
            dragStarted = dragDir != -1;
            ResizableView v = getSelectedView();
            if(v == null || !dragStarted)
                return;

            Rectangle r = v.getSelectionBounds();

            if(dragDir == ResizableView.SE)
            {
                r.width = e.getX() - r.x;
                r.height = e.getY() - r.y;
            }
            else if(dragDir == ResizableView.NE)
            {
                r.width = e.getX() - r.x;
                r.height = (r.y + r.height) - e.getY();
                r.y = e.getY();
            }
            else if(dragDir == ResizableView.SW)
            {
                r.width = (r.x + r.width) - e.getX();
                r.height = e.getY() - r.y;
                r.x = e.getX();
            }
            else if(dragDir == ResizableView.NW)
            {
                r.width = (r.x + r.width) - e.getX();
                r.height = (r.y + r.height) - e.getY();
                r.x = e.getX();
                r.y = e.getY();
            }
            else if(dragDir == ResizableView.N)
            {
                r.height = (r.y + r.height) - e.getY();
                r.y = e.getY();
            }
            else if(dragDir == ResizableView.S)
            {
                r.height = e.getY() - r.y;
            }
            else if(dragDir == ResizableView.E)
            {
                r.width = e.getX() - r.x;
            }
            else if(dragDir == ResizableView.W)
            {
                r.width = (r.x + r.width) - e.getX();
                r.x = e.getX();
            }


            e.getComponent().repaint();
        }

        public void mouseReleased(MouseEvent e)
        {
            mouseDown = false;
            ResizableView v = getSelectedView();
            if(v != null && dragStarted)
            {
                Element elem = v.getElement();
                SimpleAttributeSet sas = new SimpleAttributeSet(elem.getAttributes());
                Integer w = Integer.valueOf(v.getSelectionBounds().width);
                Integer h = Integer.valueOf(v.getSelectionBounds().height);

                if(elem.getName().equals("table"))//resize the table
                {
                    //currently jeditorpane only supports the width attrib for tables
                    sas.addAttribute(HTML.Attribute.WIDTH, w);
                    String html = HTMLUtils.getElementHTML(elem, false);
                    html = HTMLUtils.createTag(HTML.Tag.TABLE, sas, html);
                    replace(elem, html);
                }
                else if(elem.getName().equals("img"))//resize the img
                {
                    sas.addAttribute(HTML.Attribute.WIDTH, w);
                    sas.addAttribute(HTML.Attribute.HEIGHT, h);
                    String html = "<img";
                    for(Enumeration ee = sas.getAttributeNames(); ee.hasMoreElements();)
                    {
                        Object name = ee.nextElement();
                        if(!(name.toString().equals("name") || name.toString().equals("a")))
                        {
                            Object val = sas.getAttribute(name);
                            html += " " + name + "=\"" + val + "\"";
                        }
                    }
                    html += ">";

                    if(sas.isDefined(HTML.Tag.A))
                        html = "<a " + sas.getAttribute(HTML.Tag.A) + ">" + html + "</a>";
                    replace(elem, html);
                }

                //remove views not appearing in the doc
                updateMonitoredViews((HTMLDocument)v.getDocument());
            }

            dragStarted = false;
        }

        private void setCursorForDir(int d, Component c)
        {
            if(d == ResizableView.NW)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            else if(d == ResizableView.SW)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else if(d == ResizableView.NE)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
            else if(d == ResizableView.SE)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            else if(d == ResizableView.N)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            else if(d == ResizableView.S)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            else if(d == ResizableView.E)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            else if(d == ResizableView.W)
                c.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            else if(c.getCursor().getType() != Cursor.DEFAULT_CURSOR)
                c.setCursor(Cursor.getDefaultCursor());
        }

        /**
         * Updates the list of monitored ResizeableViews. If they don't
         * exist in the document, they're removed from the list.
         * @param doc
         */
        private void updateMonitoredViews(HTMLDocument doc)
        {
            for(Iterator it = monitoredViews.iterator(); it.hasNext();)
            {
                View v = (View)it.next();
                Element vElem = v.getElement();
                if(vElem.getName().equals("img"))
                {
                    Element el = doc.getCharacterElement(vElem.getStartOffset());
                    if(el != vElem)
                        it.remove();
                }
                else if(vElem.getName().equals("table"))
                {
                    Element el = doc.getParagraphElement(vElem.getStartOffset());
                    //get the parent and check if its the same element
                    el = HTMLUtils.getParent(el, HTML.Tag.TABLE);
                    //FIXME if the element is a nested table in the first cell
                    //of the parent table, the parent view is removed
                    if(el != vElem)
                        it.remove();
                }
            }
        }

        /**
         * Get the currently selected view.
         * Only one view at a time can be selected
         * @return
         */
        private ResizableView getSelectedView()
        {
            for(Iterator it = monitoredViews.iterator(); it.hasNext();)
            {
                ResizableView v = (ResizableView)it.next();
                if(v.isSelectionEnabled())
                    return v;
            }

            return null;
        }

        /**
         * Replaced the element with the specified html.
         * @param elem
         * @param html
         */
        private void replace(Element elem, String html)
        {
            HTMLDocument document = (HTMLDocument)elem.getDocument();
            CompoundUndoManager.beginCompoundEdit(document);
            try
            {
                document.setOuterHTML(elem, html);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            CompoundUndoManager.endCompoundEdit(document);
        }
    }

    /**
     * View which can draw resize handles around its delegate
     *
     * @author Bob Tantlinger
     */
    private class ResizableView extends DelegateView
    {
        public static final int NW = 0;
        public static final int NE = 1;
        public static final int SW = 2;
        public static final int SE = 3;
        public static final int N = 4;
        public static final int S = 5;
        public static final int E = 6;
        public static final int W = 7;

        private Rectangle curBounds;
        private Rectangle selBounds;

        public ResizableView(View delegate)
        {
            super(delegate);
        }

        public void paint(Graphics g, Shape allocation)
        {
            curBounds = new Rectangle(allocation.getBounds());
            delegate.paint(g, allocation);
            drawSelectionHandles(g);
        }

        /* (non-Javadoc)
         * @see javax.swing.text.View#insertUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
         */
        public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f)
        {
            setSelectionEnabled(false);
            super.insertUpdate(e, a, f);
        }

        /* (non-Javadoc)
         * @see javax.swing.text.View#changedUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
         */
        public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f)
        {
            setSelectionEnabled(false);
            super.changedUpdate(e, a, f);
        }

        /* (non-Javadoc)
         * @see javax.swing.text.View#removeUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
         */
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f)
        {
            setSelectionEnabled(false);
            super.removeUpdate(e, a, f);
        }

        /**
         * Gets the current bounds of the view
         * @return
         */
        public Rectangle getBounds()
        {
            return curBounds;
        }

        /**
         * Gets the Rectangle from which the selection handles are drawn
         * @return
         */
        public Rectangle getSelectionBounds()
        {
            return selBounds;
        }

        /**
         * Draw the selection if true
         * @param b
         */
        public void setSelectionEnabled(boolean b)
        {
            if(b && curBounds != null)
                selBounds = new Rectangle(curBounds);
            else
                selBounds = null;
        }

        public boolean isSelectionEnabled()
        {
            return selBounds != null;
        }

        /**
         * Gets the selection handle at the specified point.
         * @param p
         * @return one of NW, SW, NE, SE, N, S, E, W
         * or -1 if a handle is not at the point
         */
        public int getHandleForPoint(Point p)
        {
            if(isSelectionEnabled())
            {
                Rectangle r[] = computeHandles(selBounds);
                for(int i = 0; i < r.length; i++)
                    if(r[i].contains(p))
                        return i;
            }

            return -1;
        }

        private void drawSelectionHandles(Graphics g)
        {
            if(!isSelectionEnabled())
                return;

            Color cached = g.getColor();
            g.setColor(Color.DARK_GRAY);
            g.drawRect(selBounds.x, selBounds.y, selBounds.width, selBounds.height);

            Rectangle h[] = computeHandles(selBounds);
            for(int i = 0; i < h.length; i++)
                g.fillRect(h[i].x, h[i].y, h[i].width, h[i].height);
            g.setColor(cached);
        }

        private Rectangle[] computeHandles(Rectangle sel)
        {
            Rectangle r[] = new Rectangle[8];
            int sq = 8;
            r[NW] = new Rectangle(sel.x, sel.y, sq, sq);
            r[NE] = new Rectangle(sel.x + sel.width - sq, sel.y, sq, sq);
            r[SW] = new Rectangle(sel.x, sel.y + sel.height - sq, sq, sq);
            r[SE] = new Rectangle(sel.x + sel.width - sq, sel.y + sel.height - sq, sq, sq);

            int midX = sel.x + (sel.width/2 - sq/2);
            int midY = sel.y + (sel.height/2 - sq/2);
            r[N] = new Rectangle(midX, sel.y, sq, sq);
            r[S] = new Rectangle(midX, sel.y + sel.height - sq, sq, sq);
            r[E] = new Rectangle(sel.x + sel.width - sq, midY, sq, sq);
            r[W] = new Rectangle(sel.x, midY, sq, sq);

            return r;
        }
    }

    /**
     * Delegate view which draws borderless tables.
     *
     * This class is a delegate view because javax.swing.text.html.TableView
     * is not public...
     *
     * @author Bob Tantlinger
     *
     */
    private class BorderlessTableView extends DelegateView
    {
        public BorderlessTableView(View delegate)
        {
            super(delegate);
        }

        public void paint(Graphics g, Shape allocation)
        {
            //if the table element has no border,
            //then draw the table outline with a dotted line
            if(shouldDrawDottedBorder())
            {
                //draw the table background color if set
                //we need to do this for Java 1.5, otherwise
                //the bgcolor doesnt get painted for some reason
                Color bgColor = getTableBgcolor();
                if(bgColor != null)
                {
                    Color cachedColor = g.getColor();
                    g.setColor(bgColor);
                    Rectangle tr = allocation.getBounds();
                    g.fillRect(tr.x, tr.y, tr.width, tr.height);
                    g.setColor(cachedColor);
                }

                delegate.paint(g, allocation);

                //set up the graphics object to draw dotted lines
                Graphics2D g2 = (Graphics2D)g;
                Stroke cachedStroke = g2.getStroke();
                float dash[] = {3.0f};
                BasicStroke stroke = new BasicStroke(
                    1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, dash, 0.0f);
                g2.setStroke(stroke);
                g2.setColor(Color.DARK_GRAY);

                int rows = getViewCount();
                for(int r = 0; r < rows; r++)
                {
                    Shape rowShape = getChildAllocation(r, allocation);
                    View rowView = getView(r);
                    int cells = rowView.getViewCount();
                    //draw each cell with a dotted border
                    for(int c = 0; c < cells; c++)
                    {
                        Shape cellShape = rowView.getChildAllocation(c, rowShape);
                        Rectangle cr = cellShape.getBounds();
                        g2.drawRect(cr.x, cr.y, cr.width, cr.height);
                    }
                }

                g2.setStroke(cachedStroke);
            }
            else
                delegate.paint(g, allocation);
        }

        private Color getTableBgcolor()
        {
            AttributeSet atr = getElement().getAttributes();
            Object o = atr.getAttribute(HTML.Attribute.BGCOLOR);
            if(o != null)
            {
                Color c = HTMLUtils.stringToColor(o.toString());
                return c;
            }

            return null;
        }

        private boolean shouldDrawDottedBorder()
        {
            AttributeSet atr = getElement().getAttributes();
            boolean isBorderAttr = hasBorderAttr(atr);
            return (!isBorderAttr) || isBorderAttr &&
                atr.getAttribute(HTML.Attribute.BORDER).toString().equals("0");
        }

        private boolean hasBorderAttr(AttributeSet atr)
        {
            for(Enumeration e = atr.getAttributeNames(); e.hasMoreElements();)
            {
                if(e.nextElement().toString().equals("border"))
                    return true;
            }

            return false;
        }
    }

    /**
     * A view for {@link Element}s that are uneditable.
     * <p>
     * The default view for such {@link Element}s are big ugly blocky JTextFields,
     * which doesn't really work well in practice and tends to confuse users.
     * This view replaces the default and simply draws the elements as blocks that
     * indicates the presence of an uneditable {@link Element}.
     * <p>
     *
     * @author Bob Tantlinger
     *
     */
    private class UnknownElementView extends ComponentView
    {
        public UnknownElementView(Element e)
        {
            super(e);
        }

        protected Component createComponent()
        {
            JLabel p = new JLabel();
            if(getElement().getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.COMMENT)
            {
                p.setText("<!-- -->");
                AttributeSet as = getElement().getAttributes();
                if(as != null)
                {
                    Object comment = as.getAttribute(HTML.Attribute.COMMENT);
                    if (comment instanceof String)
                    {
                        p.setToolTipText((String)comment);
                    }
                }
            }
            else
            {
                String text = this.getElement().getName();
                if(text == null || text.equals(""))
                    text = "??";
                if(isEndTag())
                    text = "</" + text + ">";
                else
                    text = "<" + text + ">";
                p.setText(text);
            }

            p.setBorder(BorderFactory.createRaisedBevelBorder());
            p.setBackground(Color.YELLOW);
            p.setForeground(Color.BLUE);
            p.setOpaque(true);

            return p;
        }

        boolean isEndTag()
        {
            AttributeSet as = getElement().getAttributes();
            if(as != null)
            {
                Object end = as.getAttribute(HTML.Attribute.ENDTAG);
                if(end != null && (end instanceof String) && ((String)end).equals("true"))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
