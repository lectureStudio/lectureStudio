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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.view.SaveRecordingView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "save-recording")
public class SwingSaveRecordingView extends NotificationPane implements SaveRecordingView {

	private final ResourceBundle resources;

	private JProgressBar progressIndicator;

	private JButton closeButton;

	private Action closeAction;

	private Action viewShownAction;


	@Inject
	SwingSaveRecordingView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setDestinationFile(File file) {
		SwingUtils.invoke(() -> setMessage(file.getPath()));
	}

	@Override
	public void setError(String message) {
		SwingUtils.invoke(() -> {
			setType(NotificationType.ERROR);
			setTitle(resources.getString("recording.save.error"));
			setMessage(message);
		});
	}

	@Override
	public void setSuccess() {
		SwingUtils.invoke(() -> {
			setTitle(resources.getString("recording.save.success"));
		});
	}

	@Override
	public void setProgress(double progress) {
		SwingUtils.invoke(() -> {
			progressIndicator.setValue((int) (progress * 100));

			if (progressIndicator.getValue() >= progressIndicator.getMaximum()) {
				progressIndicator.setIndeterminate(false);

				closeButton.setEnabled(true);
				closeButton.requestFocusInWindow();
			}
		});
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);

		if (nonNull(closeAction)) {
			SwingUtils.bindAction(closeButton, closeAction);
		}
	}

	@Override
	public void setOnViewShown(Action action) {
		viewShownAction = Action.concatenate(viewShownAction, action);
	}

	@ViewPostConstruct
	private void initialize() {
		setTitle(resources.getString("recording.save"));

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				SwingUtilities.invokeLater(() -> executeAction(viewShownAction));
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {

			}

			@Override
			public void ancestorMoved(AncestorEvent event) {

			}
		});
	}
}
