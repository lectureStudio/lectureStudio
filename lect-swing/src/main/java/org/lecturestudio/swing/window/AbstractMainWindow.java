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

package org.lecturestudio.swing.window;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.util.Command;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.swing.KeyboardManager;
import org.lecturestudio.swing.Shortcut;

public abstract class AbstractMainWindow extends DecoratedWindow implements MainWindow, KeyEventDispatcher {

	private final static Logger LOG = LogManager.getLogger(AbstractMainWindow.class);

	private final Map<KeyStroke, Action> shortcutMap = new HashMap<>();

	private boolean showStartPanel = true;
	

	abstract protected void initComponents();

	
	public AbstractMainWindow(String title, ApplicationContext context) {
		super(context);
		
		setTitle(title);

		initLookAndFeel();
		initComponents();
		init();
		initKeyboardManager();
	}
	
	protected boolean showStartPanel() {
		return showStartPanel;
	}
	
	protected void setShowStartPanel(boolean show) {
		this.showStartPanel = show;
	}
	
	protected boolean canScroll() {
		return getDocument() != null && !showStartPanel() && getWindow().isActive();
	}

	protected void init() {
		ApplicationBus.register(this);
		
		getWindow().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	private void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		}
		catch (Exception e) {
			LOG.error("Set system look and feel failed", e);
		}
	}
	
	private void initKeyboardManager() {
		KeyboardManager kbm = new KeyboardManager(this);
		KeyboardFocusManager kbfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kbfm.addKeyEventDispatcher(kbm);
	}

	protected void registerShortcut(Shortcut shortcut, Action listener) {
		registerShortcut(shortcut.getKeyStroke(), listener);
	}

	public void registerShortcut(KeyStroke keystroke, Action listener) {
		shortcutMap.put(keystroke, listener);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (!getWindow().isFocused()) {
			return true;
		}

		KeyStroke keystroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx());
		Action listener = shortcutMap.get(keystroke);

		if (listener != null) {
			e.consume();
			
			String actionCommand = (String) listener.getValue(Action.ACTION_COMMAND_KEY);
			if (actionCommand == null) {
				actionCommand = keystroke.toString();
			}
			ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);

			listener.actionPerformed(event);
		}

		return true;
	}

    protected void launchFile(File file, boolean pathFallback) {
        try {
            // Desktop.getDesktop().open(file); does not work for some reason
	        String filePath = file.getPath();

	        if (!file.exists()) {
		        LOG.debug("File {} does not exist, trying document relative path.", filePath);

		        // Try relative path of the opened document.
		        String docPath = getDocument().getFilePath();
		        if (docPath != null && !docPath.isEmpty()) {
			        docPath = docPath.substring(0, docPath.lastIndexOf(File.separator));
			        docPath += File.separator + filePath;

			        filePath = docPath;
		        }
	        }

	        if (OsInfo.isWindows()) {
		        if (filePath.startsWith("/") || filePath.startsWith("\\"))
			        filePath = filePath.substring(1);

		        Command.execute(filePath);
	        }
		    else if (OsInfo.isLinux())
			    Command.execute(new String[] { "xdg-open", filePath });
		    else if (OsInfo.isMac())
				Command.execute(new String[] { "open", filePath });

        }
        catch (Exception e) {
        	LOG.error("Launch file failed.", e);
        	
            // Try relative path of the opened document.
            String docPath = getDocument().getFilePath();
            if (pathFallback && docPath != null && !docPath.isEmpty()) {
                docPath = docPath.substring(0, docPath.lastIndexOf(File.separator));
                docPath += File.separator + file.getName();

                launchFile(new File(docPath), false);
            }
        }
    }

    protected void browseURI(URI uri) {
	    if (uri == null) {
			return;
		}

        try {
	        String scheme = uri.getScheme();
	        String path = uri.getPath();

	        if (scheme != null && scheme.equals("file")) {
		        launchFile(new File(path), true);
		        return;
	        }
	        // for compatibility reasons
	        if (scheme == null || !scheme.startsWith("http")) {
				path = "http://" + path;
		        uri = new URI(path);
	        }

	        if (Desktop.isDesktopSupported()) {
		        Desktop.getDesktop().browse(uri);
	        }
	        else {
		        if (OsInfo.isLinux())
			        Command.execute(new String[] { "xdg-open", path });
		        else if (OsInfo.isMac())
			        Command.execute(new String[] { "open", path });
	        }
        }
        catch (Exception e) {
        	LOG.error("Browse uri failed.", e);
        }
    }

}
