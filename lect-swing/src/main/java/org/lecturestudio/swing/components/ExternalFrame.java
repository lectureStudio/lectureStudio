package org.lecturestudio.swing.components;

import com.formdev.flatlaf.util.UIScale;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.model.ExternalWindowPosition;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

public class ExternalFrame extends JFrame {
	private final Component body;

	private final Component placeholder;

	private boolean showBody = false;

	public ExternalFrame(String name, Component body, Dimension minimumSize,
						 Consumer<ExternalWindowPosition> positionChangedAction, Runnable closedAction,
						 Consumer<Dimension> sizeChangedAction, String placeholderText) {
		super(nonNull(name) ? name : "");

		this.body = body;
		this.placeholder = new JLabel(placeholderText, SwingConstants.CENTER);

		final List<Image> icons = new ArrayList<>();
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/24.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/32.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/48.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/128.png"));

		setIconImages(icons);

		setMinimumSize(UIScale.scale(minimumSize));

		if (nonNull(closedAction)) {
			addClosingListener(closedAction);

		}

		if (nonNull(positionChangedAction) || nonNull(sizeChangedAction)) {
			addMovedResizedListener(positionChangedAction, sizeChangedAction);
		}

		addPlaceholder();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && showBody) {
			removePlaceholder();
			add(body);
		} else {
			remove(body);
			addPlaceholder();
		}

		super.setVisible(visible);

		if (visible) {
			toFront();
		}
	}

	public void showBody() {
		if (showBody) {
			return;
		}

		if (isVisible()) {
			removePlaceholder();
			add(body);

			revalidate();
			repaint();
		}


		showBody = true;
	}

	public void hideBody() {
		if (!showBody) {
			return;
		}

		if (isVisible()) {
			remove(body);
			addPlaceholder();

			revalidate();
			repaint();
		}

		showBody = false;
	}

	public boolean isShowBody() {
		return showBody;
	}

	private void addPlaceholder() {
		if (nonNull(placeholder)) {
			add(placeholder);
		}
	}

	private void removePlaceholder() {
		if (nonNull(placeholder)) {
			remove(placeholder);
		}
	}

	public void updatePosition(Screen screen, Point position, Dimension size) {
		if (nonNull(screen) && nonNull(position)) {
			setLocation(position.x, position.y);
		}

		if (nonNull(size)) {
			setSize(size);
		}
	}

	private void addClosingListener(Runnable closedAction) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (nonNull(closedAction)) {
					closedAction.run();
				}
			}
		});
	}

	private void addMovedResizedListener(Consumer<ExternalWindowPosition> positionChangedAction,
										 Consumer<Dimension> sizeChangedAction) {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				if (nonNull(positionChangedAction)) {
					externalComponentOpenedOrMoved(e, positionChangedAction);
				}
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if (nonNull(sizeChangedAction)) {
					sizeChangedAction.accept(e.getComponent().getSize());
				}
			}
		});
	}

	private void externalComponentOpenedOrMoved(ComponentEvent e, Consumer<ExternalWindowPosition> action) {
		final Point position = e.getComponent().getLocationOnScreen();
		final Screen screen = Screens.createScreen(e.getComponent().getGraphicsConfiguration().getDevice());

		action.accept(new ExternalWindowPosition(screen, position));
	}

	public static class Builder {
		private String name;

		private Component body;

		private Dimension minimumSize = new Dimension(300, 300);

		private Consumer<ExternalWindowPosition> positionChangedAction;

		private Runnable closedAction;

		private Consumer<Dimension> sizeChangedAction;

		private String placeholderText;

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setBody(Component body) {
			this.body = body;
			return this;
		}

		public Builder setMinimumSize(Dimension minimumSize) {
			this.minimumSize = minimumSize;
			return this;
		}

		public Builder setPositionChangedAction(Consumer<ExternalWindowPosition> positionChangedAction) {
			this.positionChangedAction = positionChangedAction;
			return this;
		}

		public Builder setClosedAction(Runnable closedAction) {
			this.closedAction = closedAction;
			return this;
		}

		public Builder setSizeChangedAction(Consumer<Dimension> sizeChangedAction) {
			this.sizeChangedAction = sizeChangedAction;
			return this;
		}

		public Builder setPlaceholderText(String placeholderText) {
			this.placeholderText = placeholderText;
			return this;
		}

		public ExternalFrame build() {
			return new ExternalFrame(name, body, minimumSize, positionChangedAction, closedAction, sizeChangedAction,
					placeholderText);
		}
	}
}
