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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.lecturestudio.core.text.TeXFont;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXFormula.TeXIconBuilder;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TeXFontChooser extends JPanel {

	private JLabel previewLabel;

	private JList<TeXFont.Type> styleList;

	private JList<Integer> sizeList;

	private JButton okButton;

	private JButton cancelButton;

	private TeXFont font;


	public TeXFontChooser(ResourceBundle resources) {
		super();

		initialize(resources);
		setSelectedFont(new TeXFont(TeXFont.Type.SERIF, 28));
	}

	public void setSelectedFont(TeXFont font) {
		this.font = font;

		styleList.setSelectedValue(font.getType(), true);
		sizeList.setSelectedValue((int) font.getSize(), true);

		updatePreview(font);
	}

	public TeXFont getSelectedFont() {
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
		previewLabel.setVerticalTextPosition(JLabel.CENTER);
		previewLabel.setHorizontalTextPosition(JLabel.CENTER);
		previewLabel.setHorizontalAlignment(JLabel.CENTER);
		previewLabel.setBorder(new TitledBorder(dict.getString("toolbar.font.preview")));
		previewLabel.setMinimumSize(new Dimension(400, 100));
		previewLabel.setPreferredSize(new Dimension(400, 100));

		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new GridBagLayout());

		styleList = new JList<>(TeXFont.Type.values());
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
		gridBagConstraints.weightx = 1.0;

		fontPanel.add(styleScrollPane, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 1;
		gridBagConstraints.insets = new Insets(1, 1, 1, 1);
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

		mainPanel.add(previewLabel, BorderLayout.NORTH);
		mainPanel.add(fontPanel, BorderLayout.CENTER);

		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void styleSelectionChanged(TeXFont.Type style) {
		setSelectedFont(new TeXFont(style, font.getSize()));
	}

	private void sizeSelectionChanged(float size) {
		setSelectedFont(new TeXFont(font.getType(), size));
	}

	private void updatePreview(TeXFont font) {
		TeXFormula formula = new TeXFormula("\\int_a^b{f(x)\\,dx} = \\sum\\limits_{n = 1}^\\infty f(2^{-n} \\left( {b - a} \\right))");
		TeXIconBuilder builder = formula.new TeXIconBuilder()
				.setStyle(TeXConstants.STYLE_DISPLAY)
				.setSize(font.getSize())
				.setType(font.getType().getValue());

		TeXIcon icon = builder.build();

		previewLabel.setIcon(icon);
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

			TeXFont.Type style = (TeXFont.Type) value;

			switch (style) {
				case SERIF:
					label.setText(dict.getString("toolbar.font.serif"));
					break;
				case SANSSERIF:
					label.setText(dict.getString("toolbar.font.sans-serif"));
					break;
				case BOLD:
					label.setText(dict.getString("toolbar.font.bold"));
					break;
				case ITALIC:
					label.setText(dict.getString("toolbar.font.italic"));
					break;
				case BOLD_ITALIC:
					label.setText(dict.getString("toolbar.font.bold-italic"));
					break;
				case ROMAN:
					label.setText(dict.getString("toolbar.font.roman"));
					break;
				case TYPEWRITER:
					label.setText(dict.getString("toolbar.font.typewriter"));
					break;
			}

			return label;
		}
	}
}
