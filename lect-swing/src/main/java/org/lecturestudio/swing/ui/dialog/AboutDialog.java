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

package org.lecturestudio.swing.ui.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.swing.components.TableDummyHeader;

public class AboutDialog extends AbstractDialog<Void> {

	private static final long serialVersionUID = 6461713602431232684L;

	private String appName;
	private String description;
	private String version;
	private String concept;
	private String appVersion;

	private String[] developers;

	private Font versionFont;
	
	private int[] versionPos;
	
	private Icon icon;


	public AboutDialog(Frame parent, ApplicationContext context) {
		super(parent, context);

		init();
	}

	private void init() {
		setTitle(dict.get("menu.about"));
		setLayout(new BorderLayout(0, 0));
		setModal(true);
		setResizable(false);
		
		versionFont = getFont().deriveFont(Font.BOLD);
	}

	protected void initComponents() {
		JLabel imageLabel = new JLabel(icon) {

			private static final long serialVersionUID = 5507494863136842376L;

			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;

				getIcon().paintIcon(this, g2, 0, 0);
				
				if (appVersion == null || versionPos == null)
					return;
				
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setFont(versionFont);
				g2.setColor(Color.WHITE);
				g2.drawString(appVersion, versionPos[0], versionPos[1]);
			}
			
		};

		imageLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
		getContentPane().add(imageLabel, "West");

		initBottomPanel();

		pack();
		centerToScreen();
	}

	private void initBottomPanel() {
		final JPanel devPanel = createDeveloperPanel();
		devPanel.setVisible(false);

		final JPanel systemPanel = createSystemPanel();
		systemPanel.setPreferredSize(new Dimension(300, 190));
		systemPanel.setVisible(false);

		final JPanel cardPanel = new JPanel(new CardLayout());
		cardPanel.setOpaque(false);

		final ButtonGroup group = new ButtonGroup();

		JToggleButton devButton = new JToggleButton(dict.get("about.dialog.persons"));
		devButton.addActionListener(e -> {
			cardPanel.removeAll();

			if (devPanel.isVisible()) {
				group.clearSelection();
			}
			else {
				cardPanel.add(devPanel, "developers");
			}

			systemPanel.setVisible(false);
			devPanel.setVisible(!devPanel.isVisible());
			pack();
		});

		JToggleButton systemButton = new JToggleButton(dict.get("about.dialog.system"));
		systemButton.addActionListener(e -> {
			cardPanel.removeAll();

			if (systemPanel.isVisible()) {
				group.clearSelection();
			}
			else {
				cardPanel.add(systemPanel, "system");
			}

			devPanel.setVisible(false);
			systemPanel.setVisible(!systemPanel.isVisible());
			pack();
		});
		group.add(devButton);
		group.add(systemButton);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder());
		buttonPanel.setOpaque(false);
		buttonPanel.add(devButton);
		buttonPanel.add(systemButton);

		bottomPanel.add(buttonPanel, "North");
		bottomPanel.add(cardPanel, "South");

		getContentPane().add(bottomPanel, "South");
	}

	private JPanel createDeveloperPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(20, 5));

		JTextPane devTextPane = new JTextPane();
		devTextPane.setEditable(false);
		devTextPane.setOpaque(false);
		devTextPane.setMargin(new Insets(10, 10, 10, 10));

		String newLine = System.getProperty("line.separator");

		StyledDocument document = devTextPane.getStyledDocument();

		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setBold(attributes, true);
		StyleConstants.setForeground(attributes, Color.DARK_GRAY);
		try {
			StyleConstants.setFontSize(attributes, 12);
			StyleConstants.setBold(attributes, true);
			document.insertString(document.getLength(), dict.get("about.dialog.developers"), attributes);

			attributes = new SimpleAttributeSet();
			StyleConstants.setSpaceBelow(attributes, 3.0F);
			document.setParagraphAttributes(document.getLength(), 1, attributes, false);

			for (String developer : developers) {
				document.insertString(document.getLength(), newLine + developer, null);
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}

		panel.add(devTextPane, "Center");
		
		devTextPane = new JTextPane();
		devTextPane.setEditable(false);
		devTextPane.setOpaque(false);
		devTextPane.setMargin(new Insets(10, 10, 10, 10));
		devTextPane.setPreferredSize(new Dimension(300, 120));

		document = devTextPane.getStyledDocument();

		attributes = new SimpleAttributeSet();
		StyleConstants.setBold(attributes, true);
		StyleConstants.setForeground(attributes, Color.DARK_GRAY);
		try {
			StyleConstants.setFontSize(attributes, 12);
			StyleConstants.setBold(attributes, true);
			document.insertString(document.getLength(), dict.get("about.dialog.concept"), attributes);

			attributes = new SimpleAttributeSet();
			StyleConstants.setSpaceBelow(attributes, 3.0F);
			document.setParagraphAttributes(document.getLength(), 1, attributes, false);

			document.insertString(document.getLength(), newLine + concept, null);
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		panel.add(devTextPane, BorderLayout.EAST);

		return panel;
	}

	private JPanel createSystemPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(200, 200));

		Properties props = System.getProperties();
		Enumeration<?> e = props.propertyNames();

		String[] columns = { "key", "value" };
		String[][] rowData = new String[props.size()][2];

		for (int i = 0; e.hasMoreElements(); i++) {
			String key = (String) e.nextElement();
			String value = (String) props.get(key);

			rowData[i][0] = key;
			rowData[i][1] = value;
		}

		DefaultTableModel tableModel = new DefaultTableModel() {

			private static final long serialVersionUID = 4837096722530627372L;


			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};
		tableModel.setDataVector(rowData, columns);

		JTable systemTable = new JTable(tableModel);
		systemTable.setBorder(new EmptyBorder(0, 0, 0, 0));
		systemTable.setFillsViewportHeight(true);
		systemTable.getTableHeader().setReorderingAllowed(false);

		TableDummyHeader dummyHeader = new TableDummyHeader(systemTable);

		JScrollPane scrollPane = new JScrollPane(systemTable);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setCorner("UPPER_RIGHT_CORNER", dummyHeader);

		panel.add(scrollPane, "Center");

		return panel;
	}

	public void setAppName(String appName) {
		this.appName = appName;

		setTitle(dict.get("menu.about") + " " + appName);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setVersion(String version) {
		this.version = version;
		this.appVersion = dict.get("about.dialog.version") + " " + version;
	}
	
	public void setVersionPosition(int[] position) throws Exception {
		if (position.length < 2)
			throw new Exception("Position requires a [x] and [y] value.");
		
		this.versionPos = position;
	}
	
	public void setConcept(String concept) {
		this.concept = concept;
	}

	public void setDevelopers(String[] developers) {
		this.developers = developers;
	}

	public void setImage(Image image) {
		this.icon = new ImageIcon(image);
	}
	
	public Icon getImage() {
		return icon;
	}
	
	public int getVersionStringWidth() {
		FontMetrics fontMetrics = new JLabel().getFontMetrics(versionFont);
		return fontMetrics.stringWidth(appVersion);
	}

	public Void open() {
		initComponents();
		setVisible(true);

		return null;
	}

}
