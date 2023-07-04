package org.lecturestudio.editor.javafx.view;

import javax.inject.Inject;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.SplitRecordingView;
import org.lecturestudio.javafx.control.ExtRadioButton;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "recording-split", presenter = org.lecturestudio.editor.api.presenter.SplitRecordingPresenter.class)
public class FxSplitRecordingView extends StackPane implements SplitRecordingView {

	private final ApplicationContext context;
	@FXML
	private ResourceBundle resources;

	@FXML
	private ExtRadioButton beginRadio;

	@FXML
	private Label beginRadioTimeLabel;

	@FXML
	private ExtRadioButton endRadio;

	@FXML
	private Label endRadioTimeLabel;

	@FXML
	private ToggleGroup partialSaveGroup;

	@FXML
	private Label splitInfoText;

	@FXML
	private Button submitButton;

	@FXML
	private Button closeButton;

	private Interval<Long> begin;
	private Interval<Long> end;

	@Inject
	FxSplitRecordingView(ApplicationContext context) {
		super();

		this.context = context;
	}

	@Override
	public void setIntervals(Interval<Long> begin, Interval<Long> end) {
		this.begin = begin;
		this.end = end;

		if (begin.lengthInt() == 0) {
			beginRadio.setDisable(true);
			endRadio.setSelected(true);
		}
		else if (end.lengthInt() == 0) {
			endRadio.setDisable(true);
			beginRadio.setSelected(true);
		}

		beginRadio.getTooltip().setText(getRadioButtonText("recording.split.radio.begin.tooltip", begin.getStart(), begin.getEnd()));
		beginRadioTimeLabel.setText(getRadioButtonText("recording.split.radio.begin.time", begin.getStart(), begin.getEnd()));
		beginRadioTimeLabel.getTooltip().setText(getRadioButtonText("recording.split.radio.begin.tooltip", begin.getStart(), begin.getEnd()));

		endRadio.getTooltip().setText(getRadioButtonText("recording.split.radio.end.tooltip", end.getStart(), end.getEnd()));
		endRadioTimeLabel.setText(getRadioButtonText("recording.split.radio.end.time", end.getStart(), end.getEnd()));
		endRadioTimeLabel.getTooltip().setText(getRadioButtonText("recording.split.radio.end.tooltip", end.getStart(), end.getEnd()));

		beginRadio.setUserData(begin);
		endRadio.setUserData(end);
	}

	@Override
	public void setOnSubmit(ConsumerAction<Interval<Long>> action) {
		FxUtils.bindAction(submitButton, () -> action.execute((Interval<Long>) partialSaveGroup.getSelectedToggle().getUserData()));
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	private String getRadioButtonText(String message, long beginTimestamp, long endTimestamp) {
		return MessageFormat.format(resources.getString(message),
				new Time(beginTimestamp).toString(), new Time(endTimestamp).toString());
	}
}
