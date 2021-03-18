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

package org.lecturestudio.editor.api.context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.lecturestudio.core.app.AppDataLocator;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.configuration.ConfigurationService;
import org.lecturestudio.core.app.configuration.JsonConfigurationService;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.media.config.RenderConfiguration;

public class EditorContext extends ApplicationContext {

	private final BooleanProperty canCut = new BooleanProperty();

	private final BooleanProperty canDeletePage = new BooleanProperty();

	private final BooleanProperty canUndo = new BooleanProperty();

	private final BooleanProperty canRedo = new BooleanProperty();

	private final BooleanProperty seeking = new BooleanProperty();

	private final DoubleProperty primarySelection = new DoubleProperty();

	private final DoubleProperty leftSelection = new DoubleProperty();

	private final DoubleProperty rightSelection = new DoubleProperty();

	private final DoubleProperty trackZoomLevel = new DoubleProperty(1);

	private final RenderConfiguration renderConfig = new RenderConfiguration();

	private final File configFile;

	private final File tempDir;


	public EditorContext(AppDataLocator dataLocator, File configFile, Configuration config, Dictionary dict, EventBus eventBus, EventBus audioBus)
			throws IOException {
		super(dataLocator, config, dict, eventBus, audioBus);

		this.configFile = configFile;
		this.tempDir = Files.createTempDirectory("lectEditor").toFile();

		leftSelectionProperty().addListener((o, oldValue, newValue) -> {
			setCanCut(!newValue.equals(getRightSelection()));
		});
		rightSelectionProperty().addListener((o, oldValue, newValue) -> {
			setCanCut(!newValue.equals(getLeftSelection()));
		});
	}

	@Override
	public void saveConfiguration() throws Exception {
		ConfigurationService<Configuration> configService = new JsonConfigurationService<>();
		configService.save(configFile, getConfiguration());
	}

	public RenderConfiguration getRenderConfiguration() {
		return renderConfig;
	}

	public void setCanCut(boolean enable) {
		canCut.set(enable);
	}

	public BooleanProperty canCutProperty() {
		return canCut;
	}

	public void setCanDeletePage(boolean enable) {
		canDeletePage.set(enable);
	}

	public BooleanProperty canDeletePageProperty() {
		return canDeletePage;
	}

	public void setCanUndo(boolean enable) {
		canUndo.set(enable);
	}

	public BooleanProperty canUndoProperty() {
		return canUndo;
	}

	public void setCanRedo(boolean enable) {
		canRedo.set(enable);
	}

	public BooleanProperty canRedoProperty() {
		return canRedo;
	}

	public boolean isSeeking() {
		return seeking.get();
	}

	public void setSeeking(boolean enable) {
		seeking.set(enable);
	}

	public BooleanProperty seekingProperty() {
		return seeking;
	}

	public double getPrimarySelection() {
		return primarySelection.get();
	}

	public void setPrimarySelection(double value) {
		primarySelection.set(value);
	}

	public DoubleProperty primarySelectionProperty() {
		return primarySelection;
	}

	public double getLeftSelection() {
		return leftSelection.get();
	}

	public void setLeftSelection(double value) {
		leftSelection.set(value);
	}

	public DoubleProperty leftSelectionProperty() {
		return leftSelection;
	}

	public double getRightSelection() {
		return rightSelection.get();
	}

	public void setRightSelection(double value) {
		rightSelection.set(value);
	}

	public DoubleProperty rightSelectionProperty() {
		return rightSelection;
	}

	public double getTrackZoomLevel() {
		return trackZoomLevel.get();
	}

	public void setTrackZoomLevel(double value) {
		trackZoomLevel.set(value);
	}

	public DoubleProperty trackZoomLevelProperty() {
		return trackZoomLevel;
	}

	public File getTempDirectory() {
		return tempDir;
	}

}
