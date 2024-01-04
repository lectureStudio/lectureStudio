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

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.app.Theme;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.converter.IntegerStringConverter;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.GeneralSettingsView;
import org.lecturestudio.swing.beans.ConvertibleNumberProperty;
import org.lecturestudio.swing.beans.ConvertibleObjectProperty;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "general-settings", presenter = org.lecturestudio.presenter.api.presenter.GeneralSettingsPresenter.class)
public class SwingGeneralSettingsView extends JPanel implements GeneralSettingsView {

	private JComboBox<Locale> localeCombo;

	private JCheckBox checkVersionCheckBox;

	private JCheckBox maximizedCheckBox;

	private JCheckBox fullscreenCheckBox;

	private JCheckBox mouseInputCheckBox;

	private JCheckBox tabletCheckBox;

	private JCheckBox saveAnnotationsCheckBox;

	private JCheckBox fullscreenModeCheckBox;

	private JTextField pageSelectDelayField;

	private JSlider extendViewSlider;

	private JButton closeButton;

	private JButton resetButton;


	SwingGeneralSettingsView() {
		super();
	}

	@Override
	public void setTheme(ObjectProperty<Theme> theme) {

	}

	@Override
	public void setThemes(List<Theme> themes) {

	}

	@Override
	public void setLocale(ObjectProperty<Locale> locale) {
		SwingUtils.bindBidirectional(localeCombo, locale);
	}

	@Override
	public void setLocales(List<Locale> locales) {
		SwingUtils.invoke(() -> localeCombo
				.setModel(new DefaultComboBoxModel<>(new Vector<>(locales))));
	}

	@Override
	public void setCheckNewVersion(BooleanProperty check) {
		SwingUtils.bindBidirectional(checkVersionCheckBox, check);
	}

	@Override
	public void setStartMaximized(BooleanProperty maximized) {
		SwingUtils.bindBidirectional(maximizedCheckBox, maximized);
	}

	@Override
	public void setStartFullscreen(BooleanProperty fullscreen) {
		SwingUtils.bindBidirectional(fullscreenCheckBox, fullscreen);
	}

	@Override
	public void setUseMouseInput(BooleanProperty useMouse) {
		SwingUtils.bindBidirectional(mouseInputCheckBox, useMouse);
	}

	@Override
	public void setTabletMode(BooleanProperty tabletMode) {
//		SwingUtils.bindBidirectional(tabletCheckBox, tabletMode);
	}

	@Override
	public void setSaveAnnotationsOnClose(BooleanProperty saveAnnotations) {
		SwingUtils.bindBidirectional(saveAnnotationsCheckBox, saveAnnotations);
	}

	@Override
	public void setPageSelectionDelay(IntegerProperty delay) {
		SwingUtils.bindBidirectional(pageSelectDelayField,
				new ConvertibleObjectProperty<>(delay,
						new IntegerStringConverter("#")));
	}

	@Override
	public void setExtendedFullscreen(BooleanProperty extended) {
//		SwingUtils.bindBidirectional(fullscreenModeCheckBox, extended);
	}

	@Override
	public void setExtendPageDimension(ObjectProperty<Dimension2D> dimension) {
		Converter<Dimension2D, Integer> converter = SlideSpaceConverter.INSTANCE;

		SwingUtils.bindBidirectional(extendViewSlider, new ConvertibleNumberProperty<>(dimension, converter));
	}

	@Override
	public void setTextSize(DoubleProperty size) {

	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}



	/**
	 * Extended slide space to number and vice-versa converter.
	 */
	private static class SlideSpaceConverter implements Converter<Dimension2D, Integer> {

		static final SlideSpaceConverter INSTANCE = new SlideSpaceConverter();

		private final PageMetrics metrics = new PageMetrics(4, 3);


		@Override
		public Integer to(Dimension2D value) {
			return (int) (Math.abs((value.getWidth() - metrics.getWidth()) / metrics.getWidth()) * 100);
		}

		@Override
		public Dimension2D from(Integer value) {
			double width = metrics.getWidth() - (value / 100.d * metrics.getWidth());
			double height = metrics.getHeight(width);

			return new Dimension2D(width, height);
		}
	}
}
