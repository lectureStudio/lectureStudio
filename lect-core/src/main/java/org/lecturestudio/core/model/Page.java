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

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.action.ShapeAction;
import org.lecturestudio.core.model.action.DeleteShapeAction;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ShapeChangeListener;
import org.lecturestudio.core.model.listener.ShapeListener;
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.ZoomShape;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;

/**
 * This class is the representation of a {@link Page} in a {@link Document}. It
 * has a background shape, that can either be empty (white board page) or a
 * SlideShape holding a PDF-Slide. The foreground of the page consists of an
 * arbitrary number of shapes. Adding or removing shapes should be done by
 * actions only, so they can be un- and redone.
 * <p>
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

	/** The document this page belongs to. */
	private final Document document;

	/** Cached page text. */
	private String pageText;
	
	/** Cached page text layer. */
	private List<Rectangle2D> textLayer;

	/** The shapes of this page. */
	private final List<Shape> shapes = new CopyOnWriteArrayList<>();

	private final PropagateShapeChange psc = new PropagateShapeChange();

	/** A set of page edit listeners. */
	private final Set<PageEditedListener> listeners = ConcurrentHashMap.newKeySet();

	/** A set of shape listeners. */
	private final Set<ShapeListener> shapeListeners = ConcurrentHashMap.newKeySet();

	/** The undo actions of this page. */
	private final Stack<ShapeAction> undoActions = new Stack<>();
	/** The redo actions of this page. */
	private final Stack<ShapeAction> redoActions = new Stack<>();

	/** The page number of this page. */
	private int pageNumber;

	/** The unique ID of this document. */
	private UUID uid;


	/**
	 * Create a new Page with the specified document and page number.
	 *
	 * @param document The document.
	 * @param pageNumber The page number.
	 */
	public Page(Document document, int pageNumber) {
		this.document = document;
		this.pageNumber = pageNumber;
	}

	/**
	 * Get unique page ID. The unique ID can be used to differentiate pages with
	 * same index but with different internal content.
	 *
	 * @return The unique page ID.
	 */
	public UUID getUid() {
		return uid;
	}

	/**
	 * Set unique page ID. The unique ID can be used to differentiate pages with
	 * same index but with different internal content.
	 *
	 * @param uid The unique document ID.
	 */
	public void setUid(UUID uid) {
		this.uid = uid;
	}

	/**
	 * Get the media box of this page.
	 *
	 * @return The media box of this page.
	 */
	public Rectangle2D getPageRect() {
		return getDocument().getPageRect(pageNumber);
	}

	/**
	 * Get the page metrics (uses the media box of this page).
	 *
	 * @return The page metrics.
	 */
	public PageMetrics getPageMetrics() {
		Rectangle2D pageRect = getPageRect();

		return new PageMetrics(pageRect.getWidth(), pageRect.getHeight());
	}

	/**
	 * Get the page number of this page.
	 *
	 * @return The page number of this page.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	@Deprecated
	public void setPageNumber(int number) {
		this.pageNumber = number;
	}

	/**
	 * Get the page text of this page.
	 *
	 * @return The page text of this page.
	 */
	public String getPageText() {
		if (pageText == null) {
			pageText = getDocument().getPageText(pageNumber);
		}

		return pageText;
	}

	/**
	 * Get the text positions of this page.
	 *
	 * @return The text positions of this page.
	 */
	public List<Rectangle2D> getTextPositions() {
		if (textLayer == null) {
			textLayer = getDocument().getTextPositions(pageNumber);
		}

		return textLayer;
	}

	/**
	 * Copies the annotations and the undo/redo stack of the provided page to
	 * this page.
	 *
	 * @param page The page to copy.
	 */
	public void copy(Page page) {
		if (page == null || page.equals(this)) {
			return;
		}

		for (Shape shape : page.getShapes()) {
			if (shape instanceof PointerShape || shape instanceof ZoomShape) {
				continue;
			}

			addShape(shape.clone());
		}

		undoActions.addAll(page.getUndoActions());
		redoActions.addAll(page.getRedoActions());
	}

	/**
	 * Adopt shapes of provided page if the pages have equal page labels.
	 * Adoption is only performed if this page is the successor of the provided
	 * page.
	 *
	 * @param page The page to adopt to.
	 */
	public void adoptNoLabel(Page page) {
		if (page == null || page.equals(this)) {
			return;
		}
		
		// No backward adoption.
		if (pageNumber < page.getPageNumber()) {
			return;
		}
		
		// Perform only within same document.
		if (!document.equals(page.getDocument())) {
			return;
		}
		
		for (Shape shape : page.getShapes()) {
			addShape(shape.clone());
		}
	}

	/**
	 * Get the shapes of this page.
	 *
	 * @return The shapes of this page.
	 */
	public List<Shape> getShapes() {
		return new CopyOnWriteArrayList<>(shapes);
	}

	/**
	 * Specifies whether this page has undoable actions.
	 *
	 * @return {@code true} if this page has undoable actions, otherwise
	 * {@code false}.
	 */
	public boolean hasUndoActions() {
		return !undoActions.empty();
	}

	/**
	 * Specifies whether this page has re-doable actions.
	 *
	 * @return {@code true} if this page has re-doable actions, otherwise
	 * {@code false}.
	 */
	public boolean hasRedoActions() {
		return !redoActions.empty();
	}

	/**
	 * Specifies whether this page has annotations.
	 *
	 * @return {@code true} if this page has re-doable actions, undoable actions
	 * or contains {@link Shape}s, otherwise {@code false}.
	 */
	public boolean hasAnnotations() {
		return hasShapes() || hasRedoActions() || hasUndoActions();
	}

	/**
	 * Specifies whether this pages has shapes that are selected.
	 *
	 * @return {@code true} if {@link #shapes} contains shapes that are selected.
	 */
	public boolean hasSelectedShapes() {
		for (Shape shape : shapes) {
			if (shape.isSelected()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get all undo actions.
	 *
	 * @return All undo actions.
	 */
	public Stack<ShapeAction> getUndoActions() {
		Stack<ShapeAction> actions = new Stack<>();
		actions.addAll(undoActions);

		return actions;
	}

	/**
	 * Get all redo actions.
	 *
	 * @return All redo actions.
	 */
	public Stack<ShapeAction> getRedoActions() {
		Stack<ShapeAction> actions = new Stack<>();
		actions.addAll(redoActions);

		return actions;
	}

	/**
	 * Undoes the last action.
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
	 * Redoes the last undone action.
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
	 * Specifies whether are shapes placed in the foreground of the page.
	 *
	 * @return {@code true} if this page contains {@link Shape}s, otherwise
	 * {@code false}.
	 */
	public boolean hasShapes() {
		return !shapes.isEmpty();
	}

	/**
	 * Adds a shape to this page (just calling this won't result in an undoable
	 * action! Use CreateAction instead).
	 *
	 * @param shape The shape to add.
	 */
	public void addShape(Shape shape) {
		boolean inserted = insertShape(shape);

		if (inserted) {
			firePageEdited(shape, null, PageEditEvent.Type.SHAPE_ADDED);
		}
	}

	/**
	 * Adds the specified list of shapes to {@link #shapes}.
	 *
	 * @param shapeList The list of shapes to add.
	 */
	public void addShapes(List<Shape> shapeList) {
		shapes.addAll(shapeList);

		sendChangeEvent();
	}

	/**
	 * Adds the specified shape to {@link #shapes} if it is not already
	 * included.
	 *
	 * @param shape The shape to add.
	 *
	 * @return {@code false} if {@link #shapes} already contained the shape,
	 * otherwise {@code true}.
	 */
	private boolean insertShape(Shape shape) {
		if (contains(shape)) {
			return false;
		}

		shapes.add(shape);

		shape.addShapeChangedListener(psc);

		return true;
	}

	/**
	 * Specifies whether {@link #shapes} contains the specified shape.
	 *
	 * @param shape The shape to check.
	 *
	 * @return {@code true} if {@link #shapes} contains the specified shape,
	 * otherwise {@code false}.
	 */
	public boolean contains(Shape shape) {
		return shapes.contains(shape);
	}

	/**
	 * Specifies whether the specified {@link Class} is either the same as, or
	 * is a superclass or superinterface of, the class or interface from one of
	 * the shapes in {@link #shapes}.
	 *
	 * @param shapeClass The {@link Class}.
	 *
	 * @return {@code true} if the specified {@link Class} is either the same
	 * as, or is a superclass or superinterface of, the class or interface from
	 * one of the shapes in {@link #shapes}, otherwise {@code false}.
	 */
	public boolean contains(Class<? extends Shape> shapeClass) {
		for (Shape shape : shapes) {
			if (shapeClass.isAssignableFrom(shape.getClass())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Retrieves all shapes on the page with the specified class type.
	 *
	 * @param shapeClass The class of shapes to search for.
	 *
	 * @return A list of all shapes with the specified class.
	 */
	public List<? extends Shape> getShapes(Class<? extends Shape> shapeClass) {
		return shapes.stream()
				.filter(s -> shapeClass.isAssignableFrom(s.getClass()))
				.collect(Collectors.toList());
	}

	/**
	 * Get the first shape in {@link #shapes} whose handle equals the specified
	 * handle.
	 *
	 * @param handle The handle of the searched shape.
	 *
	 * @return The first shape in {@link #shapes} whose handle equals the
	 * specified handle or {@code null} if no such shape was found.
	 */
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

	/**
	 * Creates a {@link DeleteShapeAction} with this page and the first shape in
	 * {@link #shapes} whose handle equals the specified handle.
	 *
	 * @param handle The handle of the shape to delete.
	 */
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

	/**
	 * Removes the specified listener from {@link #listeners}.
	 *
	 * @param listener The listener to remove.
	 */
	public void removePageEditedListener(PageEditedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Adds the specified listener to {@link #shapeListeners}.
	 *
	 * @param listener The listener to add.
	 */
	public void addShapeListener(ShapeListener listener) {
		shapeListeners.add(listener);
	}

	/**
	 * Removes the specified listener from {@link #shapeListeners}.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeShapeListener(ShapeListener listener) {
		shapeListeners.remove(listener);
	}

	/**
	 * Calls {@link ShapeListener#shapePainted(ShapePaintEvent)} on every
	 * listener in {@link #shapeListeners} with the specified shape paint
	 * event.
	 *
	 * @param event The shape paint event.
	 */
	public void pushShapePaintEvent(ShapePaintEvent event) {
		for (ShapeListener listener : shapeListeners) {
			listener.shapePainted(event);
		}
	}

	/**
	 * Calls {@link ShapeListener#shapeModified(ShapeModifyEvent)} on every
	 * listener in {@link #shapeListeners} with the specified shape modify
	 * event.
	 *
	 * @param event The shape modify event.
	 */
	public void pushShapeModifyEvent(ShapeModifyEvent event) {
		for (ShapeListener listener : shapeListeners) {
			listener.shapeModified(event);
		}
	}

	/**
	 * Calls {@link #firePageEdited(Shape, Rectangle2D, PageEditEvent.Type)}
	 * with {@code null} as shape and dirty area and
	 * {@code PageEditEvent.Type.SHAPES_ADDED} as page edit event type.
	 */
	public void sendChangeEvent() {
		firePageEdited(null, null, PageEditEvent.Type.SHAPES_ADDED);
	}

	/**
	 * Calls {@link PageEditedListener#pageEdited(PageEditEvent)} on every
	 * listener in {@link #listeners} with a newly created {@link PageEditEvent}
	 * using this page and the specified shape, dirty area and page edit event
	 * type.
	 *
	 * @param shape     The shape.
	 * @param dirtyArea The dirty area.
	 * @param type      The page edit event type.
	 */
	protected void firePageEdited(Shape shape, Rectangle2D dirtyArea,
			PageEditEvent.Type type) {
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
	 * kept.
	 */
	public void reset() {
		clear();

		firePageEdited(null, null, PageEditEvent.Type.CLEAR);
	}

	/**
	 * Deselects every shape in {@link #shapes}.
	 */
	public void deselectShapes() {
		if (!hasSelectedShapes()) {
			return;
		}

		for (Shape shape : getShapes()) {
			shape.setSelected(false);
		}

		sendChangeEvent();    // TODO: send SHAPES_CHANGED event
	}

	/**
	 * Removes all undo actions, redo actions and shapes.
	 */
	public void clear() {
		undoActions.clear();
		redoActions.clear();
		shapes.clear();
	}

}
