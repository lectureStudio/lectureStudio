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

package org.lecturestudio.swing.swixml;

import static java.util.Objects.nonNull;

import java.awt.Container;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.table.TableColumn;

import net.atlanticbb.tantlinger.shef.HTMLEditorPane;

import org.lecturestudio.core.inject.Injector;
import org.lecturestudio.swing.components.CameraPreviewPanel;
import org.lecturestudio.swing.components.ColorChooserButton;
import org.lecturestudio.swing.components.DisplayPanel;
import org.lecturestudio.swing.components.FontPickerButton;
import org.lecturestudio.swing.components.IPTextField;
import org.lecturestudio.swing.components.LevelMeter;
import org.lecturestudio.swing.components.MessageView;
import org.lecturestudio.swing.components.previews.ArrowToolPreview;
import org.lecturestudio.swing.components.previews.EllipseToolPreview;
import org.lecturestudio.swing.components.previews.LineToolPreview;
import org.lecturestudio.swing.components.previews.PenToolPreview;
import org.lecturestudio.swing.components.previews.PointerToolPreview;
import org.lecturestudio.swing.components.previews.RectangleToolPreview;
import org.lecturestudio.swing.components.RecordButton;
import org.lecturestudio.swing.components.SettingsTab;
import org.lecturestudio.swing.components.SlideView;
import org.lecturestudio.swing.components.TeXFontPickerButton;
import org.lecturestudio.swing.components.TitledSeparator;
import org.lecturestudio.swing.components.ToggleComboButton;
import org.lecturestudio.swing.components.ToolColorPickerButton;
import org.lecturestudio.swing.components.ToolGroupButton;
import org.lecturestudio.swing.swixml.converter.IconConverter;
import org.lecturestudio.swing.swixml.factory.AbstractButtonFactory;
import org.lecturestudio.swing.swixml.factory.AbstractInjectButtonFactory;
import org.lecturestudio.swing.swixml.factory.InjectViewFactory;
import org.lecturestudio.swing.swixml.processor.ComboBoxProcessor;
import org.lecturestudio.swing.swixml.processor.PanelProcessor;
import org.lecturestudio.swing.swixml.processor.TabProcessor;
import org.lecturestudio.swing.swixml.processor.TabbedPaneProcessor;
import org.lecturestudio.swing.swixml.processor.TableColumnProcessor;
import org.lecturestudio.swing.swixml.processor.TableProcessor;
import org.lecturestudio.swing.swixml.processor.TextFieldProcessor;
import org.lecturestudio.swing.swixml.processor.TreeProcessor;
import org.lecturestudio.swing.table.ButtonEditor;
import org.lecturestudio.swing.table.ButtonRenderer;

import org.swixml.ConverterLibrary;
import org.swixml.Localizer;
import org.swixml.SwingEngine;
import org.swixml.SwingTagLibrary;
import org.swixml.TagLibrary;
import org.swixml.factory.BeanFactory;

public class ViewLoader<T extends Container> extends SwingEngine<T> {

	static {
		SwingEngine.setMacOSXSuport(false);

		ConverterLibrary converterLibrary = ConverterLibrary.getInstance();
		converterLibrary.register(Icon.class, new IconConverter());
		converterLibrary.register(ImageIcon.class, new IconConverter());

		TagLibrary tagLibrary = SwingTagLibrary.getInstance();
		tagLibrary.registerTag("ArrowToolPreview", ArrowToolPreview.class);
		tagLibrary.registerTag("CameraPreviewPanel", CameraPreviewPanel.class);
		tagLibrary.registerTag("ColorChooserButton", ColorChooserButton.class);
		tagLibrary.registerTag("ComboBox", new BeanFactory(JComboBox.class, new ComboBoxProcessor()));
		tagLibrary.registerTag("DisplayPanel", DisplayPanel.class);
		tagLibrary.registerTag("EllipseToolPreview", EllipseToolPreview.class);
		tagLibrary.registerTag("IPTextField", IPTextField.class);
		tagLibrary.registerTag("LevelMeter", LevelMeter.class);
		tagLibrary.registerTag("LineToolPreview", LineToolPreview.class);
		tagLibrary.registerTag("MessageView", MessageView.class);
		tagLibrary.registerTag("Panel", new BeanFactory(JPanel.class, new PanelProcessor()));
		tagLibrary.registerTag("PenToolPreview", PenToolPreview.class);
		tagLibrary.registerTag("PointerToolPreview", PointerToolPreview.class);
		tagLibrary.registerTag("RecordButton", RecordButton.class);
		tagLibrary.registerTag("RectangleToolPreview", RectangleToolPreview.class);
		tagLibrary.registerTag("SlideView", SlideView.class);
		tagLibrary.registerTag("Tab", new BeanFactory(SettingsTab.class, new TabProcessor()));
		tagLibrary.registerTag("TabbedPane", new BeanFactory(JTabbedPane.class, new TabbedPaneProcessor()));
		tagLibrary.registerTag("Table", new BeanFactory(JTable.class, new TableProcessor()));
		tagLibrary.registerTag("TableColumn", new BeanFactory(TableColumn.class, new TableColumnProcessor()));
		tagLibrary.registerTag("TableButtonEditor", ButtonEditor.class);
		tagLibrary.registerTag("TableButtonRenderer", ButtonRenderer.class);
		tagLibrary.registerTag("TextField", new BeanFactory(JTextField.class, new TextFieldProcessor()));
		tagLibrary.registerTag("TitledSeparator", TitledSeparator.class);
		tagLibrary.registerTag("ToggleComboButton", ToggleComboButton.class);
		tagLibrary.registerTag("ToolGroupButton", ToolGroupButton.class);
		tagLibrary.registerTag("Tree", new BeanFactory(JTree.class, new TreeProcessor()));
	}

	private final Localizer localizer;


	public ViewLoader(T client) {
		this(client, null);
	}

	public ViewLoader(T client, ResourceBundle resourceBundle) {
		this(client, resourceBundle, null);
	}

	public ViewLoader(T client, ResourceBundle resourceBundle, Injector injector) {
		super(client);

		if (nonNull(resourceBundle)) {
			localizer = new ViewLocalizer(resourceBundle);
		}
		else {
			localizer = null;
		}

		TagLibrary tagLibrary = SwingTagLibrary.getInstance();
		tagLibrary.registerTag("Button", new AbstractButtonFactory(JButton.class));
		tagLibrary.registerTag("ToggleButton", new AbstractButtonFactory(JToggleButton.class));
		tagLibrary.registerTag("RadioButton", new AbstractButtonFactory(JRadioButton.class));
		tagLibrary.registerTag("HTMLEditor", new InjectViewFactory(HTMLEditorPane.class, injector));
		tagLibrary.registerTag("FontPickerButton", new AbstractInjectButtonFactory(FontPickerButton.class, injector));
		tagLibrary.registerTag("TeXFontPickerButton", new AbstractInjectButtonFactory(TeXFontPickerButton.class, injector));
		tagLibrary.registerTag("ToolColorPickerButton", new AbstractInjectButtonFactory(ToolColorPickerButton.class, injector));
	}

	@Override
	public Localizer getLocalizer() {
		return nonNull(localizer) ? localizer : super.getLocalizer();
	}

	@Override
	public void setLocale(Locale locale) {
		getLocalizer().setLocale(locale);
	}

	@Override
	public void setResourceBundle(String bundlename) {
		getLocalizer().setResourceBundle(bundlename);
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
		getLocalizer().setClassLoader(cl);
	}
}
