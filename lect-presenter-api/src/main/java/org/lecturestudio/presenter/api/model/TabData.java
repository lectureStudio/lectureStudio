package org.lecturestudio.presenter.api.model;

import java.awt.*;

public final class TabData {
	public final int index;
	public final Component tab;
	public final Component tabComponent;

	public TabData(int index, Component tab, Component tabComponent) {
		this.index = index;
		this.tab = tab;
		this.tabComponent = tabComponent;
	}
}
