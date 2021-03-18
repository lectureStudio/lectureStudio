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

package org.lecturestudio.core.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.app.configuration.ToolConfiguration;
import org.lecturestudio.core.bus.event.BusEvent;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextBoxShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.ArrowTool;
import org.lecturestudio.core.tool.CloneTool;
import org.lecturestudio.core.tool.DeleteAllTool;
import org.lecturestudio.core.tool.EllipseTool;
import org.lecturestudio.core.tool.ExtendViewTool;
import org.lecturestudio.core.tool.HighlighterTool;
import org.lecturestudio.core.tool.LatexTool;
import org.lecturestudio.core.tool.LatexToolSettings;
import org.lecturestudio.core.tool.LineTool;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.PanningTool;
import org.lecturestudio.core.tool.ShapeModifyEvent;
import org.lecturestudio.core.tool.ShapePaintEvent;
import org.lecturestudio.core.tool.ToolContext;
import org.lecturestudio.core.tool.PenTool;
import org.lecturestudio.core.tool.PointerTool;
import org.lecturestudio.core.tool.RectangleTool;
import org.lecturestudio.core.tool.RedoTool;
import org.lecturestudio.core.tool.RubberTool;
import org.lecturestudio.core.tool.SelectGroupTool;
import org.lecturestudio.core.tool.SelectTool;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.core.tool.TextSelectionSettings;
import org.lecturestudio.core.tool.TextSelectionTool;
import org.lecturestudio.core.tool.TextSettings;
import org.lecturestudio.core.tool.TextTool;
import org.lecturestudio.core.tool.Tool;
import org.lecturestudio.core.tool.ToolEvent;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.tool.UndoTool;
import org.lecturestudio.core.tool.ZoomOutTool;
import org.lecturestudio.core.tool.ZoomTool;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * Controller for actions related to the current Page of the current Document.
 *
 * @author Alex Andres
 * @author Tobias
 */
@Singleton
public class ToolController extends Controller implements ToolContext {

	/** The document service to manage all opened documents. */
	private final DocumentService documentService;

	/** The settings for mapped tool types. */
	private final Map<ToolType, PaintSettings> paintSettings;

	/** The configuration for the tools. */
	private final ToolConfiguration toolConfig;

	/** The previously selected tool. */
	private Tool previousTool;

	/** The selected tool. */
	private Tool selectedTool;

	/** The current key event. */
	private KeyEvent keyEvent;

	/** The view matrix of the presentation view. */
	private Matrix viewMatrix;


	@Inject
	public ToolController(ApplicationContext context, DocumentService documentService) {
		super(context);

		this.documentService = documentService;

		toolConfig = getConfig().getToolConfig();

		StrokeSettings penSettings = new StrokeSettings(toolConfig.getPenSettings());
		StrokeSettings highlighterSettings = new StrokeSettings(toolConfig.getHighlighterSettings());
		StrokeSettings pointerSettings = new StrokeSettings(toolConfig.getPointerSettings());
		StrokeSettings arrowSettings = new StrokeSettings(toolConfig.getArrowSettings());
		StrokeSettings lineSettings = new StrokeSettings(toolConfig.getLineSettings());
		StrokeSettings rectangleSettings = new StrokeSettings(toolConfig.getRectangleSettings());
		StrokeSettings ellipseSettings = new StrokeSettings(toolConfig.getEllipseSettings());
		TextSelectionSettings textSelectionSettings = new TextSelectionSettings(toolConfig.getTextSelectionSettings());
		TextSettings textSettings = new TextSettings(toolConfig.getTextSettings());
		LatexToolSettings latexSettings = new LatexToolSettings(toolConfig.getLatexSettings());

		// Bind controller settings to configuration settings.
		toolConfig.getPenSettings().widthProperty().addListener((observable, oldValue, newValue) -> penSettings.setWidth(newValue));
		toolConfig.getHighlighterSettings().widthProperty().addListener((observable, oldValue, newValue) -> highlighterSettings.setWidth(newValue));
		toolConfig.getPointerSettings().widthProperty().addListener((observable, oldValue, newValue) -> pointerSettings.setWidth(newValue));

		paintSettings = new HashMap<>();
		paintSettings.put(ToolType.PEN, penSettings);
		paintSettings.put(ToolType.HIGHLIGHTER, highlighterSettings);
		paintSettings.put(ToolType.POINTER, pointerSettings);
		paintSettings.put(ToolType.ARROW, arrowSettings);
		paintSettings.put(ToolType.LINE, lineSettings);
		paintSettings.put(ToolType.RECTANGLE, rectangleSettings);
		paintSettings.put(ToolType.ELLIPSE, ellipseSettings);
		paintSettings.put(ToolType.TEXT_SELECTION, textSelectionSettings);
		paintSettings.put(ToolType.TEXT, textSettings);
		paintSettings.put(ToolType.LATEX, latexSettings);
	}

