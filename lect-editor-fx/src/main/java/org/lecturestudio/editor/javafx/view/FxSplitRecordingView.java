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
	private ExtRadioButton endRadio;

	@FXML
	private ToggleGroup partialSaveGroup;

	@FXML
	private Label splitInfoText;

	@FXML
	private Button submitButton;

	@FXML
	private Button closeButton;

	private Interval<Integer> begin;
	private Interval<Integer> end;

	@Inject
	FxSplitRecordingView(ApplicationContext context) {
		super();

		this.context = context;
	}

	@Override
	public void setIntervals(Interval<Integer> begin, Interval<Integer> end) {
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

		beginRadio.setText(getRadioButtonText("recording.split.radio.begin", begin.getStart(), begin.getEnd()));
		beginRadio.getTooltip().setText(getRadioButtonText("recording.split.radio.begin", begin.getStart(), begin.getEnd()));
		endRadio.setText(getRadioButtonText("recording.split.radio.end", end.getStart(), end.getEnd()));
		endRadio.getTooltip().setText(getRadioButtonText("recording.split.radio.end", end.getStart(), end.getEnd()));

		beginRadio.setUserData(begin);
		endRadio.setUserData(end);
	}

	@Override
	public void setOnSubmit(ConsumerAction<Interval<Integer>> action) {
		FxUtils.bindAction(submitButton, () -> action.execute((Interval<Integer>) partialSaveGroup.getSelectedToggle().getUserData()));
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
