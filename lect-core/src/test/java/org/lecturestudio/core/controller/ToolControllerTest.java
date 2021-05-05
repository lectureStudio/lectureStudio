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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.lecturestudio.core.CoreTest;
import org.lecturestudio.core.app.configuration.ToolConfiguration;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.GeometryUtils;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.ArrowShape;
import org.lecturestudio.core.model.shape.EllipseShape;
import org.lecturestudio.core.model.shape.LineShape;
import org.lecturestudio.core.model.shape.PenShape;
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.model.shape.RectangleShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ToolControllerTest extends CoreTest {

	private DocumentService documentService;

	private ToolController controller;


	@BeforeEach
	void setUp() throws IOException {
		context.getConfiguration().setExtendPageDimension(new Dimension2D(1.3, 1.3));

		ToolConfiguration toolConfig = context.getConfiguration().getToolConfig();

		toolConfig.getPenSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getPenSettings().setWidth(3);
		toolConfig.getHighlighterSettings().setColor(PresetColor.ORANGE.getColor());
		toolConfig.getHighlighterSettings().setAlpha(140);
		toolConfig.getHighlighterSettings().setWidth(11);
		toolConfig.getHighlighterSettings().setScale(false);
		toolConfig.getPointerSettings().setColor(PresetColor.RED.getColor());
		toolConfig.getPointerSettings().setAlpha(140);
		toolConfig.getPointerSettings().setWidth(11);
		toolConfig.getArrowSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getArrowSettings().setWidth(33);
		toolConfig.getLineSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getLineSettings().setWidth(3);
		toolConfig.getRectangleSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getRectangleSettings().setWidth(3);
		toolConfig.getEllipseSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getEllipseSettings().setWidth(3);
		toolConfig.getTextSelectionSettings().setColor(PresetColor.ORANGE.getColor());
		toolConfig.getTextSelectionSettings().setAlpha(140);
		toolConfig.getTextSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getTextSettings().setFont(new Font("Arial", 24));
		toolConfig.getTextSettings().setTextAttributes(new TextAttributes());
		toolConfig.getLatexSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getLatexSettings().setFont(new TeXFont(TeXFont.Type.SERIF, 20));

		documentService = new DocumentService(context);
		controller = new ToolController(context, documentService);

		Document doc = new Document();
		doc.createPage();
		doc.createPage();

		documentService.addDocument(doc);
		documentService.selectDocument(doc);
	}

	@Test
	void testArrowTool() {
		controller.selectArrowTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof ArrowShape);
	}

	@Test
	void testCloneTool() {
		LinkedList<PenPoint2D> penPoints = createRandomPoints();
		controller.selectPenTool();
		executeTool(penPoints);

		LinkedList<PenPoint2D> clonePoints = createRandomPoints();
		clonePoints.set(0, penPoints.get(5));
		controller.selectCloneTool();

		executeTool(clonePoints);

		assertEquals(2, getShapes().size());
		assertTrue(getShapes().get(0) instanceof PenShape);
		assertTrue(getShapes().get(1) instanceof PenShape);
		assertFalse(getShapes().get(0).isSelected());
		assertTrue(getShapes().get(1).isSelected());

		executeTool(createOutOfBoundsPoints());

		for (Shape shape : getShapes()) {
			assertFalse(shape.isSelected());
		}
	}

	@Test
	void testDeleteAllTool() {
		controller.selectArrowTool();
		executeTool(createRandomPoints());
		executeTool(createRandomPoints());

		controller.selectPenTool();
		executeTool(createRandomPoints());

		assertEquals(3, getShapes().size());

		controller.selectDeleteAllTool();

		assertEquals(0, getShapes().size());
	}

	@Test
	void testEllipseTool() {
		controller.selectEllipseTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof EllipseShape);
	}

	@Test
	void testExtendViewTool() {
		controller.selectExtendViewTool();

		PresentationParameterProvider ppp = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter param = ppp.getParameter(getPage());

		assertEquals(new Rectangle2D(0.0, 0.0, 1.3, 1.3), param.getPageRect());

		controller.selectExtendViewTool();

		assertEquals(new Rectangle2D(0.0, 0.0, 1.0, 1.0), param.getPageRect());
	}

	@Test
	void testGroupSelectionTool() {
		controller.selectArrowTool();
		executeTool(createRandomPoints());
		executeTool(createRandomPoints());

		LinkedList<PenPoint2D> penPoints = createRandomPoints();
		controller.selectPenTool();
		executeTool(penPoints);

		LinkedList<PenPoint2D> selectPoints = createRandomPoints();
		selectPoints.getFirst().set(0, 0);
		selectPoints.getLast().set(1000, 1000);

		controller.selectGroupSelectionTool();
		executeTool(selectPoints);

		for (Shape shape : getShapes()) {
			assertTrue(shape.isSelected());
		}

		LinkedList<PenPoint2D> movePoints = createRandomPoints();
		movePoints.set(0, penPoints.get(5));

		executeTool(movePoints);

		assertNotEquals(penPoints, getShapes().get(2).getPoints());

		executeTool(createOutOfBoundsPoints());

		for (Shape shape : getShapes()) {
			assertFalse(shape.isSelected());
		}
	}

	@Test
	void testHighlighterTool() {
		controller.selectHighlighterTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof PenShape);
	}

	@Test
	void testLatexTool() {
		controller.selectLatexTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof TeXShape);
	}

	@Test
	void testLineTool() {
		controller.selectLineTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof LineShape);
	}

	@Test
	void testPanningTool() {
		assertThrows(IllegalStateException.class, () -> {
			controller.selectPanningTool();
		});

		Rectangle2D pageBounds = getPage().getPageRect();

		double zw = pageBounds.getWidth() / 2;
		double zh = pageBounds.getHeight() / 2;

		controller.selectZoomTool();
		controller.beginToolAction(new PenPoint2D(0, 0));
		controller.executeToolAction(new PenPoint2D(zw, zh));
		controller.endToolAction(new PenPoint2D(zw, zh));

		PresentationParameterProvider ppp = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter param = ppp.getParameter(getPage());

		assertEquals(0, getShapes().size());
		assertEquals(new Rectangle2D(0, 0, zw, zh), param.getPageRect());

		LinkedList<PenPoint2D> panPoints = createRandomPoints();
		controller.selectPanningTool();
		executeTool(panPoints);

		Point2D location = panPoints.getFirst().subtract(panPoints.getLast());

		assertEquals(new Rectangle2D(location.getX(), location.getY(), zw, zh), param.getPageRect());
	}

	@Test
	void testPenTool() {
		controller.selectPenTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof PenShape);
	}

	@Test
	void testPointerTool() {
		controller.selectPointerTool();

		executeTool(createRandomPoints());

		assertEquals(0, getShapes().size());

		controller.beginToolAction(new PenPoint2D(0, 0));
		controller.executeToolAction(new PenPoint2D(10, 10));

		assertTrue(getShapes().get(0) instanceof PointerShape);

		controller.endToolAction(new PenPoint2D(20, 10));
	}

	@Test
	void testSelectPreviousTool() {
		controller.selectPointerTool();
		controller.selectLineTool();
		controller.selectEllipseTool();
		controller.selectPreviousTool();

		executeTool(createRandomPoints());

		assertTrue(getShapes().get(0) instanceof LineShape);

		controller.selectLineTool();
		controller.undo();
		assertEquals(ToolType.LINE, controller.getSelectedToolType());

		controller.selectHighlighterTool();
		controller.redo();
		assertEquals(ToolType.HIGHLIGHTER, controller.getSelectedToolType());

		controller.selectZoomTool();
		controller.selectPreviousTool();
		assertEquals(ToolType.HIGHLIGHTER, controller.getSelectedToolType());

		controller.selectLineTool();
		controller.selectZoomTool();
		controller.selectZoomOutTool();
		controller.selectPreviousTool();
		assertEquals(ToolType.LINE, controller.getSelectedToolType());

		controller.selectZoomTool();
		executeTool(createRandomPoints());
		controller.selectPanningTool();
		controller.selectPreviousTool();
		assertEquals(ToolType.LINE, controller.getSelectedToolType());

		controller.selectPenTool();
		controller.selectRubberTool();
		controller.selectPreviousTool();
		assertEquals(ToolType.PEN, controller.getSelectedToolType());

		controller.selectRectangleTool();
		controller.selectDeleteAllTool();
		assertEquals(ToolType.RECTANGLE, controller.getSelectedToolType());

		controller.selectExtendViewTool();
		assertEquals(ToolType.RECTANGLE, controller.getSelectedToolType());
	}

	@Test
	void testRectangleTool() {
		controller.selectRectangleTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof RectangleShape);
	}

	@Test
	void testRubberTool() {
		LinkedList<PenPoint2D> arrowPoints = createRandomPoints();
		controller.selectHighlighterTool();
		executeTool(arrowPoints);

		LinkedList<PenPoint2D> penPoints = createRandomPoints();
		controller.selectPenTool();
		executeTool(penPoints);

		assertEquals(2, getShapes().size());

		LinkedList<PenPoint2D> excludePoints = new LinkedList<>(arrowPoints);
		excludePoints.addAll(penPoints);

		LinkedList<PenPoint2D> rubberPoints = createOutOfBoundsPoints();
		controller.selectRubberTool();
		executeTool(rubberPoints);

		assertEquals(2, getShapes().size());

		rubberPoints = createRandomPoints();
		rubberPoints.addAll(excludePoints);

		executeTool(rubberPoints);

		assertEquals(0, getShapes().size());
	}

	@Test
	void testSelectionTool() {
		LinkedList<PenPoint2D> penPoints = createRandomPoints();
		controller.selectPenTool();
		executeTool(penPoints);

		controller.selectSelectionTool();

		LinkedList<PenPoint2D> selectPoints = createRandomPoints(penPoints);
		executeTool(selectPoints);

		assertEquals(penPoints, getShapes().get(0).getPoints());

		selectPoints.addFirst(penPoints.get(5));

		executeTool(selectPoints);

		assertNotEquals(penPoints, getShapes().get(0).getPoints());
	}

	@Test
	void testSelectTool() {
		controller.selectSelectTool();
		assertEquals(ToolType.SELECT, controller.getSelectedToolType());

		controller.selectSelectTool();
		assertEquals(ToolType.SELECT_GROUP, controller.getSelectedToolType());

		controller.selectSelectTool();
		assertEquals(ToolType.CLONE, controller.getSelectedToolType());

		controller.selectHighlighterTool();
		controller.selectPreviousTool();
		assertEquals(ToolType.CLONE, controller.getSelectedToolType());
	}

	@Test
	void testTextSelectionTool() {
		controller.selectTextSelectionTool();

		executeTool(createRandomPoints());

	}

	@Test
	void testTextTool() {
		controller.selectTextTool();

		executeTool(createRandomPoints());

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof TextShape);
	}

	@Test
	void testZoomTool() {
		LinkedList<PenPoint2D> zoomPoints = createRandomPoints();
		controller.selectZoomTool();
		executeTool(zoomPoints);

		PenPoint2D location = zoomPoints.getFirst();

		Rectangle2D pageBounds = getPage().getPageRect();
		Dimension2D size = new Dimension2D(zoomPoints.getLast().getX() - location.getX(), zoomPoints.getLast().getY() - location.getY());
		Dimension2D ratio = new Dimension2D(pageBounds.getWidth(), pageBounds.getHeight());

		size = GeometryUtils.keepAspectRatio(size, ratio);

		Rectangle2D rect = new Rectangle2D();
		rect.setLocation(location.getX(), location.getY());
		rect.setFromDiagonal(location.getX(), location.getY(), location.getX() + size.getWidth(), location.getY() + size.getHeight());

		PresentationParameterProvider ppp = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter param = ppp.getParameter(getPage());

		assertEquals(rect, param.getPageRect());
	}

	@Test
	void testZoomOutTool() {
		PresentationParameterProvider ppp = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter param = ppp.getParameter(getPage());

		Rectangle2D initRect = new Rectangle2D(0, 0, 1, 1);

		controller.selectZoomTool();
		executeTool(createRandomPoints());

		assertNotEquals(initRect, param.getPageRect());

		controller.selectZoomOutTool();

		assertEquals(initRect, param.getPageRect());
	}

	@Test
	void testToggleGrid() {
		PresentationParameterProvider provider = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter param = provider.getParameter(getPage());

		assertFalse(param.showGrid());

		controller.toggleGrid();

		assertTrue(param.showGrid());

		controller.toggleGrid();

		assertFalse(param.showGrid());
	}

	@Test
	void testUndo() {
		controller.selectArrowTool();
		executeTool(createRandomPoints());

		controller.selectPenTool();
		executeTool(createRandomPoints());
		executeTool(createRandomPoints());

		assertEquals(3, getShapes().size());

		controller.undo();

		assertEquals(2, getShapes().size());
		assertTrue(getShapes().get(0) instanceof ArrowShape);
		assertTrue(getShapes().get(1) instanceof PenShape);

		controller.undo();

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof ArrowShape);

		controller.selectHighlighterTool();
		executeTool(createRandomPoints());

		controller.undo();

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof ArrowShape);

		controller.undo();

		assertEquals(0, getShapes().size());
	}

	@Test
	void testRedo() {
		controller.selectArrowTool();
		executeTool(createRandomPoints());

		controller.selectPenTool();
		executeTool(createRandomPoints());
		executeTool(createRandomPoints());

		controller.undo();
		controller.undo();
		controller.undo();

		assertEquals(0, getShapes().size());

		controller.redo();

		assertEquals(1, getShapes().size());
		assertTrue(getShapes().get(0) instanceof PenShape);

		controller.redo();

		assertEquals(2, getShapes().size());
		assertTrue(getShapes().get(0) instanceof PenShape);

		controller.redo();

		assertEquals(3, getShapes().size());
		assertTrue(getShapes().get(0) instanceof ArrowShape);
	}

	private Page getPage() {
		return documentService.getDocuments().getSelectedDocument().getCurrentPage();
	}

	private List<Shape> getShapes() {
		return getPage().getShapes();
	}

	private LinkedList<PenPoint2D> createRandomPoints() {
		LinkedList<PenPoint2D> points = new LinkedList<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		int min = 0;
		int max = 1000;

		for (int i = 0; i < 10; i++) {
			double x = random.nextDouble(min, max);
			double y = random.nextDouble(min, max);

			points.add(new PenPoint2D(x, y));
		}

		return points;
	}

	private LinkedList<PenPoint2D> createRandomPoints(LinkedList<PenPoint2D> exclusion) {
		LinkedList<PenPoint2D> points = new LinkedList<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		int min = 0;
		int max = 1000;

		for (int i = 0; i < 10; i++) {
			double x = random.nextDouble(min, max);
			double y = random.nextDouble(min, max);

			PenPoint2D point = new PenPoint2D(x, y);

			if (!exclusion.contains(point)) {
				points.add(point);
			}
			else {
				i--;
			}
		}

		return points;
	}

	private LinkedList<PenPoint2D> createOutOfBoundsPoints() {
		LinkedList<PenPoint2D> points = new LinkedList<>();
		points.add(new PenPoint2D(-1, -1));
		points.add(new PenPoint2D(-2, -2));
		points.add(new PenPoint2D(-3, -3));

		return points;
	}

	private void executeTool(LinkedList<PenPoint2D> points) {
		controller.beginToolAction(points.getFirst());

		for (int i = 1; i < points.size(); i++) {
			controller.executeToolAction(points.get(i));
		}

		controller.endToolAction(points.getLast());
	}

}
