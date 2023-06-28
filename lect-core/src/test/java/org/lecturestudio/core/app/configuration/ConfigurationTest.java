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

package org.lecturestudio.core.app.configuration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lecturestudio.core.CoreTest;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.io.file.visitor.DeleteDirVisitor;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.text.TextAttributes;
import org.lecturestudio.core.tool.PresetColor;

class ConfigurationTest extends CoreTest {

	private final JsonConfigurationService<Configuration> manager = new JsonConfigurationService<>();

	private Path resourcesPath;

	private File configFile;


	@BeforeEach
	void setupPresenterTest() throws URISyntaxException {
		Path root = getResourcePath(".");
		resourcesPath = root.resolve("AppData");
		configFile = resourcesPath.resolve("config.json").toFile();
	}

	@AfterEach
	void deleteOutputFile() throws IOException {
		Files.walkFileTree(resourcesPath, new DeleteDirVisitor());
	}

	@Test
	final void testConfig() throws IOException {
		List<RecentDocument> recentDocuments = new ArrayList<>();
		recentDocuments.add(new RecentDocument("Test Doc", "/home/var", Calendar.getInstance().getTime()));
		recentDocuments.add(new RecentDocument("Temp Doc", "/tmp", Calendar.getInstance().getTime()));

		Configuration config = new Configuration();
		config.setLocale(Locale.GERMANY);
		config.setExtendPageDimension(new Dimension2D(6, 4));
		config.getRecentDocuments().add(recentDocuments.get(0));
		config.getRecentDocuments().add(recentDocuments.get(1));
		config.setStartMaximized(true);
		config.setTabletMode(false);

		manager.save(configFile, config);

		Configuration loadedConfig = manager.load(configFile, Configuration.class);

		assertEquals(Locale.GERMANY, loadedConfig.getLocale());
		assertEquals(new Dimension2D(6, 4), loadedConfig.getExtendPageDimension());
		assertEquals(true, loadedConfig.getStartMaximized());
		assertEquals(false, loadedConfig.getTabletMode());
		assertTrue(Arrays.deepEquals(recentDocuments.toArray(), loadedConfig.getRecentDocuments().toArray()));
	}

	@Test
	final void testWhiteboardConfig() throws IOException {
		Configuration config = new Configuration();
		config.getWhiteboardConfig().setBackgroundColor(new Color(120, 90, 60));
		config.getWhiteboardConfig().setVerticalLinesVisible(false);
		config.getWhiteboardConfig().setVerticalLinesInterval(10);
		config.getWhiteboardConfig().setHorizontalLinesVisible(true);
		config.getWhiteboardConfig().setHorizontalLinesInterval(15);
		config.getWhiteboardConfig().setGridColor(Color.BLACK);

		manager.save(configFile, config);

		Configuration loadedConfig = manager.load(configFile, Configuration.class);
		WhiteboardConfiguration whiteboardConfig = loadedConfig.getWhiteboardConfig();

		assertEquals(new Color(120, 90, 60), whiteboardConfig.getBackgroundColor());
		assertEquals(false, whiteboardConfig.getVerticalLinesVisible());
		assertEquals(10, whiteboardConfig.getVerticalLinesInterval().intValue());
		assertEquals(true, whiteboardConfig.getHorizontalLinesVisible());
		assertEquals(15, whiteboardConfig.getHorizontalLinesInterval().intValue());
		assertEquals(Color.BLACK, whiteboardConfig.getGridColor());
	}

	@Test
	final void testDisplayConfig() throws IOException {
		Configuration config = new Configuration();
		config.getDisplayConfig().setAutostart(false);
		config.getDisplayConfig().setBackgroundColor(new Color(10, 10, 20));
		config.getDisplayConfig().setIpPosition(Position.BOTTOM_CENTER);

		manager.save(configFile, config);

		Configuration loadedConfig = manager.load(configFile, Configuration.class);
		DisplayConfiguration displayConfig = loadedConfig.getDisplayConfig();

		assertEquals(false, displayConfig.getAutostart());
		assertEquals(new Color(10, 10, 20), displayConfig.getBackgroundColor());
		assertEquals(Position.BOTTOM_CENTER, displayConfig.getIpPosition());
	}