	@Subscribe
	public final void onEvent(DocumentEvent event) {
		if (isNull(selectedTool)) {
			return;
		}

		Document selectedDoc = documentService.getDocuments().getSelectedDocument();

		if (isNull(selectedDoc)) {
			return;
		}

		if (selectedDoc.isWhiteboard() && selectedTool.getType() == ToolType.TEXT_SELECTION) {
			if (previousTool.getType() == ToolType.TEXT_SELECTION) {
				selectPenTool();
			}
			else {
				selectPreviousTool();
			}
		}
	}

	@Subscribe
	public final void onEvent(PageEvent event) {
		if (isNull(selectedTool)) {
			return;
		}

		PresentationParameterProvider ppProvider = getContext().getPagePropertyPropvider(ViewType.User);

		boolean hasZoom = ppProvider.getParameter(event.getPage()).isZoomMode();

		if (!hasZoom && selectedTool.getType() == ToolType.PANNING) {
			selectPreviousTool();
		}
	}

	@Override
	public void fireToolEvent(ToolEvent event) {
//		System.out.println(event.getType());

		Document document = documentService.getDocuments().getSelectedDocument();
		Page page = document.getCurrentPage();

		if (event instanceof ShapePaintEvent) {
			page.pushShapePaintEvent((ShapePaintEvent) event);
		}
		else if (event instanceof ShapeModifyEvent) {
			page.pushShapeModifyEvent((ShapeModifyEvent) event);
		}
	}

	@Override
	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	@Override
	public double getPageScale() {
		if (getSelectedTool().getType() == ToolType.HIGHLIGHTER) {
			// Scale highlighter only if the corresponding option is set.
			if (!toolConfig.getHighlighterSettings().getScale()) {
				// Identity viewTransform.
				return 1;
			}
		}

		return getScale();
	}

	@Override
	public Matrix getViewTransform() {
		return viewMatrix;
	}

	@Override
	public void recordAction(PlaybackAction action) {
		getContext().getEventBus().post(new RecordActionEvent(action));
	}

	@Override
	protected void initInternal() throws ExecutableException {
		super.initInternal();

		getContext().getEventBus().register(this);
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		super.destroyInternal();

		getContext().getEventBus().unregister(this);
	}

	/**
	 * The the view matrix of the presentation view.
	 *
	 * @param matrix The view matrix to set.
	 */
	public void setViewTransform(Matrix matrix) {
		this.viewMatrix = matrix;
	}

	/**
	 * Get paint settings for the specified tool type.
	 *
	 * @param toolType The type of a tool for which to retrieve the settings.
	 * @param <T>      The type of the settings object.
	 *
	 * @return paint settings for the specified tool type.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends PaintSettings> T getPaintSettings(ToolType toolType) {
		return (T) paintSettings.get(toolType);
	}

	@Override
	public PresentationParameterProvider getPresentationParameterProvider(ViewType viewType) {
		return getContext().getPagePropertyPropvider(viewType);
	}

	/**
	 * Set the occurred key event that the user may have caused.
	 *
	 * @param event The key event to set.
	 */
	public void setKeyEvent(KeyEvent event) {
		keyEvent = event;

		Tool tool = getSelectedTool();

		if (nonNull(tool)) {
			tool.setKeyEvent(event);
		}
	}

	/**
	 * Select the pen tool.
	 */
	public void selectPenTool() {
		setTool(new PenTool(this));
	}

	/**
	 * Select the highlighter tool.
	 */
	public void selectHighlighterTool() {
		setTool(new HighlighterTool(this));
	}

	/**
	 * Select the pointer tool.
	 */
	public void selectPointerTool() {
		setTool(new PointerTool(this));
	}

