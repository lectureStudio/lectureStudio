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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.lecturestudio.swing.view.ViewPostConstruct;

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

	@ViewPostConstruct
	private void initialize() {
		windowContainer.setLayout(new WrapLayout());
	}

	private void onScreenSourceSelected(ScreenSourceView screenSourceView) {
		updateSelection(screenSourceView, screenContainer);
		updateSelection(screenSourceView, windowContainer);

		sourceProperty.set(screenSourceView.getScreenSource());
	}

	private void updateSelection(ScreenSourceView screenSourceView, JComponent container) {
		for (var component : container.getComponents()) {
			ScreenSourceView view = (ScreenSourceView) component;
			view.setSelected(component == screenSourceView);
		}
	}



	private static class SourceListListener implements ListChangeListener<ObservableList<SharedScreenSource>> {

		private final JComponent container;

		private final Consumer<ScreenSourceView> selectConsumer;


		SourceListListener(JComponent container, Consumer<ScreenSourceView> consumer) {
			this.container = container;
			this.selectConsumer = consumer;
		}

		@Override
		public void listItemsInserted(ObservableList<SharedScreenSource> list,
				int startIndex, int itemCount) {
			SharedScreenSource screenSource = list.get(startIndex);

			addScreenSource(screenSource);
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
			addScreenSources(list);
		}

		private void addScreenSources(List<SharedScreenSource> list) {
			container.removeAll();

			for (SharedScreenSource screenSource : list) {
				addScreenSource(screenSource);
			}
		}

		private void addScreenSource(SharedScreenSource screenSource) {
			ScreenSourceView screenSourceView = new ScreenSourceView();
			screenSourceView.setMinimumSize(new Dimension(150, 100));
			screenSourceView.setMaximumSize(new Dimension(150, 100));
			screenSourceView.setPreferredSize(new Dimension(150, 100));
			screenSourceView.setScreenSource(screenSource);
			screenSourceView.addSelectionConsumer(selectConsumer);

			container.add(screenSourceView);
		}
	}



	private static class WrapLayout extends FlowLayout {

		public WrapLayout() {
			super(FlowLayout.LEFT);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			// Size of the largest component, so we can resize it in
			// either direction with something like a split-pane.
			return computeSize(target);
		}

		private Dimension computeSize(Container target) {
			synchronized (target.getTreeLock()) {
				int hgap = getHgap();
				int vgap = getVgap();
				int w = target.getWidth();

				// Let this behave like a regular FlowLayout (single row)
				// if the container hasn't been assigned any size yet.
				if (w == 0) {
					w = Integer.MAX_VALUE;
				}

				Insets insets = target.getInsets();
				if (insets == null) {
					insets = new Insets(0, 0, 0, 0);
				}

				int reqdWidth = 0;
				int maxwidth = w - (insets.left + insets.right + hgap * 2);
				int n = target.getComponentCount();
				int x = 0;
				int y = insets.top + vgap;
				int rowHeight = 0;

				for (int i = 0; i < n; i++) {
					Component c = target.getComponent(i);

					if (c.isVisible()) {
						Dimension d = c.getPreferredSize();

						if ((x == 0) || ((x + d.width) <= maxwidth)) {
							// Fits in current row.
							if (x > 0) {
								x += hgap;
							}
							x += d.width;
							rowHeight = Math.max(rowHeight, d.height);
						}
						else {
							// Start of new row.
							x = d.width;
							y += vgap + rowHeight;
							rowHeight = d.height;
						}
						reqdWidth = Math.max(reqdWidth, x);
					}
				}

				y += rowHeight;
				y += insets.bottom;

				return new Dimension(reqdWidth + insets.left + insets.right, y);
			}
		}
	}
}