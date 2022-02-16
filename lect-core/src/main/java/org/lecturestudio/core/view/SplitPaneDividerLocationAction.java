package org.lecturestudio.core.view;

import javax.swing.*;

@FunctionalInterface
public interface SplitPaneDividerLocationAction {
	int calculate(JSplitPane pane, int tabSize, int tabOffset);
}
