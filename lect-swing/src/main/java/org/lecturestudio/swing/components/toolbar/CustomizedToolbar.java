/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/* based on the code from the Pumpernickel project under the following licence: */
/*
 * This software is released as part of the Pumpernickel project.
 *
 * All com.pump resources in the Pumpernickel project are distributed under the
 * MIT License:
 * https://raw.githubusercontent.com/mickleness/pumpernickel/master/License.txt
 *
 * More information about the Pumpernickel project is available here:
 * https://mickleness.github.io/pumpernickel/
 */
package org.lecturestudio.swing.components.toolbar;

import org.lecturestudio.core.controller.ToolController;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import static java.util.Objects.nonNull;

public class CustomizedToolbar extends JPanel {

	private static final long serialVersionUID = -5451068649494911490L;

	protected static final String DIALOG_ACTIVE = "customizeDialogActive";

	private static final String PROPERTY_TEMPORARY_CONTENTS = CustomizedToolbar.class.getName() + "#contents";

	/** This is where the order of components is stored. */
	static final Preferences prefs = Preferences.userNodeForPackage(CustomizedToolbar.class);

	/** Removes the stored layout information for a particular toolbar. */
	public static void resetPreferences(String toolbarName) {
		String base = toolbarName + ".component";
		int ctr = 0;
		String s = base + ctr;
		while (prefs.get(s, null) != null) {
			prefs.remove(s);
			ctr++;
			s = base + ctr;
		}
	}

