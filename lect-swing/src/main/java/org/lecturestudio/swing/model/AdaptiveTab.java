package org.lecturestudio.swing.model;

import javax.swing.*;
import java.awt.*;

public class AdaptiveTab {
	private JLabel label;
	private final Component component;
	private boolean visible;
	private boolean enabled;
	public final AdaptiveTabType type;

	public AdaptiveTab(AdaptiveTabType type, JLabel label, Component component) {
		this(type, label, component, true, true);
	}

	public AdaptiveTab(AdaptiveTabType type, JLabel label, Component component, boolean visible, boolean enabled) {
		this.type = type;
		this.component = component;
		this.label = label;
		this.visible = visible;
		this.enabled = enabled;
	}

	public Component getComponent() {
		return component;
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
