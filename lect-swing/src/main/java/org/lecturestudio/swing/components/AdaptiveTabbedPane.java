package org.lecturestudio.swing.components;

import com.formdev.flatlaf.util.UIScale;
import org.lecturestudio.swing.model.AdaptiveTab;
import org.lecturestudio.swing.model.AdaptiveTabType;
import org.lecturestudio.swing.util.AdaptiveTabbedPaneChangeListener;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class AdaptiveTabbedPane extends JComponent {
	public static final int TAB_SIZE_OFFSET = UIScale.scale(1);

	private AdaptiveTabType defaultTabType = AdaptiveTabType.NORMAL;

	private final List<AdaptiveTab> tabs = new ArrayList<>();

	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final List<AdaptiveTabbedPaneChangeListener> changeListeners = new ArrayList<>();

	private String selectedLabelText = "";

	/**
	 * Constructs an {@link AdaptiveTabbedPane}. Used by SWIXML, don't remove
	 */
	public AdaptiveTabbedPane() {
		this(SwingConstants.NORTH);
	}

	/**
	 * Constructs an {@link AdaptiveTabbedPane}
	 *
	 * @param tabPlacement Tab placement, ideally from {@link SwingConstants}
	 */
	public AdaptiveTabbedPane(int tabPlacement) {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BorderLayout());
		tabbedPane.setTabPlacement(tabPlacement);
		add(tabbedPane);
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex = getPaneTabIndexForCoordinate(e.getX(), e.getY());
					if (isIndexError(clickedTabIndex)) {
						return;
					}

					final AdaptiveTab selectedTab =
							getTab(((JLabel) tabbedPane.getTabComponentAt(clickedTabIndex)).getText());
					if (selectedTab == null) {
						return;
					}
					updateSelectedTabClicked(selectedTab,
							Objects.equals(selectedTab.getLabelText(), selectedLabelText));
				}
			}
		});
	}

	/**
	 * Get tab by its label text
	 *
	 * @param labelText Tab's label text
	 * @return {@link AdaptiveTab} or {@code null} if not found
	 */
	@Nullable
	public AdaptiveTab getTab(String labelText) {
		return getTabByLabelText(labelText);
	}

	public void addChangeListener(AdaptiveTabbedPaneChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(AdaptiveTabbedPaneChangeListener listener) {
		changeListeners.remove(listener);
	}

	private void tabAdded(boolean enabledOrVisible) {
		changeListeners.forEach(listener -> listener.onTabAdded(enabledOrVisible));
	}

	private void tabRemoved() {
		changeListeners.forEach(AdaptiveTabbedPaneChangeListener::onTabRemoved);
	}

	private void tabClicked(AdaptiveTab tab, boolean sameTab) {
		if (tab == null) {
			selectedLabelText = "";
		} else {
			selectedLabelText = tab.getLabelText();
			changeListeners.forEach(listener -> listener.onTabClicked(tab, sameTab));
		}
	}

	private void visibilityChanged(boolean visibility) {
		changeListeners.forEach(listener -> listener.onVisibilityChanged(visibility));
	}

	private void noTabsEnabled() {
		changeListeners.forEach(AdaptiveTabbedPaneChangeListener::onNoTabsEnabled);
	}

	/**
	 * Set tab placement. Used by SWIXML, dont remove
	 *
	 * @param tabPlacement Tab placement to set
	 */
	public void setTabPlacement(int tabPlacement) {
		tabbedPane.setTabPlacement(tabPlacement);
	}

	public int getTabPlacement() {
		return tabbedPane.getTabPlacement();
	}

	/**
	 * Sets the default tab type. Used by SWIXML, don't remove
	 *
	 * @param defaultTabType Default tab type to set
	 */
	public void setDefaultTabType(AdaptiveTabType defaultTabType) {
		this.defaultTabType = defaultTabType;
	}

	public AdaptiveTabType getDefaultTabType() {
		return defaultTabType;
	}

	public void setPaneTabSelected(String labelText) {
		final int index = getPaneTabIndex(labelText);

		if (isPaneIndexError(index)) {
			return;
		}

		tabbedPane.setSelectedIndex(index);
		selectedLabelText = labelText;
	}

	public int getPaneSelectedIndex() {
		return tabbedPane.getSelectedIndex();
	}

	public List<AdaptiveTab> getTabs() {
		return Collections.unmodifiableList(tabs);
	}

	public int getPaneTabCount() {
		return tabbedPane.getTabCount();
	}

	public void setTabVisible(String labelText, boolean visible) {
		final int index = getTabIndexByLabelText(labelText);
		setTabVisible(index, visible);
	}

	public void setTabVisible(int index, boolean visible) {
		if (isIndexError(index)) {
			return;
		}

		final AdaptiveTab tab = tabs.get(index);
		tab.setVisible(visible);

		if (!visible) {
			updateSelectedTabRemoved(tab);
		} else {
			updateSelectedTabAdded(tab);
		}

		rebuild();
	}

	public void setTabEnabled(String labelText, boolean enabled) {
		final int index = getTabIndexByLabelText(labelText);
		setTabEnabled(index, enabled);
	}

	public void setTabEnabled(int index, boolean enabled) {
		if (isIndexError(index)) {
			return;
		}

		final AdaptiveTab tab = tabs.get(index);
		tab.setEnabled(enabled);

		if (enabled) {
			updateSelectedTabAdded(tab);
		} else {
			updateSelectedTabRemoved(tab);
		}

		rebuild();
	}

	private boolean isIndexError(int index) {
		return index < 0 || index >= tabs.size();
	}

	private boolean isPaneIndexError(int index) {
		return index < 0 || index >= tabbedPane.getTabCount();
	}

	public int getPaneTabIndex(String labelText) {
		return IntStream.range(0, tabbedPane.getTabCount())
				.filter(i -> Objects.equals(((JLabel) tabbedPane.getTabComponentAt(i)).getText(), labelText))
				.findFirst().orElse(-1);
	}

	public Component getSelectedComponent() {
		return tabbedPane.getSelectedComponent();
	}

	public void setPaneBackgroundAt(int index, Color background) {
		tabbedPane.setBackgroundAt(index, background);
	}

	public boolean isTabPlacementVertical() {
		final int tabPlacement = tabbedPane.getTabPlacement();
		return tabPlacement == SwingConstants.LEFT || tabPlacement == SwingConstants.RIGHT;
	}

	private int getPaneTabSize() {
		if (tabbedPane.getTabCount() <= 0) {
			return 0;
		}

		final Rectangle tabBounds = tabbedPane.getUI().getTabBounds(tabbedPane, 0);

		return isTabPlacementVertical() ? tabBounds.width : tabBounds.height;
	}

	public Dimension getMinimumDimension() {
		return isTabPlacementVertical() ? new Dimension(getPaneTabSize() + TAB_SIZE_OFFSET, 0) :
				new Dimension(0, getPaneTabSize() + TAB_SIZE_OFFSET);
	}

	public int getPaneMainAxisSize() {
		return getPaneTabSize() + TAB_SIZE_OFFSET;
	}

	public int getPaneTabIndexForCoordinate(int x, int y) {
		return tabbedPane.getUI().tabForCoordinate(tabbedPane, x, y);
	}

	public Component getPaneComponentAt(int index) {
		return tabbedPane.getComponentAt(index);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		tabbedPane.addMouseListener(l);
	}

	/**
	 * Adds a tab before the first tab that matches {@code tabType}, or if none present at the end
	 *
	 * @param tab     {@link AdaptiveTab} to be added
	 * @param tabType {@link AdaptiveTabType} of the tab to add before
	 */
	public void addTabBefore(AdaptiveTab tab, AdaptiveTabType tabType) {
		convertTabLabel(tab);
		addTabBeforeType(tab, tabType);
		updateSelectedTabAdded(tab);
		rebuild();
	}

	/**
	 * Adds multiple tabs before the first tab that matches {@code tabType}, or if none present at the end
	 *
	 * @param tabs    {@link List<AdaptiveTab>} to be added
	 * @param tabType {@link AdaptiveTabType} of the tab to add before
	 */
	public void addTabsBefore(List<AdaptiveTab> tabs, AdaptiveTabType tabType) {
		tabs.forEach(this::convertTabLabel);
		addAllTabsBeforeType(tabs, tabType);
		updateSelectedTabsAddedByType(tabs, tabType);
		rebuild();
	}

	/**
	 * Adds a tab
	 *
	 * @param tab {@link AdaptiveTab} to be added
	 */
	public void addTab(AdaptiveTab tab) {
		convertTabLabel(tab);
		tabs.add(tab);
		updateSelectedTabAdded(tab);
		rebuild();
	}

	/**
	 * Adds multiple tabs
	 *
	 * @param tabs {@link AdaptiveTab} to be added
	 */
	public void addTabs(List<AdaptiveTab> tabs) {
		tabs.forEach(this::convertTabLabel);
		this.tabs.addAll(tabs);
		updateSelectedTabsAdded(tabs);
		rebuild();
	}

	/**
	 * Adds multiple tabs at given index
	 *
	 * @param tabs  {@link AdaptiveTab} to be added
	 * @param index The index at which to add the tabs
	 */
	public void addTabs(List<AdaptiveTab> tabs, int index) {
		tabs.forEach(this::convertTabLabel);
		this.tabs.addAll(index, tabs);
		updateSelectedTabsAdded(tabs);
		rebuild();
	}

	/**
	 * Removes a tab with matching {@code labelText}
	 *
	 * @param labelText Text of the tab's label
	 */
	public void removeTab(String labelText) {
		removeTab(getTabIndexByLabelText(labelText));
	}

	/**
	 * Removes tab at {@code index}
	 *
	 * @param index Index of the tab that should be removed
	 */
	public void removeTab(int index) {
		if (isIndexError(index)) {
			return;
		}
		final AdaptiveTab tabToRemove = tabs.get(index);
		tabs.remove(index);
		updateSelectedTabRemoved(tabToRemove);
		rebuild();
	}

	/**
	 * Removes tabs matching the {@code tabType}
	 *
	 * @param tabType {@link AdaptiveTabType} of the tabs that should be removed
	 * @return Removed tabs
	 */
	public List<AdaptiveTab> removeTabsByType(AdaptiveTabType tabType) {
		final ArrayList<AdaptiveTab> removedTabs = new ArrayList<>();

		final Iterator<AdaptiveTab> iter = tabs.iterator();

		AdaptiveTab removedSelectedTab = null;

		while (iter.hasNext()) {
			final AdaptiveTab tab = iter.next();
			if (tab.type == tabType) {
				removedTabs.add(tab);
				iter.remove();
			}
			if (Objects.equals(tab.getLabelText(), selectedLabelText)) {
				removedSelectedTab = tab;
			}
		}

		if (removedSelectedTab != null) {
			updateSelectedTabRemoved(removedSelectedTab);
		}

		rebuild();

		return Collections.unmodifiableList(removedTabs);
	}

	/**
	 * Adds a tab before the first tab that matches {@code tabType}, or if none present at the end
	 *
	 * @param tab     {@link AdaptiveTab} to be added
	 * @param tabType {@link AdaptiveTabType} of the tab to add before
	 */
	private void addTabBeforeType(AdaptiveTab tab, AdaptiveTabType tabType) {
		final int index = getLastIndexOfTabType(tabType);

		tabs.add(index, tab);
	}

	/**
	 * Get tab by its label text
	 *
	 * @param labelText Tab's label text
	 * @return {@link AdaptiveTab} or {@code null} if not found
	 */
	@Nullable
	private AdaptiveTab getTabByLabelText(String labelText) {
		final int index = getTabIndexByLabelText(labelText);
		if (index == -1) {
			return null;
		} else {
			return tabs.get(index);
		}
	}

	/**
	 * Get tab's index by its label text
	 *
	 * @param labelText Tab's label text
	 * @return {@link AdaptiveTab} or {@code -1} if not found
	 */
	private int getTabIndexByLabelText(String labelText) {
		return IntStream.range(0, tabs.size()).filter(i -> tabs.get(i).getLabelText().equals(labelText)).findFirst()
				.orElse(-1);
	}

	/**
	 * Get last visible and enabled tab
	 *
	 * @return {@link AdaptiveTab}
	 */
	@Nullable
	private AdaptiveTab getLastVisibleAndEnabledTab() {
		final int index = getLastVisibleAndEnabledTabIndex();
		if (index == -1) {
			return null;
		} else {
			return tabs.get(index);
		}
	}

	/**
	 * Get index of last visible and enabled tab
	 *
	 * @return Index
	 */
	private int getLastVisibleAndEnabledTabIndex() {
		return IntStream.range(0, tabs.size()).filter(i -> tabs.get(i).isVisible() && tabs.get(i).isEnabled())
				.reduce((first, second) -> second).orElse(-1);
	}

	/**
	 * Adds multiple tabs before the first tab that matches {@code tabType}, or if none present at the end
	 *
	 * @param tabs    {@link List <AdaptiveTab>} to be added
	 * @param tabType {@link AdaptiveTabType} of the tab to add before
	 */
	private void addAllTabsBeforeType(List<AdaptiveTab> tabs, AdaptiveTabType tabType) {
		final int index = getLastIndexOfTabType(tabType);

		this.tabs.addAll(index, tabs);
	}

	private int getLastIndexOfTabType(AdaptiveTabType type) {
		return IntStream.range(0, tabs.size()).filter(i -> tabs.get(i).type == type).findFirst().orElse(tabs.size());
	}

	private void updateSelectedTabClicked(AdaptiveTab clickedTab, boolean sameTab) {
		if (clickedTab.isEnabled()) {
			tabClicked(clickedTab, sameTab);
		}
	}

	private void updateSelectedTabsAddedByType(List<AdaptiveTab> tabs, AdaptiveTabType type) {
		final AdaptiveTab last = tabs.stream().filter(tab -> tab.isVisible() && tab.isEnabled() && tab.type == type)
				.reduce((first, second) -> second).orElse(null);
		updateSelectedTabAdded(last);
	}

	private void updateSelectedTabsAdded(List<AdaptiveTab> tabs) {
		final AdaptiveTab last =
				tabs.stream().filter(tab -> tab.isVisible() && tab.isEnabled()).reduce((first, second) -> second)
						.orElse(null);
		updateSelectedTabAdded(last);
	}

	private void updateSelectedTabAdded(@Nullable AdaptiveTab addedTab) {
		if (addedTab != null && addedTab.isVisible() && addedTab.isEnabled()) {
			selectedLabelText = addedTab.getLabelText();
			tabAdded(true);
		} else {
			final AdaptiveTab last = getLastVisibleAndEnabledTab();
			if (last != null) {
				selectedLabelText = last.getLabelText();
			}
			tabAdded(false);
		}
	}

	private void updateSelectedTabRemoved(AdaptiveTab removedTab) {
		if (Objects.equals(selectedLabelText, removedTab.getLabelText())) {
			final AdaptiveTab last = getLastVisibleAndEnabledTab();
			if (last != null) {
				selectedLabelText = last.getLabelText();
			}
		}
		tabRemoved();
	}

	private int getLastEnabledPaneTabIndex() {
		return IntStream.range(0, tabbedPane.getTabCount()).filter(tabbedPane::isEnabledAt)
				.reduce((first, second) -> second).orElse(-1);
	}

	/**
	 * Converts the label of the {@code tab} to match the tab pane orientation
	 *
	 * @param tab {@link AdaptiveTab} at which the label should be transformed
	 */
	private void convertTabLabel(AdaptiveTab tab) {
		final JLabel label = tab.getLabel();

		final int tabPlacement = tabbedPane.getTabPlacement();

		if (tabPlacement == SwingConstants.LEFT) {
			tab.setLabel(VerticalTab.fromJLabel(label, tabPlacement));
		} else if (tabPlacement == SwingConstants.RIGHT) {
			tab.setLabel(VerticalTab.fromJLabel(label, tabPlacement));
		} else {
			tab.setLabel(new JLabel(label.getText(), label.getIcon(), SwingConstants.LEFT));
		}
	}

	/**
	 * Rebuilds the tabPane using the {@link #tabs} list as a source
	 */
	private synchronized void rebuild() {
		tabbedPane.removeAll();

		boolean selectedIndexSet = false;

		boolean noTabsEnabled = true;

		for (final AdaptiveTab tab : tabs) {
			if (!tab.isVisible()) {
				continue;
			}

			final Component component = tab.getComponent();
			final JLabel label = tab.getLabel();

			tabbedPane.addTab(null, tab.isEnabled() ? component : null);
			tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);

			final int index = tabbedPane.getTabCount() - 1;

			tabbedPane.getTabComponentAt(index).setEnabled(tab.isEnabled());
			tabbedPane.setEnabledAt(index, tab.isEnabled());

			if (tab.isEnabled()) {
				noTabsEnabled = false;
			}

			if (Objects.equals(selectedLabelText, label.getText()) && tab.isVisible()) {
				tabbedPane.setSelectedIndex(index);
				selectedIndexSet = true;
			}
		}

		final int lastEnabledIndex = getLastEnabledPaneTabIndex();
		if (!selectedIndexSet && !isPaneIndexError(lastEnabledIndex)) {
			tabbedPane.setSelectedIndex(lastEnabledIndex);
		}

		tabbedPane.setMinimumSize(getMinimumDimension());

		final boolean oldVisibility = isVisible();
		final boolean newVisibility = getPaneTabCount() != 0;

		setVisible(newVisibility);

		if (oldVisibility != newVisibility) {
			visibilityChanged(newVisibility);
		}

		if (noTabsEnabled) {
			noTabsEnabled();
		}
	}

	/**
	 * Opens the tab with the given index
	 *
	 * @param index Position of the tab in the tabbar
	 */
    public void setSelectedIndex(int index) {
		tabbedPane.setSelectedIndex(index);
    }
}
