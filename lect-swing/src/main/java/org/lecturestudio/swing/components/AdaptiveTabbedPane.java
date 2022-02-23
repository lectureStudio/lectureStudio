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
import java.util.stream.IntStream;

public class AdaptiveTabbedPane extends JComponent {
	public static final int TAB_SIZE_OFFSET = 1;
	private AdaptiveTabType defaultTabType = AdaptiveTabType.NORMAL;

	private final ArrayList<AdaptiveTab> tabs = new ArrayList<>();
	private final JTabbedPane tabbedPane = new JTabbedPane();

	private String selectedLabelText = "";

	public AdaptiveTabbedPane() {
		this(SwingConstants.NORTH);
	}

	public AdaptiveTabbedPane(int tabPlacement) {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BorderLayout());
		tabbedPane.setTabPlacement(tabPlacement);
		setVisible(false);
		add(tabbedPane);
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int clickedTabIndex = getTabIndexForCoordinate(e.getX(), e.getY());
					if (isIndexError(clickedTabIndex)) {
						return;
					}

					final JLabel selectedLabel = (JLabel) tabbedPane.getTabComponentAt(clickedTabIndex);
					if (!selectedLabel.isEnabled()) {
						return;
					}

					selectedLabelText = selectedLabel.getText();
				}
			}
		});
	}

	public void setTabPlacement(int tabPlacement) {
		tabbedPane.setTabPlacement(tabPlacement);
	}

	public int getTabPlacement() {
		return tabbedPane.getTabPlacement();
	}

	public void setDefaultTabType(AdaptiveTabType defaultTabType) {
		this.defaultTabType = defaultTabType;
	}

	public AdaptiveTabType getDefaultTabType() {
		return defaultTabType;
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
		tabs.get(index).getLabel().setEnabled(enabled);
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
		selectedLabelText = tab.getLabel().getText();
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
		selectedLabelText = tabs.get(tabs.size() - 1).getLabel().getText();
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
		selectedLabelText = "";
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
		selectedLabelText = "";
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
		if (Objects.equals(tabs.get(index).getLabel().getText(), selectedLabelText)) {
			selectedLabelText = "";
		}
		tabs.remove(index);
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

		while (iter.hasNext()) {
			final AdaptiveTab tab = iter.next();
			if (tab.type == tabType) {
				removedTabs.add(tab);
				iter.remove();
			}
			if (Objects.equals(tab.getLabel().getText(), selectedLabelText)) {
				selectedLabelText = "";
			}
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

		int selectedIndex = -1;

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

			if (Objects.equals(selectedLabelText, label.getText()) && tab.isVisible()) {
				selectedIndex = index;
			}
		}

		final int tabCount = tabbedPane.getTabCount();
		if (selectedIndex == -1 && tabCount > 0) {
			tabbedPane.setSelectedIndex(tabCount - 1);
		} else if (!isIndexError(tabCount - 1)) {
			tabbedPane.setSelectedIndex(selectedIndex);
		}

		setVisible(getVisibleTabCount() != 0);

		tabbedPane.setMinimumSize(getMinimumDimension());
	}
}
