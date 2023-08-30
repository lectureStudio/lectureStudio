package org.lecturestudio.editor.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.inject.DIViewContextFactory;
import org.lecturestudio.core.inject.GuiceInjector;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.StrokeSettings;
import org.lecturestudio.core.tool.StrokeWidthSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.editor.api.bus.event.EditorToolSelectionEvent;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.view.ToolbarMockView;
import org.lecturestudio.editor.api.view.ToolbarView;

public class ToolbarPresenterTest extends PresenterTest {
	private ToolbarMockView view;
	private RecordingFileService recordingService;
	private ToolbarPresenter presenter;
	private Document newDocument;

	private EditorToolController toolController;
	private ToolType selectedToolType;

	@BeforeEach
	@Override
	void setupInjector() throws ExecutionException, InterruptedException {
		view = new ToolbarMockView();
		injector = new GuiceInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ViewContextFactory.class).to(DIViewContextFactory.class);
			}

			@Provides
			@Singleton
			ApplicationContext provideApplicationContext() {
				return context;
			}

			@Provides
			@Singleton
			AudioSystemProvider provideAudioSystemProvider() {
				return audioSystemProvider;
			}

			@Provides
			@Singleton
			ToolbarView provideToolbarView() {
				return view;
			}
		});

		recordingService = injector.getInstance(RecordingFileService.class);
		String recordingPath = Objects.requireNonNull(getClass().getClassLoader().getResource("empty_pages_recording.presenter")).getFile();
		recordingService.openRecording(new File(recordingPath)).get();

		presenter = injector.getInstance(ToolbarPresenter.class);

		String docPath = Objects.requireNonNull(getClass().getClassLoader().getResource("written_pages.pdf")).getFile();
		newDocument = context.getDocumentService().openDocument(new File(docPath)).get();

		toolController = injector.getInstance(EditorToolController.class);

		presenter.initialize();
		selectedToolType = null;

		context.getEventBus().register(this);
	}

	@AfterEach
	void tearDown() {
		context.getEventBus().unregister(this);
	}


	@Test
	void testCustomPaletteColor() throws InterruptedException {
		testPenTool();
		view.setOnCustomPaletteColor.execute(Color.BLACK);
		assertEquals(Color.BLACK, toolController.getPaintSettings(ToolType.PEN).getColor());
	}

	private void testColor(int index, Action selectColor) throws InterruptedException {
		testPenTool();
		assertEquals(ToolType.PEN, view.selectColorButtonToolType);
		selectColor.execute();
		assertEquals(ColorPalette.getColor(ToolType.PEN, index), toolController.getPaintSettings(ToolType.PEN).getColor());
	}

	@Test
	void testCustomColor() throws InterruptedException {
		testColor(0, view.setOnCustomColor);
	}

	@Test
	void testColor1() throws InterruptedException {
		testColor(1, view.setOnColor1);
	}

	@Test
	void testColor2() throws InterruptedException {
		testColor(2, view.setOnColor2);
	}

	@Test
	void testColor3() throws InterruptedException {
		testColor(3, view.setOnColor3);
	}

	@Test
	void testColor4() throws InterruptedException {
		testColor(4, view.setOnColor4);
	}

	@Test
	void testColor5() throws InterruptedException {
		testColor(5, view.setOnColor5);
	}

	@Test
	void testColor6() throws InterruptedException {
		testColor(6, view.setOnColor6);
	}

	@Test
	void testPenTool() throws InterruptedException {
		view.setOnPenTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.PEN, view.selectToolButtonToolType);
		assertSame(ToolType.PEN, selectedToolType);
	}

	@Test
	void testHighlighterTool() throws InterruptedException {
		view.setOnHighlighterTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.HIGHLIGHTER, selectedToolType);
		assertSame(ToolType.HIGHLIGHTER, view.selectToolButtonToolType);
	}

	@Test
	void testPointerTool() throws InterruptedException {
		view.setOnPointerTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.POINTER, selectedToolType);
		assertSame(ToolType.POINTER, view.selectToolButtonToolType);
	}

	@Test
	void testTextSelectTool() throws InterruptedException {
		view.setOnTextSelectTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.TEXT_SELECTION, selectedToolType);
		assertSame(ToolType.TEXT_SELECTION, view.selectToolButtonToolType);
	}

	@Test
	void testLineTool() throws InterruptedException {
		view.setOnLineTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.LINE, selectedToolType);
		assertSame(ToolType.LINE, view.selectToolButtonToolType);
	}

	@Test
	void testArrowTool() throws InterruptedException {
		view.setOnArrowTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.ARROW, selectedToolType);
		assertSame(ToolType.ARROW, view.selectToolButtonToolType);
	}

	@Test
	void testRectangleTool() throws InterruptedException {
		view.setOnRectangleTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.RECTANGLE, selectedToolType);
		assertSame(ToolType.RECTANGLE, view.selectToolButtonToolType);
	}

	@Test
	void testEllipseTool() throws InterruptedException {
		view.setOnEllipseTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.ELLIPSE, selectedToolType);
		assertSame(ToolType.ELLIPSE, view.selectToolButtonToolType);
	}

	@Test
	void testSelectTool() throws InterruptedException {
		view.setOnSelectTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.SELECT, selectedToolType);
		assertSame(ToolType.SELECT, view.selectToolButtonToolType);
	}

	@Test
	public void testTextTool() throws InterruptedException {
		view.setOnTextTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.TEXT, selectedToolType);
		assertSame(ToolType.TEXT, view.selectToolButtonToolType);
	}

	@Test
	void testEraseTool() throws InterruptedException {
		view.setOnEraseTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.RUBBER, selectedToolType);
		assertSame(ToolType.RUBBER, view.selectToolButtonToolType);
	}

	@Test
	void testClearTool() throws InterruptedException {
		view.setOnClearTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.DELETE_ALL, selectedToolType);
	}

	@Test
	void testZoomTools() throws InterruptedException {
		view.setOnZoomInTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.ZOOM, selectedToolType);
		assertSame(ToolType.ZOOM, view.selectToolButtonToolType);


		Rectangle2D pageBounds = newDocument.getCurrentPage().getPageRect();

		double zw = pageBounds.getWidth() / 2;
		double zh = pageBounds.getHeight() / 2;

		toolController.selectZoomTool();
		toolController.beginToolAction(new PenPoint2D(0, 0));
		toolController.executeToolAction(new PenPoint2D(zw, zh));
		toolController.endToolAction(new PenPoint2D(zw, zh));

		selectedToolType = null;

		view.setOnPanTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.PANNING, selectedToolType);
		assertSame(ToolType.PANNING, view.selectToolButtonToolType);

		selectedToolType = null;

		view.setOnZoomOutTool.execute();
		awaitTrue(() -> selectedToolType != null, 10);
		assertSame(ToolType.ZOOM_OUT, selectedToolType);
	}

	@Test
	void testStrokeWidthSelection() throws InterruptedException {
		testPenTool();

		assertTrue(view.strokeWidthSettings.containsAll(List.of(StrokeWidthSettings.values())));
		assertEquals(view.strokeWidthSettings.size(), StrokeWidthSettings.values().length);

		view.setOnStrokeWidthSettings.execute(StrokeWidthSettings.EXTRA_BIG);

		assertSame(StrokeWidthSettings.EXTRA_BIG, ((StrokeSettings) toolController.getPaintSettings(ToolType.PEN)).getStrokeWidthSettings());
	}

	@Subscribe
	private void onToolSelection(EditorToolSelectionEvent event) {
		if (selectedToolType == null) {
			selectedToolType = event.getToolType();
		}
	}

}
