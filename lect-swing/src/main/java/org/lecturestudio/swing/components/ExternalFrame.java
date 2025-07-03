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

	private String name;

	private Consumer<ExternalWindowPosition> positionChangedAction;

	private Runnable closedAction;

	private Consumer<Dimension> sizeChangedAction;

	private String placeholderText;

	private final Component body;

	private final Component placeholder;

	private boolean showBody = false;


	public ExternalFrame(String name, Component body, String placeholderText) {
		super(nonNull(name) ? name : "");

		this.body = body;
		this.placeholder = new JLabel(placeholderText, SwingConstants.CENTER);

		final List<Image> icons = new ArrayList<>();
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/24.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/32.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/48.png"));
		icons.add(AwtResourceLoader.getImage("gfx/app-icon/128.png"));

		setIconImages(icons);

		addPlaceholder();
	}

	@Override
	public void setMinimumSize(Dimension minimumSize) {
		super.setMinimumSize(UIScale.scale(minimumSize));
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && showBody) {
			removePlaceholder();
			add(body);
		}
		else {
			remove(body);
			addPlaceholder();
		}

		super.setVisible(visible);

		if (visible) {
			toFront();
		}
	}

	public void setClosedAction(Runnable closedAction) {
		this.closedAction = closedAction;

		if (nonNull(closedAction)) {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (nonNull(closedAction)) {
						closedAction.run();
					}
				}
			});
		}
	}

	public void setSizeChangedAction(Consumer<Dimension> sizeAction) {
		sizeChangedAction = sizeAction;

		if (nonNull(sizeChangedAction)) {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					if (nonNull(sizeChangedAction)) {
						sizeChangedAction.accept(e.getComponent().getSize());
					}
				}
			});
		}
	}

	public void setPositionChangedAction(Consumer<ExternalWindowPosition> positionAction) {
		positionChangedAction = positionAction;

		if (nonNull(positionChangedAction) ) {
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentMoved(ComponentEvent e) {
					if (nonNull(positionChangedAction)) {
						externalComponentOpenedOrMoved(e, positionChangedAction);
					}
				}
			});
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

	private void externalComponentOpenedOrMoved(ComponentEvent e, Consumer<ExternalWindowPosition> action) {
		final Point position = e.getComponent().getLocationOnScreen();
		final Screen screen = Screens.createScreen(e.getComponent().getGraphicsConfiguration().getDevice(), null);

		action.accept(new ExternalWindowPosition(screen, position));
	}
}
