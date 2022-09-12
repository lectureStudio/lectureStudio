/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.model.SharedScreenSource;
import org.lecturestudio.presenter.api.view.StartScreenSharingView;
import org.lecturestudio.presenter.swing.component.ScreenSourceView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;

@SwingView(name = "start-screen-sharing")
public class SwingStartScreenSharingView extends JPanel implements StartScreenSharingView {

	private JPanel screenContainer;

	private JPanel windowContainer;

	private JButton closeButton;

	private JButton startButton;

	private ObjectProperty<SharedScreenSource> sourceProperty;


	SwingStartScreenSharingView() {
		super();
	}

	@Override
	public void setWindows(ObservableList<SharedScreenSource> windows) {
		windows.addListener(new SourceListListener(windowContainer,
				this::onScreenSourceSelected));
	}

	@Override
	public void setScreens(ObservableList<SharedScreenSource> screens) {
		screens.addListener(new SourceListListener(screenContainer,
				this::onScreenSourceSelected));
	}

	@Override
	public void bindScreenSource(ObjectProperty<SharedScreenSource> sourceProperty) {
		this.sourceProperty = sourceProperty;

		sourceProperty.addListener((o, oldValue, newValue) -> {
			startButton.setEnabled(nonNull(newValue));
		});

		startButton.setEnabled(nonNull(sourceProperty.get()));
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnStart(Action action) {
		SwingUtils.bindAction(startButton, action);
	}

	private void onScreenSourceSelected(ScreenSourceView screenSourceView) {
		updateSelection(screenSourceView, screenContainer);
		updateSelection(screenSourceView, windowContainer);

		sourceProperty.set(screenSourceView.getScreenSource());
	}

	private void updateSelection(ScreenSourceView screenSourceView, JPanel container) {
		for (var component : container.getComponents()) {
			ScreenSourceView view = (ScreenSourceView) component;
			view.setSelected(component == screenSourceView);
		}
	}



	private static class SourceListListener implements ListChangeListener<ObservableList<SharedScreenSource>> {

		private final JPanel container;

		private final Consumer<ScreenSourceView> selectConsumer;


		SourceListListener(JPanel container, Consumer<ScreenSourceView> consumer) {
			this.container = container;
			this.selectConsumer = consumer;
		}

		@Override
		public void listItemsInserted(ObservableList<SharedScreenSource> list,
				int startIndex, int itemCount) {
			SharedScreenSource screenSource = list.get(startIndex);

			addScreenSource(screenSource, container);
		}

		@Override
		public void listItemsRemoved(ObservableList<SharedScreenSource> list,
				int startIndex, int itemCount) {
			for (var component : container.getComponents()) {
				if (component instanceof ScreenSourceView) {
					ScreenSourceView screenSourceView = (ScreenSourceView) component;

					if (!list.contains(screenSourceView.getScreenSource())) {
						screenSourceView.removeSelectionConsumer(selectConsumer);

						container.remove(screenSourceView);
						container.revalidate();
						container.repaint();
						break;
					}
				}
			}
		}

		@Override
		public void listChanged(ObservableList<SharedScreenSource> list) {
			addScreenSources(list, container);
		}

		private void addScreenSources(List<SharedScreenSource> list,
				JPanel container) {
			container.removeAll();

			for (SharedScreenSource screenSource : list) {
				addScreenSource(screenSource, container);
			}
		}

		private void addScreenSource(SharedScreenSource screenSource,
				JPanel container) {
			ScreenSourceView screenSourceView = new ScreenSourceView();
			screenSourceView.setMaximumSize(new Dimension(150, 100));
			screenSourceView.setPreferredSize(new Dimension(150, 100));
			screenSourceView.setScreenSource(screenSource);
			screenSourceView.addSelectionConsumer(selectConsumer);

			container.add(screenSourceView);
		}
	}
}