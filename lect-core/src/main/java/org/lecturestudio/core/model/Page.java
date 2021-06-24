/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.model;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.action.DeleteShapeAction;
import org.lecturestudio.core.model.action.ShapeAction;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ShapeChangeListener;
import org.lecturestudio.core.model.listener.ShapeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is the representation of a Page in a Document. It has a background
 * shape, that can either be empty (white board page) or a SlideShape holding a
 * PDF-Slide. The foreground of the page consists of an arbitrary number of
 * shapes. Adding or removing shapes should be done by actions only, so they can
 * be un- and redone.
 * 
 * The positions and sizes of shapes are in page metrics, e.g. a normal
 * background shape has width 4 and height 3.
 * 
 * @author Alex Andres
 * @author Tobias
 */
public class Page {

	private class PropagateShapeChange implements ShapeChangeListener {

		@Override
		public void shapeChanged(Shape sender, Rectangle2D dirtyArea) {
			firePageEdited(sender, dirtyArea, PageEditEvent.Type.SHAPE_CHANGE);
		}

	}

	private final Document document;

	/** Cached page text. */
	private String pageText;
	
	/** Cached page text layer. */
	private List<Rectangle2D> textLayer;
	
	/** Cached page URIs. */
	private List<URI> pageUris;
	
	/** Cached page file launch actions. */
	private List<File> pageLaunchFiles;
	
	private final List<Shape> shapes = new CopyOnWriteArrayList<>();

	private final PropagateShapeChange psc = new PropagateShapeChange();

	private final Set<PageEditedListener> listeners = ConcurrentHashMap.newKeySet();

	private final Set<ShapeListener> shapeListeners = ConcurrentHashMap.newKeySet();

	private final Stack<ShapeAction> undoActions = new Stack<>();
	private final Stack<ShapeAction> redoActions = new Stack<>();

	private final int pageNumber;

	public Page(Document document, int pageNumber) {
		this.document = document;
		this.pageNumber = pageNumber;
	}

	public Rectangle2D getPageRect() {
		return getDocument().getPageRect(pageNumber);
	}

