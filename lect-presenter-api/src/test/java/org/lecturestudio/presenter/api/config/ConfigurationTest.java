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

package org.lecturestudio.presenter.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.io.file.visitor.DeleteDirVisitor;
import org.lecturestudio.web.api.filter.RegexFilter;
import org.lecturestudio.web.api.filter.RegexRule;

class ConfigurationTest {

	private final JsonConfigurationService<PresenterConfiguration> manager = new PresenterConfigService();

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
		PresenterConfiguration config = new PresenterConfiguration();
		config.setSaveDocumentOnClose(true);

		manager.save(configFile, config);

		PresenterConfiguration loadedConfig = manager.load(configFile, PresenterConfiguration.class);

		assertEquals(true, loadedConfig.getSaveDocumentOnClose());
	}

	@Test
	final void testStreamConfig() throws IOException {
		PresenterConfiguration config = new PresenterConfiguration();
		config.getStreamConfig().setAudioCodec("Opus");
		config.getStreamConfig().setAudioFormat(new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		config.getStreamConfig().setCameraName("Logitech");
		config.getStreamConfig().setCameraFormat(new CameraFormat(1280, 720, 25));
		config.getStreamConfig().getCameraCodecConfig().setBitRate(128);
		config.getStreamConfig().getCameraCodecConfig().setFrameRate(30);
		config.getStreamConfig().getCameraCodecConfig().setPreset("high");
		config.getStreamConfig().getCameraCodecConfig().setViewRect(new Rectangle2D(20, 20, 640, 480));

		manager.save(configFile, config);

		PresenterConfiguration loadedConfig = manager.load(configFile, PresenterConfiguration.class);
		StreamConfiguration streamConfig = loadedConfig.getStreamConfig();

		assertEquals("Opus", streamConfig.getAudioCodec());
		assertEquals(streamConfig.getAudioFormat(), new AudioFormat(AudioFormat.Encoding.S16LE, 44100, 1));
		assertEquals("Logitech", streamConfig.getCameraName());
		assertEquals(new CameraFormat(1280, 720, 25), streamConfig.getCameraFormat());
		assertEquals(128, streamConfig.getCameraCodecConfig().getBitRate());
		assertEquals(0, Double.compare(30, streamConfig.getCameraCodecConfig().getFrameRate()));
		assertEquals("high", streamConfig.getCameraCodecConfig().getPreset());
		assertEquals(new Rectangle2D(20, 20, 640, 480), streamConfig.getCameraCodecConfig().getViewRect());
	}

	@Test
	final void testQuizConfig() throws IOException {
		RegexFilter filter = new RegexFilter();
		filter.registerRule(new RegexRule("##"));
		filter.registerRule(new RegexRule("(.*)"));

		PresenterConfiguration config = new PresenterConfiguration();
		config.getQuizConfig().setInputFilter(filter);

		manager.save(configFile, config);

		PresenterConfiguration loadedConfig = manager.load(configFile, PresenterConfiguration.class);
		RegexFilter loadedFilter = loadedConfig.getQuizConfig().getInputFilter();

		assertTrue(Arrays.deepEquals(filter.getRules().toArray(), loadedFilter.getRules().toArray()));
	}

	Path getResourcePath(String path) throws URISyntaxException {
		return Path.of(Objects.requireNonNull(
				getClass().getClassLoader().getResource(path)).toURI());
	}
}