	/**
	 * Select the LaTeX tool.
	 */
	public void selectLatexTool() {
		setTool(new LatexTool(this));
	}

	/**
	 * Selects the LaTeX Tool and assigns the given handle to the created
	 * LaTeXShape.
	 *
	 * @param handle The shape handle.
	 */
	public void selectLatexTool(int handle) {
		setTool(new LatexTool(this, handle));
	}

	/**
	 * Select the text tool.
	 */
	public void selectTextTool() {
		setTool(new TextTool(this));
	}

	/**
	 * Select the text tool and assigns the given handle to the created
	 * TextShape.
	 *
	 * @param handle The shape handle.
	 */
	public void selectTextTool(int handle) {
		setTool(new TextTool(this, handle));
	}

	/**
	 * Set the font for the text tool.
	 *
	 * @param font The font to set.
	 */
	public void setTextFont(Font font) {
		((TextSettings) getPaintSettings(ToolType.TEXT)).setFont(font);
	}

	/**
	 * Sets the text attributes for the text tool.
	 *
	 * @param attributes The text attributes to set.
	 */
	public void setTextAttributes(TextAttributes attributes) {
		((TextSettings) getPaintSettings(ToolType.TEXT)).setTextAttributes(attributes);
	}

	/**
	 * Select the text-selection tool.
	 */
	public void selectTextSelectionTool() {
		setTool(new TextSelectionTool(this));
	}

	/**
	 * Select the text-selection tool with predefined text bounding boxes to
	 * select.
	 *
	 * @param textPositions The predefined text bounding boxes to select.
	 */
	public void selectTextSelectionTool(List<Rectangle2D> textPositions) {
		setTool(new TextSelectionTool(this, textPositions));
	}

	/**
	 * Select the line tool.
	 */
	public void selectLineTool() {
		setTool(new LineTool(this));
	}

	/**
	 * Select the arrow tool.
	 */
	public void selectArrowTool() {
		setTool(new ArrowTool(this));
	}

	/**
	 * Select the rectangle tool.
	 */
	public void selectRectangleTool() {
		setTool(new RectangleTool(this));
	}

	/**
	 * Select the ellipse tool.
	 */
	public void selectEllipseTool() {
		setTool(new EllipseTool(this));
	}

	/**
	 * Select the select tool.
	 */
	public void selectSelectTool() {
		Tool selectedTool = getSelectedTool();

		if (isNull(selectedTool)) {
			selectSelectionTool();
			return;
		}

		// Toggle in cycle.
		if (selectedTool.getType() == ToolType.SELECT) {
			selectGroupSelectionTool();
		}
		else if (selectedTool.getType() == ToolType.SELECT_GROUP) {
			selectCloneTool();
		}
		else {
			selectSelectionTool();
		}
	}

	/**
	 * Select the clone tool.
	 */
	public void selectCloneTool() {
		setTool(new CloneTool(this));
	}

	/**
	 * Select the selection tool.
	 */
	public void selectSelectionTool() {
		setTool(new SelectTool(this));
	}

	/**
	 * Select the group-selection tool.
	 */
	public void selectGroupSelectionTool() {
		setTool(new SelectGroupTool(this));
	}

	/**
	 * Select the rubber tool.
	 */
	public void selectRubberTool() {
		setTool(new RubberTool(this));
	}

	/**
	 * Select the zoom tool.
	 */
	public void selectZoomTool() {
		setTool(new ZoomTool(this));
	}

	/**
	 * Select the panning tool.
	 */
	public void selectPanningTool() throws IllegalStateException {
		if (!isZoomMode()) {
			throw new IllegalStateException("No zoom previously performed.");
		}

		setTool(new PanningTool(this));
	}

	/**
	 * Select the delete-all tool to remove all annotations from the current
	 * page.
	 */
	public void selectDeleteAllTool() {
		setTool(new DeleteAllTool(this));

		simpleToolAction();
	}

	/**
	 * Toggle the extended drawing area mode on the current page on all views.
	 */
	public void selectExtendViewTool() {
		setTool(new ExtendViewTool(this));

		simpleToolAction();
	}

	/**
	 * Reset the page content to its original size.
	 */
	public void selectZoomOutTool() {
		setTool(new ZoomOutTool(this));

		simpleToolAction();
	}

	/**
	 * Undo the last action on the current page. If the page contains no actions
	 * the operation has no effect.
	 */
	public void undo() {
		setTool(new UndoTool(this));

		simpleToolAction();
	}

