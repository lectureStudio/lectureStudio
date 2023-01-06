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

package org.lecturestudio.swing.components;

import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import org.lecturestudio.swing.list.FontCellRenderer;

public class FontChooser extends JPanel {

	private JLabel previewLabel;

	private JList<Integer> styleList;

	private JList<Integer> sizeList;

	private JCheckBox strikeCheck;

	private JCheckBox underlineCheck;

	private JButton okButton;

	private JButton cancelButton;

	private Font font;


	public FontChooser(ResourceBundle resources) {
		super();

		initialize(resources);
		setSelectedFont(new Font("Arial", Font.PLAIN, 24));
	}

	public void setSelectedFont(Font font) {
		this.font = font;

		Map<TextAttribute, ?> attributes = font.getAttributes();

		styleList.setSelectedValue(font.getStyle(), true);
		sizeList.setSelectedValue(font.getSize(), true);
		strikeCheck.setSelected(attributes.get(TextAttribute.STRIKETHROUGH)
				== TextAttribute.STRIKETHROUGH_ON);
		underlineCheck.setSelected(attributes.get(TextAttribute.UNDERLINE)
				== TextAttribute.UNDERLINE_ON);
	}

	public Font getSelectedFont() {
		return font;
	}

	public void setOnOk(ActionListener listener) {
		okButton.addActionListener(listener);
	}

	public void setOnCancel(ActionListener listener) {
		cancelButton.addActionListener(listener);
	}