	/** Removes the stored layout information for all toolbars. */
	public static void resetAllPreferences() {
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	/** Runs <code>updateContents()</code> in the AWT thread. */
	private final Runnable updateContentsRunnable = this::updateContents;

	class CustomizedToolbarLayout extends AnimatedLayout {

		@Override
		protected Map<JComponent, Rectangle> getDestinationMap(JComponent container) {
			Map<JComponent, Rectangle> returnValue = new HashMap<>();

			String[] contents = (String[]) getClientProperty(PROPERTY_TEMPORARY_CONTENTS);
			if (contents == null)
				contents = getContents();
			int w = getWidth();

			Component[] components = getComponents();
			Collection<Component> processed = new HashSet<>();

			int totalFlexGaps = 0;
			int minPreferredWidth = 0;
			for (String content : contents) {
				try {
					if (content.length() > 0 && content.charAt(0) == '\t') {
						// this is a flexible gap
						totalFlexGaps++;
					} else {
						JComponent comp = getComponent(content);
						Dimension d = comp.getPreferredSize();
						minPreferredWidth += d.width;
					}
				} catch (Exception ignored) {
				}
			}

			int x = 0;
			int extraSpace = w - (componentInsets.left + componentInsets.right) * contents.length - minPreferredWidth;
			if (extraSpace < 0)
				extraSpace = 0;

			for (String content : contents) {
				try {
					JComponent comp = getComponent(content);
					Dimension d;

					if (content.length() > 0 && content.charAt(0) == '\t') {
						// flexible gap:
						int width = extraSpace / totalFlexGaps;
						extraSpace = extraSpace - width;
						totalFlexGaps--;

						d = new Dimension(width, minimumHeight);
					} else {
						d = comp.getPreferredSize();
					}
					if (comp instanceof JSeparator)
						d.height = minimumHeight;

					boolean contains = false;
					for (Component component : components) {
						if (component == comp) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						if (draggingComponent != null && hideActiveComponents) {
							boolean show = !comp.getName().equals(draggingComponent);
							comp.setVisible(show);
						}
						add(comp);
					}

					Rectangle bounds = new Rectangle(x + componentInsets.left, componentInsets.top + minimumHeight / 2 - d.height / 2, d.width, d.height);
					returnValue.put(comp, bounds);
					x += d.width + componentInsets.left + componentInsets.right;
					processed.add(comp);
				} catch (NullPointerException e) {
					// this may get thrown if getComponent(name) yields no component
					// this may happen if a component's name changes, or a component is removed.
				}
			}

			for (Component c : getComponents()) {
				if (!processed.contains(c)) {
					remove(c);
					repaint();
				}
			}

			return returnValue;
		}
	}

	/** The components this toolbar may display. */
	private final JComponent[] componentList;

	/** The name of the component currently being dragged. */
	public String draggingComponent;

	/**
	 * Whether a drag is originating from the toolbar, or the dialog. This can make a big difference on how the drag is treated.
	 */
	public boolean draggingFromToolbar;

	private boolean draggingIntoToolbar;

	private CustomizedToolbarOptions options;

	private JDialog dialog;

	private ResourceBundle resourceBundle;

	private ToolController toolController;

	private ButtonGroup colorGroup;

	private ButtonGroup toolGroup;

	/**
	 * The minimum height of this toolbar, based on the preferred height of all its components (whether they are visible or not).
	 */
	public int minimumHeight;

	/** The padding between each component in this toolbar. */
	private final Insets componentInsets = new Insets(4, 4, 4, 4);

	/**
	 * Controls whether we change visibility of components during DnD operations.
	 */
	protected static boolean hideActiveComponents = DragSource.isDragImageSupported();

	private static final DragSource dragSource = DragSource.getDefaultDragSource();

	private final DragSourceListener dragSourceListener = new DragSourceAdapter() {
		@Override
		public void dragDropEnd(DragSourceDropEvent dsde) {
			endDrag(dsde);
		}
	};

	private final DragGestureListener dragGestureListener = new DragGestureListener() {

		public void dragGestureRecognized(DragGestureEvent dge) {
			Point p = dge.getDragOrigin();
			Component c = dge.getComponent();
			JFrame f = (JFrame) SwingUtilities.getWindowAncestor(c);
			p = SwingUtilities.convertPoint(c, p, f);

			for (JComponent jComponent : componentList) {
				if (triggerDrag(f, p, dge, jComponent))
					return;
			}
			// double-check for separators & gaps:
			for (int a = 0; a < getComponentCount(); a++) {
				if (triggerDrag(f, p, dge, (JComponent) getComponent(a)))
					return;
			}

		}

		private boolean triggerDrag(JFrame f, Point p, DragGestureEvent dge, JComponent c) {
			Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
			r = SwingUtilities.convertRectangle(c, r, f);

			if (r.contains(p)) {
				draggingFromToolbar = true;
				draggingComponent = c.getName();
				MockComponent mc = new MockComponent(c);
				Transferable transferable = new MockComponentTransferable(mc);
				BufferedImage bi = mc.getBufferedImage();
				dge.startDrag(DragSource.DefaultMoveDrop, bi, new Point(r.x - p.x, r.y - p.y), transferable, dragSourceListener);
				return true;
			}
			return false;
		}

	};

	private final DropTargetListener dropTargetListener = new DropTargetListener() {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			if (!draggingFromToolbar) {
				setVisibilityOfMockComponent(getComponent(draggingComponent), false);
			}
			dragOver(dtde);
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			updateContents(getContents(new Point(-1000, -1000)));
			if (draggingComponent != null) {
				JComponent theComponent = getComponent(draggingComponent);
				Rectangle r = getLayout().getDestinationMap(CustomizedToolbar.this).get(theComponent);
				if (r != null) {
					theComponent.setBounds(r);
				}
				if (hideActiveComponents) {
					theComponent.setVisible(true);
				}
			}
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			if (draggingComponent == null) {
				dtde.rejectDrag();
			} else {
				Point p = dtde.getLocation();
				p = SwingUtilities.convertPoint(((DropTarget) dtde.getSource()).getComponent(), p, CustomizedToolbar.this);
				String[] contents = getContents(p);
				updateContents(contents);
				dtde.acceptDrag(DnDConstants.ACTION_MOVE);

				JComponent component = getComponent(draggingComponent);
				if (component != null) {
					component.setVisible(true);
				}
			}
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			if (draggingComponent == null) {
				dtde.rejectDrop();
			} else {
				Point p = dtde.getLocation();
				p = SwingUtilities.convertPoint(((DropTarget) dtde.getSource()).getComponent(), p, CustomizedToolbar.this);

				String[] contents = getContents(p);
				setContents(contents);
				dtde.acceptDrop(DnDConstants.ACTION_MOVE);
				JComponent theComponent = getComponent(draggingComponent);
				Rectangle r = getLayout().getDestinationMap(CustomizedToolbar.this).get(theComponent);
				if (r != null) {
					theComponent.setBounds(r);
				}
				if (hideActiveComponents && draggingIntoToolbar) {
					theComponent.setVisible(true);
					setVisibilityOfMockComponent(theComponent, false);
					if (isDraggedComponentOnlyToolInToolbar(theComponent)) {
						((AbstractButton) theComponent).doClick();
					}
				} else if (!draggingIntoToolbar) {
					setVisibilityOfMockComponent(theComponent, true);
				}
			}
			dtde.dropComplete(true);
		}

		private void setVisibilityOfMockComponent(JComponent correspondingToolbarComponent, boolean visibilityOfMockComponent) {
			int draggingComponentIndex = -1;
			for (int i = 0; i < componentList.length; i++){
				if (correspondingToolbarComponent.equals(componentList[i])) {
					draggingComponentIndex = i;
					break;
				}
			}
			if (draggingComponentIndex != -1) {
				options.componentList[draggingComponentIndex].setVisible(visibilityOfMockComponent);
				dialog.pack();
				if (visibilityOfMockComponent) {
					boolean removedSelectedToolFromToolbar = toolGroup.getSelection().equals(((AbstractButton) correspondingToolbarComponent).getModel());
					if (isDraggedComponentOnlyToolInToolbar(correspondingToolbarComponent)) {
						toolGroup.clearSelection();
						colorGroup.clearSelection();
						for (Iterator<AbstractButton> iterator = colorGroup.getElements().asIterator(); iterator.hasNext();) {
							iterator.next().setEnabled(false);
						}
						toolController.setTool(null);
					} else if (removedSelectedToolFromToolbar) {
						AbstractButton nextToolToSelect = null;
						for (Iterator<AbstractButton> iterator = toolGroup.getElements().asIterator(); iterator.hasNext();) {
							AbstractButton toolGroupElement = iterator.next();
							if (toolGroupElement.isVisible() && !toolGroupElement.equals(correspondingToolbarComponent)){
								nextToolToSelect = toolGroupElement;
								break;
							}
						}
						if (nonNull(nextToolToSelect)){
							nextToolToSelect.doClick();
						}
					}
				}
			}
		}

		private boolean isDraggedComponentOnlyToolInToolbar(JComponent draggedComponent) {
			boolean onlyToolInToolbar = false;
			for (Iterator<AbstractButton> iterator = toolGroup.getElements().asIterator(); iterator.hasNext();) {
				AbstractButton toolGroupElement = iterator.next();
				if (toolGroupElement.isVisible() && toolGroupElement.isEnabled() && !toolGroupElement.equals(draggedComponent)) {
					onlyToolInToolbar = false;
					break;
				}
				if (toolGroupElement.equals(draggedComponent)) {
					onlyToolInToolbar = true;
				}
			}
			return onlyToolInToolbar;
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}
	};

