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

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "restore-recording")
public class SwingRestoreRecordingView extends NotificationPane implements RestoreRecordingView {

	private final ResourceBundle resources;

	private JProgressBar progressIndicator;

	private JButton discardButton;

	private JButton saveButton;


	@Inject
	SwingRestoreRecordingView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setSavePath(String path) {
		SwingUtils.invoke(() -> setMessage(path));
	}

	@Override
	public void setError(String message) {
		SwingUtils.invoke(() -> {
			setType(NotificationType.ERROR);
			setTitle(resources.getString("restore.recording.write.error"));
			setMessage(message);
		});
	}

	@Override
	public void setSuccess() {
		SwingUtils.invoke(() -> {
			setTitle(resources.getString("restore.recording.write.success"));

			clearButtons();
		});
	}

	@Override
	public void setProgress(double progress) {
		SwingUtils.invoke(() -> {
			progressIndicator.setValue((int) (progress * 100));
		});
	}

	@Override
	public void setOnDiscard(Action action) {
		SwingUtils.bindAction(discardButton, action);
	}

	@Override
	public void setOnSave(Action action) {
		SwingUtils.bindAction(saveButton, action);
	}

	@Override
	public void showProgress() {
		SwingUtils.invoke(() -> {
			setTitle(resources.getString("restore.recording.write"));

			progressIndicator.setVisible(true);
		});
	}

	@Override
	public void showWarning() {
		SwingUtils.invoke(() -> {
			setType(NotificationType.WARNING);
			setMessage(resources.getString("restore.recording.discard.warning"));
		});
	}

	@ViewPostConstruct
	private void initialize() {
		setType(NotificationType.QUESTION);
		setTitle(resources.getString("restore.recording.save"));
		setMessage(resources.getString("restore.recording.question"));

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				saveButton.requestFocus();
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