	@Test
	final void testToolConfig() throws IOException {
		Configuration config = new Configuration();
		ToolConfiguration toolConfig = config.getToolConfig();

		toolConfig.getPenSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getPenSettings().setWidth(0.02);
		toolConfig.getHighlighterSettings().setColor(PresetColor.ORANGE.getColor());
		toolConfig.getHighlighterSettings().setAlpha(140);
		toolConfig.getHighlighterSettings().setWidth(0.07);
		toolConfig.getHighlighterSettings().setScale(true);
		toolConfig.getPointerSettings().setColor(PresetColor.RED.getColor());
		toolConfig.getPointerSettings().setAlpha(170);
		toolConfig.getPointerSettings().setWidth(0.05);
		toolConfig.getArrowSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getArrowSettings().setWidth(0.02);
		toolConfig.getLineSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getLineSettings().setWidth(0.02);
		toolConfig.getRectangleSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getRectangleSettings().setWidth(0.02);
		toolConfig.getEllipseSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getEllipseSettings().setWidth(0.02);
		toolConfig.getTextSelectionSettings().setColor(PresetColor.ORANGE.getColor());
		toolConfig.getTextSelectionSettings().setAlpha(140);
		toolConfig.getTextSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getTextSettings().setFont(new Font("Arial", 24));
		toolConfig.getTextSettings().setTextAttributes(new TextAttributes());
		toolConfig.getLatexSettings().setColor(PresetColor.BLACK.getColor());
		toolConfig.getLatexSettings().setFont(new TeXFont(TeXFont.Type.SERIF, 20));

		config.getToolConfig().getPresetColors().addAll(Arrays.asList(Color.BLACK, Color.WHITE));

		manager.save(configFile, config);

		Configuration loadedConfig = manager.load(configFile, Configuration.class);
		toolConfig = loadedConfig.getToolConfig();

		assertEquals(0, Double.compare(0.07, toolConfig.getHighlighterSettings().getWidth()));
		assertEquals(0, Double.compare(0.02, toolConfig.getPenSettings().getWidth()));
		assertEquals(0, Double.compare(0.05, toolConfig.getPointerSettings().getWidth()));
		assertEquals(true, toolConfig.getHighlighterSettings().getScale());
		assertEquals(140, toolConfig.getHighlighterSettings().getAlpha());
		assertEquals(170, toolConfig.getPointerSettings().getAlpha());
		assertArrayEquals(new Color[] { Color.BLACK, Color.WHITE }, toolConfig.getPresetColors().toArray());
	}

	@Test
	final void testAudioConfig() throws IOException {
		Configuration config = new Configuration();
		config.getAudioConfig().setCaptureDeviceName("Integrated Microphone");
		config.getAudioConfig().setPlaybackDeviceName("Speakers");
		config.getAudioConfig().setRecordingFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		config.getAudioConfig().setRecordingPath("/home/tmp");
		config.getAudioConfig().setRecordingVolume("Microphone", 0.7f);

		manager.save(configFile, config);

		Configuration loadedConfig = manager.load(configFile, Configuration.class);
		AudioConfiguration audioConfig = loadedConfig.getAudioConfig();

		assertEquals("Integrated Microphone", audioConfig.getCaptureDeviceName());
		assertEquals("Speakers", audioConfig.getPlaybackDeviceName());
		assertEquals(audioConfig.getRecordingFormat(), new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		assertEquals("/home/tmp", audioConfig.getRecordingPath());
		assertEquals(Double.valueOf(0.7f), audioConfig.getRecordingVolume("Microphone"));
	}

	@Override
	protected ApplicationContext getApplicationContext() {
		return null;
	}
}