	/**
	 * Returns the contents (by name, in order of appearance) after factoring in the point that the mouse is current at point p.
	 * (This means the component that is currently being dragged will offset everything and appear near point p.)
	 */
	private String[] getContents(Point p) {
		if (draggingComponent == null) {
			return getContents();
		}

		Rectangle toolbarBounds = new Rectangle(0, 0, getWidth(), getHeight());

		boolean verticallyInside = p.y >= 0 && p.y <= toolbarBounds.height;

		draggingIntoToolbar = verticallyInside;

		String[] order = getContents();

		if ((!verticallyInside) && !draggingFromToolbar) {
			return order;
		}

		int a = 0;
		Component theComponent = getComponent(draggingComponent);
		if (hideActiveComponents) {
			theComponent.setVisible(false);
		}
		while (a < order.length) {
			if (order[a].equals(draggingComponent)) {
				order = remove(order, a);
			} else {
				a++;
			}
		}

		if ((!verticallyInside) && draggingFromToolbar) {
			return order;
		}

		for (a = 0; a < order.length; a++) {
			JComponent c = getComponent(order[a]);
			Rectangle r = c.getBounds();
			if (p.x < r.x + r.width / 2) {
				order = insert(order, draggingComponent, a);
				return order;
			}
		}

		order = insert(order, draggingComponent, order.length);
		return order;
	}

