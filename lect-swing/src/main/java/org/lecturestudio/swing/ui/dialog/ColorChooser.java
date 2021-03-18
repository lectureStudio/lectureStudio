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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.swing.components.HsbChooser;

public class ColorChooser extends AbstractDialog<Color> {

	private static final long serialVersionUID = 6067828544586359773L;

	private final HsbChooser hsbChooser = new HsbChooser();

	private JLabel colorLabel;

	private Color color = null;


	public ColorChooser(Frame parent, ApplicationContext context) {
		super(parent, context);

		init();
	}

	@Override
	public Color open() {
		setVisible(true);

		return color;
	}

	public void setColor(Color color) {
		hsbChooser.setColor(color);
	}

	private void init() {
		setTitle(dict.get("color.dialog.title"));
		setLayout(new GridBagLayout());
		setModal(true);
		setResizable(false);

		initComponentListeners();
		initComponents();

		pack();
		centerToScreen();
	}

	protected void initComponentListeners() {
		hsbChooser.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateSelectedColor();
			}
		});
	}

	@Override
	protected void closeDialog() {
		color = null;
		super.closeDialog();
	}

	@Override
	protected void initComponents() {
		colorLabel = new JLabel() {

			private static final long serialVersionUID = -4499722600260440127L;


			@Override
			public void paintComponent(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(10, 20, getSize().width - 20, getSize().height - 30);
				g.setColor(Color.BLACK);
				g.drawRect(8, 18, getSize().width - 17, getSize().height - 27);
			}
		};
		colorLabel.setPreferredSize(new Dimension(80, 60));
		colorLabel.setBackground(hsbChooser.getColor());
		colorLabel.setOpaque(false);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.insets = new Insets(0, 8, 0, 0);

		getContentPane().add(colorLabel, constraints);
		getContentPane().add(hsbChooser);

		initBottomPanel();
	}

	protected void initBottomPanel() {
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

		JButton okButton = new JButton();
		okButton.setText(dict.get("button.ok"));
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		JButton cancelButton = new JButton();
		cancelButton.setText(dict.get("button.cancel"));
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});

		Dimension okSize = okButton.getPreferredSize();
		Dimension cancelSize = cancelButton.getPreferredSize();
		Dimension commonSize = new Dimension();
		commonSize.width = Math.max(okSize.width, cancelSize.width);
		commonSize.height = Math.max(okSize.height, cancelSize.height) + 2;

		okButton.setPreferredSize(commonSize);
		cancelButton.setPreferredSize(commonSize);

		bottomPanel.add(cancelButton);
		bottomPanel.add(okButton);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.EAST;

		getContentPane().add(bottomPanel, constraints);
	}

	private void updateSelectedColor() {
		this.color = hsbChooser.getColor();
		colorLabel.setBackground(color);
	}

}
