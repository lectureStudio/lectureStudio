package org.lecturestudio.swing.components;

import org.lecturestudio.swing.model.AdaptiveTab;
import org.lecturestudio.swing.model.AdaptiveTabType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class AdaptiveTabbedPane extends JComponent {
	public static final int TAB_SIZE_OFFSET = 3;

	private AdaptiveTabType defaultTabType = AdaptiveTabType.NORMAL;

	private final ArrayList<AdaptiveTab> tabs = new ArrayList<>();

	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final List<Consumer<Boolean>> visibilityChangedListeners = new ArrayList<>();

	private final List<BiConsumer<AdaptiveTab, Boolean>> sameTabClickedListeners = new ArrayList<>();

	private final List<Runnable> noTabsEnabledListeners = new ArrayList<>();

	private String selectedLabelText = "";

	/**
	 * Constructs an adaptive tab pane. Used by SWIXML, don't remove
	 */
	public AdaptiveTabbedPane() {
		this(SwingConstants.NORTH);
	}

	public AdaptiveTabbedPane(int tabPlacement) {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BorderLayout());
		tabbedPane.setTabPlacement(tabPlacement);
		add(tabbedPane);
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex = getTabIndexForCoordinate(e.getX(), e.getY());
					if (isIndexError(clickedTabIndex)) {
						return;
					}

					final String selectedTabLabelText =
							((JLabel) tabbedPane.getTabComponentAt(clickedTabIndex)).getText();
					final AdaptiveTab selectedTab = getTab(selectedTabLabelText);
					if (selectedTab == null || !selectedTab.isEnabled()) {
						return;
					}

					final String newSelectedLabelText = selectedTab.getLabel().getText();

					tabSelected(selectedTab, Objects.equals(selectedLabelText, newSelectedLabelText));

					selectedLabelText = newSelectedLabelText;
				}
			}
		});
	}

	public AdaptiveTab getTab(String labelText) {
		return tabs.stream().filter(tab -> tab.getLabel().getText().equals(labelText)).findFirst().orElse(null);
	}

	public void addVisibilityChangedListener(Consumer<Boolean> listener) {
		visibilityChangedListeners.add(listener);
	}

	public void removeVisibilityChangedListener(Consumer<Boolean> listener) {
		visibilityChangedListeners.remove(listener);
	}

	private void visibilityChanged(boolean visibility) {
		visibilityChangedListeners.forEach(listener -> listener.accept(visibility));
	}

	public void addSelectedTabChangedListener(BiConsumer<AdaptiveTab, Boolean> listener) {
		sameTabClickedListeners.add(listener);
	}

	public void removeSelectedTabChangedListener(BiConsumer<AdaptiveTab, Boolean> listener) {
		sameTabClickedListeners.remove(listener);
	}

	private void tabSelected(AdaptiveTab selectedTab, boolean sameTab) {
		sameTabClickedListeners.forEach(listener -> listener.accept(selectedTab, sameTab));
	}

	public void addNoTabsEnabledListener(Runnable listener) {
		noTabsEnabledListeners.add(listener);
	}

	public void removeNoTabsEnabledListener(Runnable listener) {
		noTabsEnabledListeners.remove(listener);
	}

	private void noTabsEnabled() {
		noTabsEnabledListeners.forEach(Runnable::run);
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

	public void setTabSelected(String labelText) {
		final int index = getTabIndex(labelText);

		if (isIndexError(index)) {
			return;
		}

		tabbedPane.setSelectedIndex(index);
		selectedLabelText = labelText;
	}

	public int getSelectedIndex() {
		return tabbedPane.getSelectedIndex();
	}

	public List<AdaptiveTab> getTabs() {
		return Collections.unmodifiableList(tabs);
	}

	public int getVisibleTabCount() {
		return tabbedPane.getTabCount();
	}

	public void setTabVisible(String labelText, boolean visible) {
		final int index = getTabIndex(labelText);
		setTabVisible(index, visible);
	}

	public void setTabVisible(int index, boolean visible) {
		if (isIndexError(index)) {
			return;
		}

		tabs.get(index).setVisible(visible);

		rebuild();
	}

	public void setTabEnabled(String labelText, boolean enabled) {
		final int index = getTabIndex(labelText);
		setTabEnabled(index, enabled);
	}

	public void setTabEnabled(int index, boolean enabled) {
		if (isIndexError(index)) {
			return;
		}

		tabs.get(index).setEnabled(enabled);
		rebuild();
	}

	private boolean isIndexError(int index) {
		return index < 0 || index >= tabs.size();
	}

	public int getTabIndex(String labelText) {
		return IntStream.range(0, tabs.size()).filter(i -> tabs.get(i).getLabel().getText().equals(labelText))
				.findFirst().orElse(-1);
	}

	public Component getSelectedComponent() {
		return tabbedPane.getSelectedComponent();
	}

	public void setBackgroundAt(int index, Color background) {
		tabbedPane.setBackgroundAt(index, background);
	}

	public boolean isTabPlacementVertical() {
		final int tabPlacement = tabbedPane.getTabPlacement();
		return tabPlacement == SwingConstants.LEFT || tabPlacement == SwingConstants.RIGHT;
	}

	public boolean isEnabledAt(int index) {
		if (isIndexError(index)) {
			return false;
		}

		return tabs.get(index).isEnabled();
	}

	public int getTabSize() {
		if (tabbedPane.getTabCount() <= 0) {
			return 0;
		}

		return isTabPlacementVertical() ? tabbedPane.getUI().getTabBounds(tabbedPane, 0).width + TAB_SIZE_OFFSET :
				tabbedPane.getUI().getTabBounds(tabbedPane, 0).height + TAB_SIZE_OFFSET;
	}

	public Dimension getMinimumDimension() {
		return isTabPlacementVertical() ? new Dimension(getTabSize(), 0) : new Dimension(0, getTabSize());
	}

	public int getTabIndexForCoordinate(int x, int y) {
		return tabbedPane.getUI().tabForCoordinate(tabbedPane, x, y);
	}

	public int getTabCount() {
		return tabbedPane.getTabCount();
	}

	public Component getTabComponentAt(int index) {
		return tabbedPane.getTabComponentAt(index);
	}

	public Component getComponentAt(int index) {
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
		final int index = IntStream.range(0, tabs.size()).filter(i -> tabs.get(i).type == tabType)
				.findFirst().orElse(tabs.size());

		tabs.add(index, tab);
		if (tab.isVisible() && tab.isEnabled()) {
			selectedLabelText = tab.getLabel().getText();
		}

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
		final int index = IntStream.range(0, this.tabs.size()).filter(i -> this.tabs.get(i).type == tabType)
				.findFirst().orElse(this.tabs.size());

		this.tabs.addAll(index, tabs);
		selectedLabelText = getLastVisibleAndEnabledTabLabelText();
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
		if (tab.isVisible() && tab.isEnabled()) {
			selectedLabelText = tab.getLabel().getText();
		}
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
		selectedLabelText = getLastVisibleAndEnabledTabLabelText();
		rebuild();
	}

	/**
	 * Removes a tab with matching {@code labelText}
	 *
	 * @param labelText Text of the tab's label
	 */
	public void removeTab(String labelText) {
		removeTab(getTabIndex(labelText));
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
		if (Objects.equals(tabToRemove.getLabel().getText(), selectedLabelText)) {
			selectedLabelText = getLastVisibleAndEnabledTabLabelText();
		}
		rebuild();
	}

	private String getLastVisibleAndEnabledTabLabelText() {
		final AdaptiveTab tab =
				tabs.stream().filter(tab1 -> tab1.isVisible() && tab1.isEnabled()).reduce((first, second) -> second)
						.orElse(null);
		if (tab == null) {
			return "";
		}
		return tab.getLabel().getText();
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

		boolean selectedLabelRemoved = false;

		while (iter.hasNext()) {
			final AdaptiveTab tab = iter.next();
			if (tab.type == tabType) {
				removedTabs.add(tab);
				iter.remove();
			}
			if (Objects.equals(tab.getLabel().getText(), selectedLabelText)) {
				selectedLabelRemoved = true;
			}
		}

		if (selectedLabelRemoved) {
			selectedLabelText = getLastVisibleAndEnabledTabLabelText();
		}

		rebuild();

		return Collections.unmodifiableList(removedTabs);
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
				tabSelected(tab, false);
				selectedIndexSet = true;
			}
		}

		final int lastTabIndex = tabbedPane.getTabCount() - 1;
		if (!selectedIndexSet && !isIndexError(lastTabIndex)) {
			tabbedPane.setSelectedIndex(lastTabIndex);
		}

		tabbedPane.setMinimumSize(getMinimumDimension());

		final boolean oldVisibility = isVisible();
		final boolean newVisibility = getVisibleTabCount() != 0;

		setVisible(newVisibility);

		if (oldVisibility != newVisibility) {
			visibilityChanged(newVisibility);
		}

		if (noTabsEnabled) {
			this.noTabsEnabled();
		}
	}
}