	/**
	 * Removes an element from an array.
	 * <P>
	 * This returns an array of type array.getClass(), not necessarily an array
	 * of type Object.
	 *
	 * @param array the array to remove an element from
	 * @param index the index of the element to remove
	 * @return the new array that is 1 unit smaller than the argument array.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T[] remove(T[] array, int index) {
		Class<?> cl = array.getClass().getComponentType();
		Object[] newArray = (Object[]) Array.newInstance(cl, array.length - 1);
		System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
		return (T[]) newArray;
	}

	/**
	 * Adds an element to an array.
	 * <P>
	 * This returns an array of type array.getClass(), not necessarily an array
	 * of type Object.
	 *
	 * @param array the array to add an element to
	 * @param newObject the object to add
	 * @param index the index to insert at.
	 * @return the new array that is 1 unit larger than the argument array.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T[] insert(T[] array, Object newObject, int index) {
		Class<?> cl = array.getClass().getComponentType();
		Object[] newArray = (Object[]) Array.newInstance(cl, array.length + 1);
		System.arraycopy(array, 0, newArray, 0, index);
		newArray[index] = newObject;
		System.arraycopy(array, index, newArray, index + 1, array.length - index);
		return (T[]) newArray;
	}

	/**
	 * Creates a new CustomizedToolbar.
	 *
	 * @param components
	 *            the components that may be in this toolbar.
	 *            <P>
	 *            Each component must have a unique name; this is how the order
	 *            of each component is stored between sessions.
	 * @param defaults
	 *            the default order of the components. This array should contain
	 *            the names of the components in the previous argument.
	 *            <P>
	 *            These defaults are used when there are no preferences to
	 *            indicate how to lay out this toolbar.
	 *            <P>
	 *            Special/reserved names include:
	 *            <ul>
	 *            <LI>"-": used to indicate a JSeparator.</li>
	 *            <LI>" ": used to indicate a regular gap.</li>
	 *            <LI>"\t": used to indicate a flexible gap.</LI>
	 *            </ul>
	 *            <P>
	 *            You can use as many of these special names as you like, but
	 *            actual components in the previous argument should only be
	 *            referenced once.
	 * @param toolbarName
	 *            a unique name for this type of toolbar. This name serves as a
	 *            key in the preferences to retrieve the order of the components
	 *            in this toolbar. Most applications will only have 1 type of
	 *            customized toolbar. Apple's Mail application has at least 2
	 *            toolbars: one in the main window, and one when you compose a
	 *            new message. So each type of window should use a unique name.
	 *            If this is null then no preferences will be consulted.
	 */
	public CustomizedToolbar(JComponent[] components, String[] defaults, String toolbarName) {
		super();
		setLayout(new CustomizedToolbarLayout());

		int separatorCtr = 0;
		int spaceCtr = 0;
		int flexCtr = 0;
		for (int a = 0; a < defaults.length; a++) {
			switch (defaults[a]) {
				case "-":
					defaults[a] = "-" + separatorCtr;
					separatorCtr++;
					break;
				case " ":
					defaults[a] = " " + spaceCtr;
					spaceCtr++;
					break;
				case "\t":
					defaults[a] = "\t" + flexCtr;
					flexCtr++;
					break;
			}
		}

		minimumHeight = getMinimumHeight(components);

		setName(toolbarName);
		String base = getName() + ".component";
		if (isEmpty()) {
			for (int a = 0; a < defaults.length; a++) {
				if (defaults[a] == null)
					throw new NullPointerException("defaults[" + a + "] is null");
				prefs.put(base + a, defaults[a]);
			}
		}
		setMinimumSize(new Dimension(5, minimumHeight + componentInsets.top + componentInsets.bottom));
		setPreferredSize(new Dimension(5, minimumHeight + componentInsets.top + componentInsets.bottom));
		setMaximumSize(new Dimension(5, minimumHeight + componentInsets.top + componentInsets.bottom));

		componentList = new JComponent[components.length];
		Map<String, JComponent> componentMap = new HashMap<>();
		System.arraycopy(components, 0, componentList, 0, components.length);
		for (JComponent component : componentList) {
			component.setVisible(false);
			componentMap.put(component.getName(), component);
		}

		int ctr = 0;
		String componentName = prefs.get(base + ctr, null);
		while (componentName != null) {
			JComponent component = componentMap.get(componentName);
			if (component != null)
				component.setVisible(true);
			ctr++;
			componentName = prefs.get(base + ctr, null);
		}

		// This listens possibly updates these components if the preferences change.
		PreferenceChangeListener prefListener = evt -> SwingUtilities.invokeLater(updateContentsRunnable);
		prefs.addPreferenceChangeListener(prefListener);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateContents();
			}
		});

		if (SwingUtilities.isEventDispatchThread()) {
			updateContentsRunnable.run();
		} else {
			SwingUtilities.invokeLater(updateContentsRunnable);
		}

		addHierarchyListener(e -> updateContents());
	}

	public CustomizedToolbar(JComponent[] components, String[] defaults, String toolbarName,
							 ResourceBundle resourceBundle, ToolController toolController,
							 ButtonGroup colorGroup, ButtonGroup toolGroup) {
		this(components, defaults, toolbarName);
		this.resourceBundle = resourceBundle;
		this.toolController = toolController;
		this.colorGroup = colorGroup;
		this.toolGroup = toolGroup;
	}

	private static int getMinimumHeight(JComponent[] components) {
		int h = 0;
		for (JComponent component : components) {
			h = Math.max(component.getPreferredSize().height, h);
			h = Math.max(component.getHeight(), h);
		}
		return h;
	}

	/**
	 * Returns the components that may or may not be visible in this toolbar, depending on how the user has configured this toolbar.
	 */
	public JComponent[] getPossibleComponents() {
		JComponent[] array = new JComponent[componentList.length];
		System.arraycopy(componentList, 0, array, 0, componentList.length);
		return array;
	}

	@Override
	public CustomizedToolbarLayout getLayout() {
		return (CustomizedToolbarLayout) super.getLayout();
	}

	protected void endDrag(DragSourceDropEvent e) {
		if (draggingComponent != null) {
			Point p = e.getLocation();
			SwingUtilities.convertPointFromScreen(p, this);
		}
		draggingComponent = null;
	}

	private boolean isEmpty() {
		String base = getName() + ".component";
		String s = prefs.get(base + "0", null);
		return s == null;
	}

	private void updateContents() {
		updateContents(getContents());
	}

	private void updateContents(String[] contents) {
		putClientProperty(PROPERTY_TEMPORARY_CONTENTS, contents);
		getLayout().layoutContainer(this);
	}

	/**
	 * Returns the component names (in order of appearance) that this toolbar should display from the preferences.
	 */
	private String[] getContents() {
		List<String> v = new ArrayList<>();
		String base = getName() + ".component";
		String s;
		int ctr = 0;
		while ((s = prefs.get(base + ctr, null)) != null) {
			v.add(s);
			ctr++;
		}

		return v.toArray(new String[0]);
	}

	/**
	 * Stores the component names (in order of appearance) that this toolbar should display in the preferences.
	 */
	private void setContents(String[] array) {
		String base = getName() + ".component";
		for (int a = 0; a < array.length; a++) {
			prefs.put(base + a, array[a]);
		}

		int ctr = array.length;
		while (prefs.get(base + ctr, null) != null) {
			prefs.remove(base + ctr);
			ctr++;
		}
	}

	/** Returns an unused name for a new separator. */
	protected String getNewSeparatorName() {
		int min = 0;
		search: while (true) {
			for (int a = 0; a < getComponentCount(); a++) {
				String name = getComponent(a).getName();
				if (name.equals("-" + min)) {
					min++;
					continue search;
				}
			}
			return "-" + min;
		}
	}

	/** Returns an unused name for a new space. */
	protected String getNewSpaceName() {
		int min = 0;
		search: while (true) {
			for (int a = 0; a < getComponentCount(); a++) {
				String name = getComponent(a).getName();
				if (name.equals(" " + min)) {
					min++;
					continue search;
				}
			}
			return " " + min;
		}
	}

	/** Returns an unused name for a new flexible space. */
	protected String getNewFlexibleSpaceName() {
		int min = 0;
		search: while (true) {
			for (int a = 0; a < getComponentCount(); a++) {
				String name = getComponent(a).getName();
				if (name.equals("\t" + min)) {
					min++;
					continue search;
				}
			}
			return "\t" + min;
		}
	}

	/**
	 * Gets the component that has a certain name.
	 * <P>
	 * The argument should be a name of a component that was passed when you
	 * created this toolbar, or one of the special names: "-", " ", "\t"
	 */
	protected JComponent getComponent(String name) {
		if (name == null)
			throw new NullPointerException();

		for (JComponent jComponent : componentList) {
			if (jComponent.getName().equals(name))
				return jComponent;
		}
		// the rest of this method deals with separators and gaps
		for (int a = 0; a < getComponentCount(); a++) {
			if (getComponent(a).getName().equals(name))
				return (JComponent) getComponent(a);
		}
		if (name.length() > 0 && name.charAt(0) == '-') {
			JSeparator newSeparator = new JSeparator(SwingConstants.VERTICAL);
			newSeparator.setUI(new MacToolbarSeparatorUI());
			newSeparator.setName(name);
			return newSeparator;
		} else if (name.length() > 0 && name.charAt(0) == ' ') {
			SpaceComponent space = new SpaceComponent(this, false);
			space.setName(name);
			return space;
		} else if (name.length() > 0 && name.charAt(0) == '\t') {
			SpaceComponent space = new SpaceComponent(this, true);
			space.setName(name);
			return space;
		}
		throw new NullPointerException("No component \"" + name + "\"");
	}

	/**
	 * Displays the dialog that lets the user customize this component.
	 * <P>
	 * This covers the underlying JFrame in a transparent sheet so the toolbar
	 * can also send/receive drag and drop events appropriately, and to block
	 * out the rest of the GUI so the dialog appears modal. (It is not
	 * technically modal, because otherwise we would have trouble interacting
	 * with the toolbar underneath.)
	 *
	 * @param dialogMaxWidth
	 *            this is the width used to decide when to wrap rows of
	 *            components. Depending on the size of your toolbar, and the
	 *            platform, you may want to change this number.
	 */
	public void displayDialog(int dialogMaxWidth) {
		final JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		if (parent != null) {
			putClientProperty(DIALOG_ACTIVE, Boolean.TRUE); // Make spaces paint themselves

			options = new CustomizedToolbarOptions(this, dialogMaxWidth, resourceBundle);

			final JComponent modalCover = new JComponent() {
				private static final long serialVersionUID = -3681053092932147810L;
			};
			dragSource.createDefaultDragGestureRecognizer(modalCover, DnDConstants.ACTION_COPY_OR_MOVE, dragGestureListener);
			new DropTarget(modalCover, dropTargetListener);

			// Having a MouseListener -- even if its empty -- prevents MouseEvents from leaking through to components underneath.
			modalCover.addMouseListener(new MouseAdapter() {
			});
			Dimension windowSize = parent.getSize();
			modalCover.setSize(windowSize);
			final JLayeredPane layeredPane = parent.getLayeredPane();
			layeredPane.add(modalCover);

			// Must be non-modal, so you can interact with the toolbar underneath.
			dialog = new JDialog(parent, false);
			dialog.getContentPane().add(options);
			dialog.setUndecorated(true);
			options.setBorder(new LineBorder(Color.gray));
			dialog.pack();

			layeredPane.setLayer(modalCover, JLayeredPane.MODAL_LAYER);
			FakeSheetWindowListener windowListener = new FakeSheetWindowListener(parent, dialog, this, modalCover);
			parent.addComponentListener(windowListener);
			dialog.getRootPane().setDefaultButton(options.closeButton);
			AbstractAction dialogCloseAction = new AbstractAction() {
				private static final long serialVersionUID = 6919540407036089155L;

				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			};
			windowListener.repositionDialog();
			options.closeButton.addActionListener(dialogCloseAction);
			dialog.setVisible(true);
			dialog.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					layeredPane.remove(modalCover);
					putClientProperty(DIALOG_ACTIVE, Boolean.FALSE);
				}
			});

			KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
			dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, escapeKey);
			dialog.getRootPane().getActionMap().put(escapeKey, dialogCloseAction);
		}
	}
}