	/**
	 * Redo the last undone action on the current page. If the page contains no
	 * undone actions the operation has no effect.
	 */
	public void redo() {
		setTool(new RedoTool(this));

		simpleToolAction();
	}

	/**
	 * Select the next page in the selected document.
	 */
	public void selectNextPage() {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (nonNull(doc)) {
			int currentPage = doc.getCurrentPageNumber();

			selectPage(doc, currentPage + 1);
		}
	}

	/**
	 * Select the page with the specified page number in the selected document.
	 *
	 * @param pageNumber The page number of the page to select.
	 */
	public void selectPage(int pageNumber) {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (nonNull(doc)) {
			selectPage(doc, pageNumber);
		}
	}

	private void selectPage(Document document, int pageNumber) {
		int currentPageNumber = document.getCurrentPageNumber();

		if (document.selectPage(pageNumber)) {
			Page oldPage = document.getPage(currentPageNumber);
			Page newPage = document.getPage(pageNumber);

			pushEvent(new PageEvent(newPage, oldPage, PageEvent.Type.SELECTED));
		}
	}

	/**
	 * Create a page on the active whiteboard. Does nothing if the active
	 * document is not a whiteboard.
	 */
	public void createWhiteboardPage() {
		Document selectedDocument = documentService.getDocuments().getSelectedDocument();

		if (nonNull(selectedDocument) /*&& selectedDocument.isWhiteboard()*/) {
			Page page = selectedDocument.createPage();

			pushEvent(new PageEvent(page, PageEvent.Type.CREATED));

			selectPage(selectedDocument, page.getPageNumber());
		}
	}

	/**
	 * Remove a page with the specified page number on the active whiteboard.
	 * Does nothing if the selected document is not a whiteboard. If this would
	 * lead to an empty whiteboard, a new blank page is set as the first page of
	 * the whiteboard.
	 *
	 * @param pageNumber The number of the page to remove from the whiteboard.
	 */
	public void deleteWhiteboardPage(int pageNumber) {
		Document selectedDocument = documentService.getDocuments().getSelectedDocument();

		if (selectedDocument.isWhiteboard()) {
			if (selectedDocument.getPageCount() > 1) {
				Page page = selectedDocument.getPage(pageNumber);

				if (selectedDocument.removePage(page)) {
					pushEvent(new PageEvent(page, PageEvent.Type.REMOVED));

					// Check if the removed page was selected.
					if (pageNumber == selectedDocument.getPages().size()) {
						pageNumber--;
					}

					selectPage(selectedDocument, pageNumber);
				}
			}
			else {
				selectedDocument.getCurrentPage().reset();
			}
		}
	}

	/**
	 * Toggle the zoom tool.
	 */
	private void toggleZoomTool() {
		Tool tool = getSelectedTool();

		if (tool != null && tool.getType() == ToolType.ZOOM) {
			selectPreviousTool();
		}
		else {
			selectZoomTool();
		}
	}

	/**
	 * Zoom the page to the given bounds described by a rectangle.
	 *
	 * @param rect The zoom-rectangle.
	 */
	public void zoom(Rectangle2D rect) {
		if (rect.isEmpty()) {
			return;
		}

		Document doc = documentService.getDocuments().getSelectedDocument();
		Page page = null;

		if (nonNull(doc)) {
			page = doc.getCurrentPage();
		}
		if (isNull(page)) {
			return;
		}

		// Zoom on user view and presentation view
		PresentationParameterProvider ppp = getContext().getPagePropertyPropvider(ViewType.User);
		PresentationParameter param = ppp.getParameter(page);
		param.zoom(rect);

		ppp = getContext().getPagePropertyPropvider(ViewType.Preview);
		param = ppp.getParameter(page);
		param.zoom(rect);

		ppp = getContext().getPagePropertyPropvider(ViewType.Presentation);
		param = ppp.getParameter(page);
		param.zoom(rect);
	}

	/**
	 * Set the paint color of the selected paint tool.
	 *
	 * @param color The new paint color.
	 */
	public void selectPaintColor(Color color) {
		if (isNull(color)) {
			return;
		}

		if (nonNull(selectedTool)) {
			PaintSettings settings = getPaintSettings(selectedTool.getType());

			if (nonNull(settings)) {
				settings.setColor(color);
			}
		}
	}

