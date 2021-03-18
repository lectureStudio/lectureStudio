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
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.ColorConverter;

public class ToolColorChooser extends JPanel {

	private JPanel presetPanel;

	private ColorBox selectedLabel;

	private JSlider toolWidthSlider;

	private PenToolPreview toolPreview;

	private HsbChooser hsbChooser;

	private JButton okButton;

	private JButton cancelButton;

	private Stroke stroke;


	public ToolColorChooser(ResourceBundle resources) {
		super();

		initialize(resources);
		setSelectedStroke(new Stroke(org.lecturestudio.core.graphics.Color.BLACK, 1), true);
	}

	public void setSelectedStroke(Stroke stroke, boolean selectLabel) {
		this.stroke = stroke;

		Color color = ColorConverter.INSTANCE.to(stroke.getColor());

		toolPreview.setColor(color);
		hsbChooser.setColor(color);

		toolWidthSlider.setValue((int) stroke.getWidth());

		if (selectLabel) {
			for (Component c : presetPanel.getComponents()) {
				if (color.equals(c.getBackground())) {
					ColorBox label = (ColorBox) c;
					label.select(true);

					selectedLabel = label;
					break;
				}
			}
		}
	}

	public Stroke getSelectedStroke() {
		return stroke;
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

		toolWidthSlider = new JSlider(JSlider.HORIZONTAL);
		toolWidthSlider.setSnapToTicks(true);
		toolWidthSlider.setMinimum(1);
		toolWidthSlider.setMaximum(30);
		toolWidthSlider.setMinorTickSpacing(1);
		toolWidthSlider.setMajorTickSpacing(2);
		toolWidthSlider.addChangeListener(e -> {
			stroke.setWidth(toolWidthSlider.getValue());
			toolPreview.setWidth(toolWidthSlider.getValue());
		});

		toolPreview = new PenToolPreview();
		toolPreview.setPreferredSize(new Dimension(200, 80));

		hsbChooser = new HsbChooser();
		hsbChooser.addChangeListener(e -> {
			stroke.setColor(ColorConverter.INSTANCE.from(hsbChooser.getColor()));
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(10, 10));

		presetPanel = new JPanel();
		presetPanel.setLayout(new GridLayout(10, 10));

		final int numColors = RAW_VALUES.length / 3;

		for (int i = 0; i < numColors; i++) {
			final Color color = new Color(RAW_VALUES[i * 3], RAW_VALUES[i * 3 + 1], RAW_VALUES[i * 3 + 2]);
			final ColorBox colorBox = new ColorBox(color);

			presetPanel.add(colorBox);
		}

		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new BorderLayout(5, 5));
		toolPanel.add(new JLabel(dict.getString("toolbar.paint.size")), BorderLayout.NORTH);
		toolPanel.add(toolWidthSlider, BorderLayout.CENTER);
		toolPanel.add(toolPreview, BorderLayout.SOUTH);

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

		mainPanel.add(presetPanel, BorderLayout.CENTER);
		mainPanel.add(toolPanel, BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}



	private class ColorBox extends JComponent {

		private final Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
		private final Border selectBorder = new LineBorder(Color.blue);


		ColorBox(Color color) {
			setOpaque(true);
			setBackground(color);
			setBorder(emptyBorder);
			setPreferredSize(new Dimension(20, 20));
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					selectedLabel = ColorBox.this;
					stroke.setColor(ColorConverter.INSTANCE.from(getBackground()));
					setSelectedStroke(stroke, false);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					if (nonNull(selectedLabel)) {
						selectedLabel.select(false);
					}

					select(true);

					if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
						selectedLabel = ColorBox.this;
						stroke.setColor(ColorConverter.INSTANCE.from(getBackground()));
						setSelectedStroke(stroke, false);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setBorder(emptyBorder);

					Point p = SwingUtilities.convertPoint(ColorBox.this, e.getPoint(), presetPanel);

					if (!presetPanel.getBounds().contains(p)) {
						if (nonNull(selectedLabel)) {
							selectedLabel.select(true);
						}
					}
				}
			});
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintBorder(g);

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(getBackground());
			g.fillRect(2, 2, getSize().width - 4, getSize().height - 4);
		}

		public void select(boolean select) {
			if (select) {
				setBorder(selectBorder);
			}
			else {
				setBorder(emptyBorder);
			}
		}
	}


	private static final int[] RAW_VALUES = {
			// 1st row
			250, 250, 250,
			245, 245, 245,
			238, 238, 238,
			224, 224, 224,
			189, 189, 189,
			158, 158, 158,
			117, 117, 117,
			97, 97, 97,
			66, 66, 66,
			33, 33, 33,

			// 2nd row
			236, 239, 241,
			207, 216, 220,
			176, 190, 197,
			144, 164, 174,
			120, 144, 156,
			96, 125, 139,
			84, 110, 122,
			69, 90, 100,
			55, 71, 79,
			38, 50, 56,

			// 3rd row
			255, 235, 238,
			255, 205, 210,
			239, 154, 154,
			229, 115, 115,
			239, 83, 80,
			244, 67, 54,
			229, 57, 53,
			211, 47, 47,
			198, 40, 40,
			183, 28, 28,

			// 4th row
			232, 234, 246,
			197, 202, 233,
			159, 168, 218,
			121, 134, 203,
			92, 107, 192,
			63, 81, 181,
			57, 73, 171,
			48, 63, 159,
			40, 53, 147,
			26, 35, 126,

			// 5th row
			227, 242, 253,
			187, 222, 251,
			144, 202, 249,
			100, 181, 246,
			66, 165, 245,
			33, 150, 243,
			30, 136, 229,
			25, 118, 210,
			21, 101, 192,
			13, 71, 161,

			// 6th row
			232, 245, 233,
			200, 230, 201,
			165, 214, 167,
			129, 199, 132,
			102, 187, 106,
			76, 175, 80,
			67, 160, 71,
			56, 142, 60,
			46, 125, 50,
			27, 94, 32,

			// 7th row
			255, 253, 231,
			255, 249, 196,
			255, 245, 157,
			255, 241, 118,
			255, 238, 88,
			255, 235, 59,
			253, 216, 53,
			251, 192, 45,
			249, 168, 37,
			245, 127, 23,

			// 8th row
			255, 243, 224,
			255, 224, 178,
			255, 204, 128,
			255, 183, 77,
			255, 167, 38,
			255, 152, 0,
			251, 140, 0,
			245, 124, 0,
			239, 108, 0,
			230, 81, 0,

			// 9th row
			251, 233, 231,
			255, 204, 188,
			255, 171, 145,
			255, 138, 101,
			255, 112, 67,
			255, 87, 34,
			244, 81, 30,
			230, 74, 25,
			216, 67, 21,
			191, 54, 12,

			// 10th row
			239, 235, 233,
			215, 204, 200,
			188, 170, 164,
			161, 136, 127,
			141, 110, 99,
			121, 85, 72,
			109, 76, 65,
			93, 64, 55,
			78, 52, 46,
			62, 39, 35,
	};
}
