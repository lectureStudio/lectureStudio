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

package org.lecturestudio.javafx.control;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Pair;

import org.lecturestudio.core.app.LocaleProvider;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.javafx.util.FxUtils;

public class MediaControlsSkin extends SkinBase<MediaControls> {

	private ResourceBundle dict;

	private Slider timeSlider;
	private Slider volumeSlider;

	private HBox buttonContainer;
	private HBox volumeContainer;
	private VBox stateContainer;

	private FadeTransition fadeTransition;

	private HideTimer fadeTimer;


	protected MediaControlsSkin(MediaControls control) {
		super(control);

		LocaleProvider localeProvider = new LocaleProvider();

		try {
			Locale locale = localeProvider.getBestSupported(Locale.getDefault());
			dict = ResourceBundle.getBundle("resources.i18n.media-controls",
					locale,
					getClass().getClassLoader());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		initLayout(control);
	}

	@Override
	protected void layoutChildren(final double contentX, final double contentY,
								  final double contentWidth,
								  final double contentHeight) {
		layoutInArea(timeSlider, contentX, contentY, contentWidth,
					 contentHeight, -1, HPos.CENTER, VPos.TOP);

		final double sliderH = timeSlider.prefHeight(contentWidth);
		final double controlsY = snapPositionY(contentY + sliderH);

		layoutInArea(volumeContainer, contentX, controlsY, contentWidth,
					 contentHeight - sliderH, -1, HPos.LEFT, VPos.CENTER);

		layoutInArea(buttonContainer, contentX, controlsY, contentWidth,
					 contentHeight - sliderH, -1, HPos.CENTER, VPos.CENTER);

		layoutInArea(stateContainer, contentX, controlsY, contentWidth,
					 contentHeight, -1, HPos.RIGHT, VPos.TOP);
	}

	private void initLayout(MediaControls control) {
		timeSlider = new Slider();
		timeSlider.getStyleClass().add("time-slider");
		timeSlider.setMin(0);
		timeSlider.setMax(1);

		SvgIcon prevIcon = new SvgIcon();
		SvgIcon nextIcon = new SvgIcon();
		SvgIcon playIcon = new SvgIcon();

		prevIcon.getStyleClass().add("prev-icon");
		nextIcon.getStyleClass().add("next-icon");
		playIcon.getStyleClass().add("play-icon");

		Button prevButton = new Button();
		Button nextButton = new Button();
		ToggleButton playButton = new ToggleButton();

		prevButton.getStyleClass().add("prev");
		nextButton.getStyleClass().add("next");
		playButton.getStyleClass().add("play");

		prevButton.setGraphic(prevIcon);
		nextButton.setGraphic(nextIcon);
		playButton.setGraphic(playIcon);

		prevButton.setTooltip(new Tooltip(dict.getString("media.controls.previous")));
		nextButton.setTooltip(new Tooltip(dict.getString("media.controls.next")));
		playButton.setTooltip(new Tooltip(dict.getString("media.controls.play")));

		playButton.selectedProperty().addListener(o -> {
			Platform.runLater(() -> {
				Tooltip tooltip = playButton.getTooltip();

				if (playButton.isSelected()) {
					tooltip.setText(dict.getString("media.controls.pause"));
				}
				else {
					tooltip.setText(dict.getString("media.controls.play"));
				}
			});
		});

		buttonContainer = new HBox();
		buttonContainer.getStyleClass().add("media-buttons");
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		buttonContainer.getChildren().addAll(prevButton, playButton, nextButton);

		Label timeLabel = new Label("- / -");
		Label pageLabel = new Label("- / -");

		timeLabel.getStyleClass().add("time");
		pageLabel.getStyleClass().add("page");

		pageLabel.setAlignment(Pos.CENTER_RIGHT);
		pageLabel.setMaxWidth(Double.MAX_VALUE);

		stateContainer = new VBox(timeLabel, pageLabel);
		stateContainer.getStyleClass().add("state-labels");
		stateContainer.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);

		fadeTransition = new FadeTransition(Duration.millis(300));

		SvgIcon volumeIcon = new SvgIcon();
		volumeIcon.getStyleClass().add("volume-icon");

		ToggleButton volumeButton = new ToggleButton();
		volumeButton.getStyleClass().add("volume-button");
		volumeButton.setTooltip(new Tooltip(dict.getString("media.controls.mute")));
		volumeButton.setGraphic(volumeIcon);
		volumeButton.setOnMouseEntered(event -> {
			stopVolumeHideTimer();

			if (!volumeSlider.isManaged()) {
				showVolumeSlider();
			}
		});
		volumeButton.setOnMouseExited(event -> {
			startVolumeHideTimer();
		});

		volumeSlider = new Slider();
		volumeSlider.getStyleClass().add("volume-slider");
		volumeSlider.setMin(0);
		volumeSlider.setMax(1);
		volumeSlider.valueProperty().addListener((o, oldValue, newValue) -> {
			String iconClass = getVolumeIconClass(newValue.doubleValue());

			volumeIcon.getStyleClass().removeIf(s -> s.startsWith("volume-"));
			volumeIcon.getStyleClass().add(iconClass);
		});
		volumeSlider.setValue(1);
		volumeSlider.setManaged(false);
		volumeSlider.setVisible(false);
		volumeSlider.setOnMouseEntered(event -> {
			stopVolumeHideTimer();
		});
		volumeSlider.setOnMouseExited(event -> {
			startVolumeHideTimer();
		});

		fadeTransition.setNode(volumeSlider);

		volumeContainer = new HBox();
		volumeContainer.getStyleClass().add("volume-controls");
		volumeContainer.setAlignment(Pos.CENTER);
		volumeContainer.setMaxSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		volumeContainer.getChildren().addAll(volumeButton, volumeSlider);

		getChildren().addAll(timeSlider, buttonContainer, volumeContainer, stateContainer);

		setTime(timeLabel, control.durationProperty().get());
		setPages(pageLabel, control.pagesProperty().get());

		timeSlider.valueProperty().addListener(o -> {
			setTime(timeLabel, control.durationProperty().get());
		});
		timeSlider.valueProperty().bindBidirectional(control.timeProperty());
		timeSlider.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			EventHandler<ActionEvent> handler = control.onSeekActionProperty().get();

			if (nonNull(handler)) {
				handler.handle(new ActionEvent(timeSlider, Event.NULL_SOURCE_TARGET));
			}
		});

		volumeSlider.valueProperty().bindBidirectional(control.volumeProperty());

		prevButton.onActionProperty().bind(control.onPrevActionProperty());
		nextButton.onActionProperty().bind(control.onNextActionProperty());
		playButton.selectedProperty().bindBidirectional(control.playingProperty());
		volumeButton.selectedProperty().bindBidirectional(control.muteProperty());

		control.durationProperty().addListener((o, oldValue, newValue) -> {
			setTime(timeLabel, newValue);
		});
		control.pagesProperty().addListener((o, oldValue, newValue) -> {
			setPages(pageLabel, newValue);
		});
	}

	private void setTime(Label label, Time total) {
		if (isNull(total)) {
			return;
		}

		final Time current = new Time((long) (total.getMillis() * timeSlider.getValue()));

		FxUtils.invoke(() -> label.setText(current + " / " + total));
	}

	private void setPages(Label label, Pair<Integer, Integer> pageData) {
		if (isNull(pageData)) {
			return;
		}

		FxUtils.invoke(() -> label.setText(pageData.getKey() + " / " + pageData.getValue()));
	}

	private String getVolumeIconClass(double value) {
		if (value == 0) {
			return "volume-mute-icon";
		}
		if (value > 0 && value <= 0.32) {
			return "volume-low-icon";
		}
		if (value > 0.15 && value <= 0.65) {
			return "volume-medium-icon";
		}
		return "volume-high-icon";
	}

	private void showVolumeSlider() {
		volumeSlider.setManaged(true);
		volumeSlider.setVisible(true);

		fadeTransition.setFromValue(0);
		fadeTransition.setToValue(1);
		fadeTransition.play();
	}

	private void hideVolumeSlider() {
		fadeTransition.setOnFinished(event -> {
			fadeTransition.setOnFinished(null);

			volumeSlider.setManaged(false);
			volumeSlider.setVisible(false);
		});
		fadeTransition.setFromValue(1);
		fadeTransition.setToValue(0);
		fadeTransition.play();
	}

	private void startVolumeHideTimer() {
		if (nonNull(fadeTimer) && fadeTimer.hasRunningTask()) {
			fadeTimer.resetTime();
		}
		else {
			fadeTimer = new HideTimer(this::hideVolumeSlider);
			fadeTimer.runIdleTask();
		}
	}

	private void stopVolumeHideTimer() {
		if (nonNull(fadeTimer)) {
			fadeTimer.stop();
		}
	}



	private static class HideTimer extends Timer {

		private final Runnable runnable;

		private TimerTask idleTask;

		private long taskEnd = 0;


		HideTimer(Runnable runnable) {
			this.runnable = runnable;
		}

		boolean hasRunningTask() {
			if (idleTask == null) {
				return false;
			}

			return (System.currentTimeMillis() - idleTask.scheduledExecutionTime()) < 0;
		}

		void resetTime() {
			taskEnd = System.currentTimeMillis() + 3000;
		}

		void runIdleTask() {
			idleTask = new TimerTask() {
				@Override
				public void run() {
					if (System.currentTimeMillis() >= taskEnd) {
						stop();

						runnable.run();
					}
				}
			};

			resetTime();

			scheduleAtFixedRate(idleTask, 0, 1000);
		}

		void stop() {
			cancel();
			purge();

			idleTask = null;
		}
	}
}