	public PageMetrics getPageMetrics() {
		Rectangle2D pageRect = getPageRect();

		return new PageMetrics(pageRect.getWidth(), pageRect.getHeight());
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public String getPageText() {
		if (pageText == null) {
			pageText = getDocument().getPageText(pageNumber);
		}

		return pageText;
	}

	public List<Rectangle2D> getTextPositions() {
		if (textLayer == null) {
			textLayer = getDocument().getTextPositions(pageNumber);
		}

		return textLayer;
	}

	public List<URI> getUriActions() {
		if (pageUris == null) {
			pageUris = getDocument().getUriActions(pageNumber);
		}

		return pageUris;
	}

	public List<File> getLaunchActions() {
		if (pageLaunchFiles == null) {
			pageLaunchFiles = getDocument().getLaunchActions(pageNumber);
		}

		return pageLaunchFiles;
	}

	/**
	 * Adopt shapes of provided page, if the pages have equal page labels.
	 * Adoption is only performed if this page is the successor of the provided
	 * page.
	 *
	 * @param page The page to adopt to.
	 */
	public void adoptNoLabel(Page page) {
		if (page == null || page.equals(this))
			return;
		
		// No backward adoption.
		if (pageNumber < page.getPageNumber())
			return;
		
		// Perform only within same document.
		if (!document.equals(page.getDocument()))
			return;
		
		for (Shape shape : page.getShapes()) {
			addShape(shape.clone());
		}
	}

	public List<Shape> getShapes() {
		return new CopyOnWriteArrayList<>(shapes);
	}
	
	/**
	 * True, if page has undoable actions.
	 * 
	 * @return {@code true}, if this page has undoable actions.
	 */
	public boolean hasUndoActions() {
		return !undoActions.empty();
	}

	/**
	 * True, if page has re-doable actions.
	 * 
	 * @return {@code true}, if this page has re-doable actions.
	 */
	public boolean hasRedoActions() {
		return !redoActions.empty();
	}
	
	public boolean hasAnnotations() {
		return hasShapes() || hasRedoActions() || hasUndoActions();
	}
	
	public boolean hasSelectedShapes() {
		for (Shape shape : shapes) {
			if (shape.isSelected()) {
				return true;
			}
		}
		
		return false;
	}

	public Stack<ShapeAction> getUndoActions() {
		Stack<ShapeAction> actions = new Stack<>();
		actions.addAll(undoActions);

		return actions;
	}

	public Stack<ShapeAction> getRedoActions() {
		Stack<ShapeAction> actions = new Stack<>();
		actions.addAll(redoActions);

		return actions;
	}

	/**
	 * Undoes the last action
	 */
	public void undo() {
		if (undoActions.empty()) {
			return;
		}

		ShapeAction action = undoActions.pop();
		redoActions.push(action);
		action.undo();
	}

	/**
	 * Redoes the last undone action
	 */
	public void redo() {
		if (redoActions.empty()) {
			return;
		}

		ShapeAction action = redoActions.pop();
		undoActions.push(action);
		action.redo();
	}

	/**
	 * Adds and executes an action.
	 * 
	 * @param action The action to add.
	 */
	public void addAction(ShapeAction action) {
		undoActions.push(action);
		redoActions.clear();
		
		action.execute();
	}

	/**
	 * True, if there are shapes placed in the foreground of the page.
	 * 
	 * @return {@code True}, if this page contains {@link Shape}s.
	 */
	public boolean hasShapes() {
		return !shapes.isEmpty();
	}

	/**
	 * Adds a shape to this page (just calling this won't result in an undoable
	 * action! Use CreateAction instead)
	 * 
	 * @param shape The shape to add.
	 */
	public void addShape(Shape shape) {
		boolean inserted = insertShape(shape);
		
		if (inserted) {
			firePageEdited(shape, null, PageEditEvent.Type.SHAPE_ADDED);
		}
	}

	public void addShapes(List<Shape> shapeList) {
		shapes.addAll(shapeList);

		sendChangeEvent();
	}

	private boolean insertShape(Shape shape) {
		if (contains(shape)) {
			return false;
		}

		shapes.add(shape);

		shape.addShapeChangedListener(psc);

		return true;
	}

	public boolean contains(Shape shape) {
		return shapes.contains(shape);
	}

	public boolean contains(Class<? extends Shape> shapeClass) {
		for (Shape shape : shapes) {
			if (shapeClass.isAssignableFrom(shape.getClass())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the shape at the given index.
	 * 
	 * @param index The index of the shape to retrieve.
	 *
	 * @return The shape at the given index.
	 */
	public Shape getShapeAt(int index) {
		if (index < 0 || index >= shapes.size()) {
			throw new IllegalArgumentException("Index out of Bounds: " + index);
		}

		return shapes.get(index);
	}

	public Shape getShape(int handle) {
		for (Shape shape : shapes) {
			if (shape.getHandle() == handle) {
				return shape;
			}
		}

		return null;
	}

	/**
	 * Remove the shape from this page.
	 * 
	 * @param shape The shape to remove.
	 */
	public void removeShape(Shape shape) {
		if (shapes.remove(shape)) {
			shape.removeShapeChangedListener(psc);
			firePageEdited(shape, null, PageEditEvent.Type.SHAPE_REMOVED);
		}
	}

	public int getShapeCount() {
		return shapes.size();
	}

	/**
	 * Removes the shape at the given index.
	 * 
	 * @param index The index of the shape to remove.
	 */
	public void removeShapeAt(int index) {
		if (index < 0 || index >= shapes.size()) {
			throw new IllegalArgumentException("Index out of Bounds: " + index);
		}

		Shape removed = shapes.remove(index);

		if (removed != null) {
			removed.removeShapeChangedListener(psc);
			firePageEdited(removed, null, PageEditEvent.Type.SHAPE_REMOVED);
		}
	}

	public void removeShape(int handle) {
		if (!hasShapes()) {
			return;
		}

		for (Shape shape : shapes) {
			if (shape.getHandle() == handle) {
				addAction(new DeleteShapeAction(this, List.of(shape)));
				return;
			}
		}
	}

	/**
	 * Adds a listener that is notified whenever the page is edited, e.g. a
	 * shape on the page is edited.
	 *
	 * @param listener The listener to add.
	 */
	public void addPageEditedListener(PageEditedListener listener) {
		listeners.add(listener);
	}

	public void removePageEditedListener(PageEditedListener listener) {
		listeners.remove(listener);
	}

	public void addShapeListener(ShapeListener listener) {
		shapeListeners.add(listener);
	}

	public void removeShapeListener(ShapeListener listener) {
		shapeListeners.remove(listener);
	}

	public void pushShapePaintEvent(ShapePaintEvent event) {
		for (ShapeListener listener : shapeListeners) {
			listener.shapePainted(event);
		}
	}

	public void pushShapeModifyEvent(ShapeModifyEvent event) {
		for (ShapeListener listener : shapeListeners) {
			listener.shapeModified(event);
		}
	}

	public void sendChangeEvent() {
		firePageEdited(null, null, PageEditEvent.Type.SHAPES_ADDED);
	}

	protected void firePageEdited(Shape shape, Rectangle2D dirtyArea, PageEditEvent.Type type) {
		for (PageEditedListener l : listeners) {
			l.pageEdited(new PageEditEvent(this, shape, dirtyArea, type));
		}
	}

	/**
	 * Returns the document this page belongs to.
	 * 
	 * @return The document this page belongs to.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Initializes the page to its original state, only the background shape is
	 * kept
	 */
	public void reset() {
		clear();

		firePageEdited(null, null, PageEditEvent.Type.CLEAR);
	}

	public void deselectShapes() {
		if (!hasSelectedShapes()) {
			return;
		}

		for (Shape shape : getShapes()) {
			shape.setSelected(false);
		}

		sendChangeEvent();    // TODO: send SHAPES_CHANGED event
	}

	public void clear() {
		undoActions.clear();
		redoActions.clear();
		shapes.clear();
	}

}