	/**
	 * Start a paint action with the current PaintTool at the given position.
	 *
	 * @param point The location on the painting surface where to start.
	 */
	public void beginToolAction(PenPoint2D point) {
		if (nonNull(getSelectedTool())) {
			Document doc = documentService.getDocuments().getSelectedDocument();

			if (isNull(doc)) {
				return;
			}

			Page page = doc.getCurrentPage();

			getSelectedTool().begin(point, page);
		}
	}

	/**
	 * Execute the paint action with the current PaintTool at the given
	 * position.
	 *
	 * @param point The location on the painting surface where to execute.
	 */
	public void executeToolAction(PenPoint2D point) {
		if (nonNull(getSelectedTool())) {
			getSelectedTool().execute(point);
		}
	}

	/**
	 * End the paint action with the current PaintTool at the given position.
	 *
	 * @param point The location on the painting surface where to end.
	 */
	public void endToolAction(PenPoint2D point) {
		Tool tool = getSelectedTool();

		if (nonNull(tool)) {
			tool.end(point);

			if (tool.getType() == ToolType.ZOOM) {
				toggleZoomTool();
			}
		}
	}

	/**
	 * Copy a TextShape.
	 *
	 * @param shape The TextShape to copy.
	 */
	public void copyText(TextShape shape) {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (isNull(doc)) {
			return;
		}

		Page page = doc.getCurrentPage();

		PenPoint2D loc = new PenPoint2D(shape.getLocation().getX(), shape.getLocation().getY());

		TextTool tool = new TextTool(this, shape.getHandle());
		tool.begin(loc, page);
		tool.execute(loc);
		tool.end(loc);
		tool.copy(shape);
	}

	/**
	 * Copy a TeXShape.
	 *
	 * @param shape The TeXShape to copy.
	 */
	public void copyTeX(TeXShape shape) {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (isNull(doc)) {
			return;
		}

		Page page = doc.getCurrentPage();

		PenPoint2D loc = new PenPoint2D(shape.getLocation().getX(), shape.getLocation().getY());

		LatexTool tool = new LatexTool(this, shape.getHandle());
		tool.begin(loc, page);
		tool.execute(loc);
		tool.end(loc);
		tool.copy(shape);
	}

	/**
	 * Set text of a text shape with the specified handle.
	 *
	 * @param handle The handle of a text shape.
	 * @param text   The new text to set.
	 *
	 * @throws Exception If the text shape could not be found.
	 */
	public void setText(int handle, String text) throws Exception {
		TextBoxShape<?> textShape = getTextShape(handle);
		textShape.setText(text);
	}

	/**
	 * Set text attributes of a text shape with the specified handle.
	 *
	 * @param handle     The handle of a text shape.
	 * @param color      The text color to set.
	 * @param font       The text font to set.
	 * @param attributes The text attributes to set.
	 *
	 * @throws Exception If the text shape could not be found.
	 */
	@SuppressWarnings("unchecked")
	public void setTextFont(int handle, Color color, Font font, TextAttributes attributes) throws Exception {
		TextBoxShape<Font> textShape = (TextBoxShape<Font>) getTextShape(handle);
		textShape.setFont(font);
		textShape.setTextAttributes(attributes);
		textShape.setTextColor(color);
	}

	/**
	 * Set LaTeX attributes of a LaTeX shape with the specified handle.
	 *
	 * @param handle The handle of a text shape.
	 * @param color  The text color to set.
	 * @param font   The text font to set.
	 *
	 * @throws Exception If the LaTeX shape could not be found.
	 */
	@SuppressWarnings("unchecked")
	public void setTeXFont(int handle, Color color, TeXFont font) throws Exception {
		TextBoxShape<TeXFont> textShape = (TextBoxShape<TeXFont>) getTextShape(handle);
		textShape.setFont(font);
		textShape.setTextColor(color);
	}

	/**
	 * Set the location of a text shape with the specified handle.
	 *
	 * @param handle   The handle of a text shape.
	 * @param location The new location of the shape.
	 *
	 * @throws Exception If the text shape could not be found.
	 */
	public void setTextLocation(int handle, Point2D location) throws Exception {
		TextBoxShape<?> textShape = getTextShape(handle);
		textShape.setLocation(location);
	}

