package org.lecturestudio.presenter.api.model;

import javax.swing.*;
import java.awt.*;

public class TabData {
	public final Component component;
	private JLabel tabComponent;
	private boolean visible;
	private boolean enabled;

	public TabData(Component component, JLabel tabComponent, boolean visible, boolean enabled) {
		this.component = component;
		this.tabComponent = tabComponent;
		this.visible = visible;
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public JLabel getTabComponent() {
		return tabComponent;
	}

	public void setTabComponent(JLabel tabComponent) {
		this.tabComponent = tabComponent;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
