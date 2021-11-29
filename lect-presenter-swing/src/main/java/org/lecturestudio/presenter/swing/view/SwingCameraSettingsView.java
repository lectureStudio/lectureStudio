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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.AspectRatio;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.converter.CameraFormatConverter;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.CameraSettingsView;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.components.CameraPreviewPanel;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "camera-settings", presenter = org.lecturestudio.presenter.api.presenter.CameraSettingsPresenter.class)
public class SwingCameraSettingsView extends JPanel implements CameraSettingsView {

	private JComboBox<String> camerasCombo;

	private JComboBox<AspectRatio> cameraFormatsCombo;

	private JComboBox<CameraFormat> cameraResolutionsCombo;

	private CameraPreviewPanel cameraView;

	private JButton closeButton;

	private JButton resetButton;

	private ConsumerAction<Boolean> viewVisibleAction;


	SwingCameraSettingsView() {
		super();

		initialize();
	}

	@Override
	public void setCameraName(StringProperty cameraName) {
		SwingUtils.bindBidirectional(camerasCombo, cameraName);
	}

	@Override
	public void setCameraNames(String[] cameraNames) {
		if (isNull(cameraNames)) {
			return;
		}

		SwingUtils.invoke(() -> camerasCombo
				.setModel(new DefaultComboBoxModel<>(cameraNames)));
	}

	@Override
	public void setCameraFormats(CameraFormat[] cameraFormats) {
		SwingUtils.invoke(() -> cameraResolutionsCombo
				.setModel(new DefaultComboBoxModel<>(cameraFormats)));
	}

	@Override
	public void setCameraViewRect(ObjectProperty<Rectangle2D> viewRect) {
		Converter<Rectangle2D, CameraFormat> converter = new CameraFormatRectConverter(viewRect);
		ConvertibleObjectProperty<Rectangle2D, CameraFormat> property = new ConvertibleObjectProperty<>(viewRect, converter);

		SwingUtils.bindBidirectional(cameraResolutionsCombo, property);

//		cameraView.setListener(e -> viewRect.set(cameraView.getCaptureRect()));
//		viewRect.addListener((observable, oldValue, newValue) -> cameraView
//				.setCaptureRect(newValue));
	}

	@Override
	public void setCameraAspectRatio(AspectRatio ratio) {
		SwingUtils.invoke(() -> {
			cameraFormatsCombo.getModel().setSelectedItem(ratio);
		});
	}

	@Override
	public void setCameraAspectRatios(AspectRatio[] ratios) {
		SwingUtils.invoke(() -> cameraFormatsCombo
				.setModel(new DefaultComboBoxModel<>(ratios)));
	}

	@Override
	public void setOnCameraAspectRatioChanged(ConsumerAction<AspectRatio> action) {
		cameraFormatsCombo.addItemListener(e -> {
			int stateChange = e.getStateChange();

			if (stateChange == ItemEvent.SELECTED) {
				executeAction(action, cameraFormatsCombo.getModel()
						.getElementAt(cameraFormatsCombo.getSelectedIndex()));
			}
		});
	}

	@Override
	public void setOnViewVisible(ConsumerAction<Boolean> action) {
		this.viewVisibleAction = action;
	}

	@Override
	public void setCamera(Camera camera) {
		cameraView.setCamera(camera);
	}

	@Override
	public void setCameraFormat(CameraFormat cameraFormat) {
		cameraView.setCameraFormat(cameraFormat);
	}

	@Override
	public void setCameraError(String errorMessage) {
		SwingUtils.invoke(() -> {
			cameraView.setStatusMessage(errorMessage);
		});
	}

	@Override
	public void startCameraPreview() {
		cameraView.startCapture();
	}

	@Override
	public void stopCameraPreview() {
		cameraView.stopCapture();
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	private void initialize() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				executeAction(viewVisibleAction, true);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				executeAction(viewVisibleAction, false);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});
	}



	private static class CameraFormatRectConverter extends CameraFormatConverter {

		private final ObjectProperty<Rectangle2D> property;


		public CameraFormatRectConverter(ObjectProperty<Rectangle2D> property) {
			this.property = property;
		}

		@Override
		public Rectangle2D from(CameraFormat value) {
			Rectangle2D rect = super.from(value);
			Rectangle2D propRect = property.get();

			if (nonNull(propRect)) {
				rect.setLocation(propRect.getX(), propRect.getY());
			}

			return rect;
		}

	}
}
