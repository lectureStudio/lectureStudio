package org.lecturestudio.swing.util;

import org.lecturestudio.swing.model.AdaptiveTab;

public interface AdaptiveTabbedPaneChangeListener {
	default void onTabAdded(boolean visibleAndEnabled) {
	}

	default void onTabRemoved() {
	}

	default void onTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
	}

	default void onVisibilityChanged(boolean visible) {
	}

	default void onNoTabsEnabled() {
	}
}
