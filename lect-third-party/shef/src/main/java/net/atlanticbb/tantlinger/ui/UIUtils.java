/*
 * Created on Oct 30, 2007
 */
package net.atlanticbb.tantlinger.ui;

import java.awt.*;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.formdev.flatlaf.util.UIScale;

import com.kitfox.svg.app.beans.SVGIcon;

import org.bushe.swing.action.ActionUIFactory;

/**
 * A collection of static UI helper methods.
 *
 * @author Bob Tantlinger
 */
public class UIUtils {
    /**
     * The icon size
     */
    public static final int ICON_SIZE = (int) (30 * UIScale.getUserScaleFactor());

    /**
     * The button size
     */
    public static final int BUTTON_SIZE = ICON_SIZE+4;

    /**
     * The icons' path
     */
    private static final String ICONS_PATH = "resources/gfx/icons/";

    /**
     * Gets the icon with the specified name
     * @param iconName Icon's filename
     * @return Icon, or null if it doesn't exist
     */
    public static ImageIcon getIcon(String iconName) {
        return getIcon(iconName, ICON_SIZE);
    }

    /**
     * Gets the icon with the specified name and size
     * @param iconName Icon filename
     * @param size Icon's size
     * @return Icon, or null if it doesn't exist
     */
    public static ImageIcon getIcon(String iconName, int size) {
        final String filePath = ICONS_PATH + iconName;
        try {
            URL url = ClassLoader.getSystemResource(filePath);

            SVGIcon svgicon = new SVGIcon();
            svgicon.setAntiAlias(true);
            svgicon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
            svgicon.setSvgURI(url.toURI());

            svgicon.setPreferredSize(new Dimension(size, size));

            return svgicon;
        } catch (Exception e) {
            return null;
        }
    }

    public static AbstractButton addToolBarButton(JToolBar tb, Action a) {
        return addToolBarButton(tb, ActionUIFactory.getInstance().createButton(a));
    }

    public static AbstractButton addToolBarButton(JToolBar tb, Action a, boolean focusable, boolean showIconOnly) {
        return addToolBarButton(tb, ActionUIFactory.getInstance().createButton(a), false, true);
    }

    public static AbstractButton addToolBarButton(JToolBar tb, AbstractButton button) {
        return addToolBarButton(tb, button, false, true);
    }

    public static AbstractButton addToolBarButton(JToolBar tb, AbstractButton button, boolean focusable,
                                                  boolean showIconOnly) {
        if (button.getAction() != null) {
            button.setToolTipText((String) button.getAction().getValue(Action.NAME));
            //prefer large icons for toolbar buttons
            if (button.getAction().getValue("LARGE_ICON") != null) {
                try {
                    button.setIcon((Icon) button.getAction().getValue("LARGE_ICON"));
                } catch (ClassCastException cce) {
                }
            }
        }

        Icon ico = button.getIcon();
        if (ico != null && showIconOnly) {
            button.setText(null);
            button.setMnemonic(0);
            button.putClientProperty("hideActionText", Boolean.TRUE);
            int square = Math.max(ico.getIconWidth(), ico.getIconHeight()) + 6;
            Dimension size = new Dimension(square, square);
            button.setPreferredSize(size);
            //button.setMinimumSize(size);
            //button.setMaximumSize(size);
        }

        if (!focusable) {
            button.setFocusable(false);
            button.setFocusPainted(false);
        }

        button.setMargin(new Insets(1, 1, 1, 1));
        tb.add(button);
        return button;
    }

    public static JMenuItem addMenuItem(JMenu menu, Action action) {
        JMenuItem item = menu.add(action);
        configureMenuItem(item, action);
        return item;
    }

    public static JMenuItem addMenuItem(JPopupMenu menu, Action action) {
        JMenuItem item = menu.add(action);
        configureMenuItem(item, action);
        return item;
    }

    private static void configureMenuItem(JMenuItem item, Action action) {
        KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (keystroke != null)
            item.setAccelerator(keystroke);

        item.setIcon(null);
        item.setToolTipText(null);
    }
}
