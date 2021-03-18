/*
 * Created on Jan 21, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Shape;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.Position.Bias;

/**
 * This class is essentially a wrapper for another view. The paint method
 * is left abstract so that custom drawing can be done. 
 * 
 * This is useful for extending the functionallity of non-public Swing views
 * such as javax.swing.text.html.TableView
 * 
 * @author Bob Tantlinger
 */
public abstract class DelegateView extends View
{
    protected View delegate;    
    
    public DelegateView(View delegate)
    {
        super(delegate.getElement());
        this.delegate = delegate;        
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#append(javax.swing.text.View)
     */
    public void append(View v)
    {
        delegate.append(v);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#breakView(int, int, float, float)
     */
    public View breakView(int axis, int offset, float pos, float len)
    {
        return delegate.breakView(axis, offset, pos, len);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#changedUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
     */
    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f)
    {
        delegate.changedUpdate(e, a, f);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#createFragment(int, int)
     */
    public View createFragment(int p0, int p1)
    {
        return delegate.createFragment(p0, p1);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        return delegate.equals(obj);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getAlignment(int)
     */
    public float getAlignment(int axis)
    {
        return delegate.getAlignment(axis);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getAttributes()
     */
    public AttributeSet getAttributes()
    {
        return delegate.getAttributes();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getBreakWeight(int, float, float)
     */
    public int getBreakWeight(int axis, float pos, float len)
    {
        return delegate.getBreakWeight(axis, pos, len);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getChildAllocation(int, java.awt.Shape)
     */
    public Shape getChildAllocation(int index, Shape a)
    {
        return delegate.getChildAllocation(index, a);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getContainer()
     */
    public Container getContainer()
    {
        return delegate.getContainer();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getDocument()
     */
    public Document getDocument()
    {
        return delegate.getDocument();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getElement()
     */
    public Element getElement()
    {
        return delegate.getElement();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getEndOffset()
     */
    public int getEndOffset()
    {
        return delegate.getEndOffset();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getGraphics()
     */
    public Graphics getGraphics()
    {
        return delegate.getGraphics();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getMaximumSpan(int)
     */
    public float getMaximumSpan(int axis)
    {
        return delegate.getMaximumSpan(axis);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getMinimumSpan(int)
     */
    public float getMinimumSpan(int axis)
    {
        return delegate.getMinimumSpan(axis);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getNextVisualPositionFrom(int, javax.swing.text.Position.Bias, java.awt.Shape, int, javax.swing.text.Position.Bias[])
     */
    public int getNextVisualPositionFrom(int pos, Bias b, Shape a, int direction, Bias[] biasRet) throws BadLocationException
    {
        return delegate.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getParent()
     */
    public View getParent()
    {
        return delegate.getParent();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getPreferredSpan(int)
     */
    public float getPreferredSpan(int axis)
    {
        return delegate.getPreferredSpan(axis);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getResizeWeight(int)
     */
    public int getResizeWeight(int axis)
    {
        return delegate.getResizeWeight(axis);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getStartOffset()
     */
    public int getStartOffset()
    {
        return delegate.getStartOffset();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getToolTipText(float, float, java.awt.Shape)
     */
    public String getToolTipText(float x, float y, Shape allocation)
    {
        return delegate.getToolTipText(x, y, allocation);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getView(int)
     */
    public View getView(int n)
    {
        return delegate.getView(n);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getViewCount()
     */
    public int getViewCount()
    {
        return delegate.getViewCount();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getViewFactory()
     */
    public ViewFactory getViewFactory()
    {
        return delegate.getViewFactory();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getViewIndex(float, float, java.awt.Shape)
     */
    public int getViewIndex(float x, float y, Shape allocation)
    {
        return delegate.getViewIndex(x, y, allocation);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#getViewIndex(int, javax.swing.text.Position.Bias)
     */
    public int getViewIndex(int pos, Bias b)
    {
        return delegate.getViewIndex(pos, b);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return delegate.hashCode();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#insert(int, javax.swing.text.View)
     */
    public void insert(int offs, View v)
    {
        delegate.insert(offs, v);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#insertUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
     */
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f)
    {
        delegate.insertUpdate(e, a, f);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#isVisible()
     */
    public boolean isVisible()
    {
        return delegate.isVisible();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#modelToView(int, javax.swing.text.Position.Bias, int, javax.swing.text.Position.Bias, java.awt.Shape)
     */
    public Shape modelToView(int p0, Bias b0, int p1, Bias b1, Shape a) throws BadLocationException
    {
        return delegate.modelToView(p0, b0, p1, b1, a);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#modelToView(int, java.awt.Shape, javax.swing.text.Position.Bias)
     */
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException
    {
        return delegate.modelToView(pos, a, b);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#modelToView(int, java.awt.Shape)
     */
    public Shape modelToView(int pos, Shape a) throws BadLocationException
    {
        return delegate.modelToView(pos, a);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#preferenceChanged(javax.swing.text.View, boolean, boolean)
     */
    public void preferenceChanged(View child, boolean width, boolean height)
    {
        delegate.preferenceChanged(child, width, height);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#remove(int)
     */
    public void remove(int i)
    {
        delegate.remove(i);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#removeAll()
     */
    public void removeAll()
    {
        delegate.removeAll();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#removeUpdate(javax.swing.event.DocumentEvent, java.awt.Shape, javax.swing.text.ViewFactory)
     */
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f)
    {
        delegate.removeUpdate(e, a, f);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#replace(int, int, javax.swing.text.View[])
     */
    public void replace(int offset, int length, View[] views)
    {
        delegate.replace(offset, length, views);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#setParent(javax.swing.text.View)
     */
    public void setParent(View parent)
    {
        delegate.setParent(parent);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#setSize(float, float)
     */
    public void setSize(float width, float height)
    {
        delegate.setSize(width, height);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return delegate.toString();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#viewToModel(float, float, java.awt.Shape, javax.swing.text.Position.Bias[])
     */
    public int viewToModel(float x, float y, Shape a, Bias[] biasReturn)
    {
        return delegate.viewToModel(x, y, a, biasReturn);
    }

    /* (non-Javadoc)
     * @see javax.swing.text.View#viewToModel(float, float, java.awt.Shape)
     */
    public int viewToModel(float x, float y, Shape a)
    {
        return delegate.viewToModel(x, y, a);
    }
}