	/**
	 * Remove a text shape that has the specified shape handle.
	 *
	 * @param handle The handle of a shape to remove.
	 *
	 * @throws Exception If the text shape could not be found.
	 */
	public void removeText(int handle) throws Exception {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();
		Page page = selectedDoc.getCurrentPage();
		getTextShape(handle);

		page.removeShape(handle);
	}

	/**
	 * Toggle the grid on/off.
	 */
	public void toggleGrid() {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();

		PresentationParameterProvider provider = getContext().getPagePropertyPropvider(ViewType.User);
		PresentationParameter param = provider.getParameter(selectedDoc.getCurrentPage());

		// Toggle
		boolean showGrid = !param.showGrid();

		param.setShowGrid(showGrid);

		GridConfiguration gridConfig = getConfig().getGridConfig();

		if (gridConfig.getShowGridOnDisplays()) {
			provider = getContext().getPagePropertyPropvider(ViewType.Presentation);
			param = provider.getParameter(selectedDoc.getCurrentPage());

			// Sync with user's view.
			param.setShowGrid(showGrid);
		}
	}

	/**
	 * Select the previously selected tool.
	 */
	public void selectPreviousTool() {
		setTool(previousTool);
	}

	protected void pushEvent(BusEvent event) {
		getContext().getEventBus().post(event);
	}

	/**
	 * Set the new painting tool.
	 */
	private void setTool(Tool tool) {
		if (isNull(tool)) {
			return;
		}
		if (nonNull(getSelectedTool()) && getSelectedTool() == tool) {
			return;
		}

		setPreviousTool(getSelectedTool());

		if (needDeselect(tool)) {
			Document doc = documentService.getDocuments().getSelectedDocument();

			if (nonNull(doc)) {
				Page page = doc.getCurrentPage();
				page.deselectShapes();
			}
		}

		selectedTool = tool;

		pushEvent(new ToolSelectionEvent(tool.getType(), getPaintSettings(tool.getType())));
	}

	private void setPreviousTool(Tool tool) {
		if (isNull(tool)) {
			return;
		}

		switch (tool.getType()) {
			// Don't remember simple tools.
			case UNDO:
			case REDO:
			case ZOOM:
			case ZOOM_OUT:
			case PANNING:
			case RUBBER:
			case DELETE_ALL:
			case EXTEND_VIEW:
				return;

			default:
				previousTool = tool;
				break;
		}
	}

	/**
	 * Get the type of the selected tool.
	 *
	 * @return the type of the selected tool.
	 */
	ToolType getSelectedToolType() {
		return nonNull(selectedTool) ? selectedTool.getType() : null;
	}

	/**
	 * Get the selected tool.
	 *
	 * @return the selected tool.
	 */
	private Tool getSelectedTool() {
		return selectedTool;
	}

	private boolean needDeselect(Tool tool) {
		switch (tool.getType()) {
			case SELECT:
			case SELECT_GROUP:
			case CLONE:
				return false;

			default:
				return true;
		}
	}

	/**
	 * Returns the scale for the user-view.
	 */
	private double getScale() {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (isNull(doc)) {
			return 1;
		}

		PresentationParameterProvider ppp = getContext().getPagePropertyPropvider(ViewType.User);
		PresentationParameter para = ppp.getParameter(doc.getCurrentPage());

		return 1 / (1 / para.getPageRect().getWidth());
	}

	/**
	 * Executes the selected tool as a simple action.
	 */
	private void simpleToolAction() {
		beginToolAction(null);
		executeToolAction(null);
		endToolAction(null);

		selectPreviousTool();
	}

	private TextBoxShape<?> getTextShape(int handle) throws Exception {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();
		Page page = selectedDoc.getCurrentPage();
		Shape shape = page.getShape(handle);

		if (isNull(shape) || !(shape instanceof TextBoxShape)) {
			throw new Exception("No text shape with given handle found.");
		}

		return (TextBoxShape<?>) shape;
	}

	/**
	 * Returns true if the selected page is zoomed on the user-view.
	 */
	private boolean isZoomMode() {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (isNull(doc)) {
			return false;
		}

		Page page = doc.getCurrentPage();
		PresentationParameterProvider ppp = getContext().getPagePropertyPropvider(ViewType.User);
		PresentationParameter para = ppp.getParameter(page);

		return para.isZoomMode();
	}

}
