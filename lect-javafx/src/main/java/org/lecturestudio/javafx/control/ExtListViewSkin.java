package org.lecturestudio.javafx.control;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Set;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;

public class ExtListViewSkin<T> extends ListViewSkin<T> {

	private VirtualFlow<ListCell<T>> flow;

	private ScrollBar hBar;

	private ScrollBar vBar;


	/**
	 * Creates a new ListViewSkin instance.
	 *
	 * @param control The control that this skin should be installed onto.
	 */
	public ExtListViewSkin(final ListView<T> control) {
		super(control);

		initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void dispose() {
		flow = null;

		getChildren().removeAll(hBar, vBar);

		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void layoutChildren(final double x, final double y, final double w, final double h) {
		super.layoutChildren(x, y, w, h);

		ListView<T> control = getSkinnable();

		final double prefWidth = vBar.prefWidth(-1);
		final double prefHeight = hBar.prefHeight(-1);

		final double hBarX = control.snappedLeftInset();
		final double hBarY = h - prefHeight + control.snappedBottomInset();
		final double vBarX = w - prefWidth + control.snappedRightInset();
		final double vBarY = control.snappedTopInset();

		double hBarW = w;
		double vBarH = h;

		if (hBar.isVisible()) {
			vBarH -= prefHeight;
		}
		if (vBar.isVisible()) {
			hBarW -= prefWidth;
		}

		hBar.resizeRelocate(hBarX, hBarY, hBarW, prefHeight);
		vBar.resizeRelocate(vBarX, vBarY, prefWidth, vBarH);
	}

	private void initialize() {
		flow = getVirtualFlow();

		vBar = new ScrollBar();
		vBar.setManaged(false);
		vBar.setOrientation(Orientation.VERTICAL);
		vBar.getStyleClass().add("theme-scroll-bar");

		hBar = new ScrollBar();
		hBar.setManaged(false);
		hBar.setOrientation(Orientation.HORIZONTAL);
		hBar.getStyleClass().add("theme-scroll-bar");

		getChildren().addAll(hBar, vBar);

		bindScrollBars();
	}

	private void onResize() {
		if (isNull(flow)) {
			// Happens during initialization.
			return;
		}

		Orientation orientation = getSkinnable().getOrientation();

		boolean isVertical = orientation == Orientation.VERTICAL;

		final ListCell<T> first = flow.getFirstVisibleCell();
		final ListCell<T> last = flow.getLastVisibleCell();

		final int firstIndex = nonNull(first) ? first.getIndex() : 0;
		final int lastIndex = nonNull(last) ? last.getIndex() : flow.getCellCount();

		final double viewPortLength = isVertical ? flow.getHeight() : flow.getWidth();
		final double viewPortBreadth = isVertical ? flow.getWidth() : flow.getHeight();

		double cellsLength = 0;
		double cellsBreadth = 0;

		for (int i = firstIndex; i <= lastIndex; i++) {
			ListCell<T> cell = flow.getVisibleCell(i);

			if (nonNull(cell)) {
				cellsLength += isVertical ? cell.getHeight() : cell.getWidth();
				cellsBreadth = Math.max(cellsBreadth, isVertical ? cell.getWidth() : cell.getHeight());
			}
		}

		double fixedCellSize = getSkinnable().getFixedCellSize();
		double inc = 1;

		if (fixedCellSize > 0) {
			double contentLength = fixedCellSize * flow.getCellCount();
			inc = fixedCellSize / (contentLength - viewPortLength);
		}

		boolean showBreadthBar = Math.abs(cellsBreadth - viewPortBreadth) > 1;
		boolean showLengthBar = cellsLength > viewPortLength;

		if (isVertical) {
			hBar.setVisible(showBreadthBar);
			vBar.setVisible(showLengthBar);

			if (fixedCellSize > 0) {
				vBar.setUnitIncrement(inc);
				vBar.setBlockIncrement(inc);
			}
		}
		else {
			hBar.setVisible(showLengthBar);
			vBar.setVisible(showBreadthBar);

			if (fixedCellSize > 0) {
				hBar.setUnitIncrement(inc);
				hBar.setBlockIncrement(inc);
			}
		}
	}

	private void bindScrollBars() {
		final Set<Node> nodes = getSkinnable().lookupAll("VirtualScrollBar");

		for (Node node : nodes) {
			if (node instanceof ScrollBar) {
				ScrollBar bar = (ScrollBar) node;

				if (bar.getOrientation().equals(Orientation.HORIZONTAL)) {
					bindScrollBars(hBar, bar);
				}
				else if (bar.getOrientation().equals(Orientation.VERTICAL)) {
					bindScrollBars(vBar, bar);
				}
			}
		}
	}

	private static void bindScrollBars(ScrollBar scrollBarA, ScrollBar scrollBarB) {
		scrollBarA.valueProperty().bindBidirectional(scrollBarB.valueProperty());
		scrollBarA.minProperty().bindBidirectional(scrollBarB.minProperty());
		scrollBarA.maxProperty().bindBidirectional(scrollBarB.maxProperty());
		scrollBarA.visibleProperty().bindBidirectional(scrollBarB.visibleProperty());
		scrollBarA.visibleAmountProperty().bindBidirectional(scrollBarB.visibleAmountProperty());
		scrollBarA.unitIncrementProperty().bindBidirectional(scrollBarB.unitIncrementProperty());
		scrollBarA.blockIncrementProperty().bindBidirectional(scrollBarB.blockIncrementProperty());
	}
}