	private void initialize(ResourceBundle dict) {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.lightGray),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));

		previewLabel = new JLabel();
		previewLabel.setText("AaBbYyZz");
		previewLabel.setVerticalTextPosition(JLabel.CENTER);
		previewLabel.setHorizontalTextPosition(JLabel.CENTER);
		previewLabel.setHorizontalAlignment(JLabel.CENTER);
		previewLabel.setBorder(new TitledBorder(dict.getString("toolbar.font.preview")));
		previewLabel.setMinimumSize(new Dimension(400, 100));
		previewLabel.setPreferredSize(new Dimension(400, 100));

		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new GridBagLayout());

		final JList<String> fontList = new JList<>();
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.addListSelectionListener(e -> fontSelectionChanged(fontList.getSelectedValue()));
		fontList.setCellRenderer(new FontCellRenderer());

		final JScrollPane fontScrollPane = new JScrollPane();
		fontScrollPane.setViewportView(fontList);

		// Load fonts asynchronously.
		SwingWorker<String[], Void> worker = new SwingWorker<>() {

			@Override
			protected String[] doInBackground() {
				return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			}

			@Override
			protected void done() {
				try {
					fontList.setListData(get());

					if (nonNull(font)) {
						fontList.setSelectedValue(font.getFontName(), true);
					}

					fontScrollPane.setPreferredSize(new Dimension(200, 100));
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		worker.execute();

		Integer[] styles = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };

		styleList = new JList<>(styles);
		styleList.setCellRenderer(new FontStyleRenderer(dict));
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		styleList.addListSelectionListener(e -> styleSelectionChanged(styleList.getSelectedValue()));

		JScrollPane styleScrollPane = new JScrollPane();
		styleScrollPane.setViewportView(styleList);

		Integer[] sizeArray = { 8, 10, 11, 12, 14, 16, 20, 24, 28, 36, 48, 72, 96 };

		sizeList = new JList<>(sizeArray);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sizeList.addListSelectionListener(e -> sizeSelectionChanged(sizeList.getSelectedValue()));

		JScrollPane sizeScrollPane = new JScrollPane();
		sizeScrollPane.setViewportView(sizeList);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 1;
		gridBagConstraints.insets = new Insets(1, 1, 1, 10);
		gridBagConstraints.weightx = 2.0;

		fontPanel.add(fontScrollPane, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 1;
		gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 10);
		gridBagConstraints.weightx = 1.0;

		fontPanel.add(styleScrollPane, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 1;
		gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
		gridBagConstraints.weightx = 0.5;

		fontPanel.add(sizeScrollPane, gridBagConstraints);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		okButton = new JButton();
		okButton.setText(dict.getString("button.ok"));

		cancelButton = new JButton();
		cancelButton.setText(dict.getString("button.cancel"));

		Dimension okSize = okButton.getPreferredSize();
		Dimension cancelSize = cancelButton.getPreferredSize();
		Dimension commonSize = new Dimension();
		commonSize.width = Math.max(okSize.width, cancelSize.width);
		commonSize.height = Math.max(okSize.height, cancelSize.height) + 2;

		okButton.setPreferredSize(commonSize);
		cancelButton.setPreferredSize(commonSize);

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		JPanel effectPanel = new JPanel();
		effectPanel.setLayout(new GridLayout(2, 1));
		effectPanel.setBorder(new TitledBorder(dict.getString("toolbar.font.effects")));
		effectPanel.setMinimumSize(new Dimension(400, 70));
		effectPanel.setPreferredSize(new Dimension(400, 70));

		strikeCheck = new JCheckBox(dict.getString("toolbar.font.strikethrough"));
		strikeCheck.addActionListener(e -> {
			JCheckBox comp = (JCheckBox) e.getSource();
			Boolean value = comp.isSelected() ? Boolean.TRUE : Boolean.FALSE;

			Map<TextAttribute, Boolean> attributes1 = new HashMap<>();
			attributes1.put(TextAttribute.STRIKETHROUGH, value);
			font = font.deriveFont(attributes1);
			previewLabel.setFont(font);
		});

		underlineCheck = new JCheckBox(dict.getString("toolbar.font.underline"));
		underlineCheck.addActionListener(e -> {
			JCheckBox comp = (JCheckBox) e.getSource();
			Integer value = comp.isSelected() ? TextAttribute.UNDERLINE_ON : Integer.valueOf(-1);

			Map<TextAttribute, Integer> attributes12 = new HashMap<>();
			attributes12.put(TextAttribute.UNDERLINE, value);
			font = font.deriveFont(attributes12);
			previewLabel.setFont(font);
		});

		effectPanel.add(underlineCheck);
		effectPanel.add(strikeCheck);

		mainPanel.add(previewLabel, BorderLayout.NORTH);
		mainPanel.add(fontPanel, BorderLayout.CENTER);
		mainPanel.add(effectPanel, BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void fontSelectionChanged(Object value) {
		setNewFont(new Font((String) value, font.getStyle(), font.getSize()));
	}

	private void styleSelectionChanged(Integer style) {
		setNewFont(new Font(font.getFamily(), style, font.getSize()));
	}

	private void sizeSelectionChanged(Object value) {
		int size = (Integer) value;
		setNewFont(new Font(font.getFamily(), font.getStyle(), size));
	}

	private void setNewFont(Font newFont) {
		Map<TextAttribute, ?> attributes = font.getAttributes();
		Map<TextAttribute, Object> attr = new HashMap<>();
		attr.put(TextAttribute.UNDERLINE, attributes.get(TextAttribute.UNDERLINE));
		attr.put(TextAttribute.STRIKETHROUGH, attributes.get(TextAttribute.STRIKETHROUGH));

		font = newFont.deriveFont(attr);

		previewLabel.setFont(font);
	}



	private static class FontStyleRenderer extends DefaultListCellRenderer {

		private final ResourceBundle dict;


		FontStyleRenderer(ResourceBundle dict) {
			this.dict = dict;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, cellHasFocus);

			int style = (int) value;

			if (style == Font.PLAIN) {
				label.setText(dict.getString("toolbar.font.standard"));
			}
			else if (style == Font.BOLD) {
				label.setText(dict.getString("toolbar.font.bold"));
			}
			else if (style == Font.ITALIC) {
				label.setText(dict.getString("toolbar.font.italic"));
			}
			else if (style == (Font.BOLD + Font.ITALIC)) {
				label.setText(dict.getString("toolbar.font.bold-italic"));
			}

			return label;
		}
	}
}
