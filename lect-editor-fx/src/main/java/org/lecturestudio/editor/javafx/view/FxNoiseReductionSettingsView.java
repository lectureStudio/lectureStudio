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

package org.lecturestudio.editor.javafx.view;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.util.converter.NumberStringConverter;

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.presenter.NoiseReductionSettingsPresenter;
import org.lecturestudio.editor.api.view.NoiseReductionSettingsView;
import org.lecturestudio.javafx.beans.LectBooleanProperty;
import org.lecturestudio.javafx.beans.LectDoubleProperty;
import org.lecturestudio.javafx.control.SpectrogramChart;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.media.audio.Spectrogram;

@FxmlView(name = "noise-reduction-settings", presenter = NoiseReductionSettingsPresenter.class)
public class FxNoiseReductionSettingsView extends ContentPane implements NoiseReductionSettingsView {

	@FXML
	private ResourceBundle resources;

	@FXML
	private TextField sensitivityField;

	@FXML
	private Slider sensitivitySlider;

	@FXML
	private SpectrogramChart spectrogramChart;

	@FXML
	private Button saveProfileSelectionButton;

	@FXML
	private ToggleButton playSnippetButton;

	@FXML
	private Button denoiseTrialButton;

	@FXML
	private Button updateAudioSnippetButton;

	@FXML
	private Button denoiseFinalButton;


	@Override
	public void bindSensitivity(DoubleProperty sensitivity) {
		sensitivitySlider.valueProperty().bindBidirectional(new LectDoubleProperty(sensitivity));
		sensitivityField.textProperty().bindBidirectional(
				sensitivitySlider.valueProperty(), new NumberStringConverter());
	}

	@Override
	public void bindPlayAudioSnippet(BooleanProperty play) {
		playSnippetButton.selectedProperty().bindBidirectional(new LectBooleanProperty(play));
		playSnippetButton.selectedProperty().addListener(o -> {
			String text;

			if (playSnippetButton.isSelected()) {
				text = "noise.reduction.settings.stop.audio.snippet";
			}
			else {
				text = "noise.reduction.settings.play.audio.snippet";
			}

			FxUtils.invoke(() -> {
				playSnippetButton.setText(resources.getString(text));
			});
		});
	}

	@Override
	public void setNoiseReductionEnabled(boolean enabled) {
		FxUtils.invoke(() -> {
			lookupAll("#reduction").forEach(node -> node.setDisable(!enabled));
		});
	}

	@Override
	public void setNoiseReductionRunning(boolean running) {
		FxUtils.invoke(() -> {
			playSnippetButton.setDisable(running);
			denoiseTrialButton.setDisable(running);
			denoiseFinalButton.setDisable(running);
		});
	}

	@Override
	public void setNoiseReductionProgress(double progress) {
		FxUtils.invoke(() -> {

		});
	}

	@Override
	public void setSpectrogram(Spectrogram spectrogram) {
		FxUtils.invoke(() -> {
			spectrogramChart.setSpectrogram(spectrogram);
		});
	}

	@Override
	public void setOnUpdateAudioSnippet(Action action) {
		FxUtils.bindAction(updateAudioSnippetButton, action);
	}

	@Override
	public void setOnSaveProfileSelection(Action action) {
		FxUtils.bindAction(saveProfileSelectionButton, action);
	}

	@Override
	public void setOnTrialDenoise(Action action) {
		FxUtils.bindAction(denoiseTrialButton, action);
	}

	@Override
	public void setOnFinalDenoise(Action action) {
		FxUtils.bindAction(denoiseFinalButton, action);
	}
}
