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

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.OpenDocumentEvent;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.util.FileUtils;

public abstract class DocumentOverview extends JPanel {

	private static final long serialVersionUID = 6441509730772693838L;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private JProgressBar progressBar;

	protected JPanel docPanel;

	private JPanel loadingPanel;

	private JLabel errorLabel;

	protected ApplicationContext context;


	protected abstract JPanel createButtonPanel();


	public DocumentOverview(ApplicationContext context) {
		init(context);
	}

	public void showErrorMessage(String message) {
		errorLabel.setVisible(true);
		errorLabel.setText(message);
	}

	public void hideStatus() {
		errorLabel.setText("");
		errorLabel.setVisible(false);
		loadingPanel.setVisible(false);
	}

    protected ApplicationContext getApplicationContext() {
    	return context;
    }

	protected void progressPending() {
		errorLabel.setText("");
		progressBar.setIndeterminate(true);
		loadingPanel.setVisible(true);
	}

	public void updateOverview() {
		docPanel.removeAll();

		Configuration config = context.getConfiguration();
		List<RecentDocument> docs = config.getRecentDocuments();
		Dimension buttonSize = new Dimension(250, 50);

		for (final RecentDocument doc : docs) {
			File file = new File(doc.getDocumentPath());
			if (!file.exists()) {
				continue;
			}

			JButton button = new JButton() {

				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);

					Font font = getFont();
					Graphics2D g2d = (Graphics2D) g;
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
					g2d.setPaint(Color.BLACK);
					g2d.setFont(font.deriveFont(Font.BOLD));
					g2d.drawString(doc.getDocumentName(), 15, 20);
					g2d.setFont(font.deriveFont(Font.PLAIN));
					g2d.drawString(FileUtils.shortenPath(doc.getDocumentPath(), 35), 15, 50 - 10);
				}

			};
			button.setMinimumSize(buttonSize);
			button.setPreferredSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.setToolTipText(context.getDictionary().get("overview.open.document"));
			button.addActionListener(e -> {
				progressPending();

				OpenDocumentEvent event = new OpenDocumentEvent(new File(doc.getDocumentPath()));
				event.setEventProcessedHandler(this::hideStatus);

				ApplicationBus.post(event);
			});

			docPanel.add(button);
			docPanel.add(Box.createRigidArea(new Dimension(0, 3)));
		}
	}

	private void init(ApplicationContext context) {
		this.context = context;
		
		setLayout(new GridBagLayout());
		setBorder(new EmptyBorder(20, 20, 20, 20));

		JPanel c = new JPanel(new BorderLayout(0, 100));
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(false);

		loadingPanel = new JPanel();
		loadingPanel.setLayout(new GridBagLayout());
		loadingPanel.setOpaque(false);
		loadingPanel.setVisible(false);

		JLabel loadingLabel = new JLabel(context.getDictionary().get("loading.document"));

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);


		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = 17;

		loadingPanel.add(loadingLabel, constraints);

		constraints.fill = 2;
		constraints.weightx = 1.0D;
		constraints.gridy = 1;
		loadingPanel.add(progressBar, constraints);

		errorLabel = new JLabel();
		errorLabel.setForeground(Color.RED);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0D;

		panel.add(loadingPanel, constraints);

		constraints.gridy = 1;
		constraints.fill = 0;
		constraints.anchor = 10;
		panel.add(errorLabel, constraints);

		docPanel = new JPanel();
		docPanel.setLayout(new BoxLayout(docPanel, BoxLayout.Y_AXIS));
		docPanel.setOpaque(false);



		GridBagConstraints cts = new GridBagConstraints();
		cts.gridx = 0;
		cts.gridy = 0;
		cts.anchor = GridBagConstraints.NORTH;

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new GridBagLayout());
		containerPanel.setOpaque(false);

		containerPanel.add(createButtonPanel(), cts);

		cts.gridx++;
		cts.insets = new Insets(0, 50, 0, 0);
		containerPanel.add(docPanel, cts);

		c.add(panel, BorderLayout.NORTH);
		c.add(containerPanel, BorderLayout.CENTER);

		Font font = new Font(Font.DIALOG, Font.PLAIN, 20);
		JPanel topBeta = new JPanel();
		JPanel bottomBeta = new JPanel();

		for (int i = 0; i < 3; i++) {
			JLabel label = new JLabel(context.getDictionary().get("beta"));
			label.setFont(font);
			label.setForeground(Color.DARK_GRAY);
			topBeta.add(label);
		}
		for (int i = 0; i < 3; i++) {
			JLabel label = new JLabel(context.getDictionary().get("beta"));
			label.setFont(font);
			label.setForeground(Color.DARK_GRAY);
			bottomBeta.add(label);
		}

		c.add(topBeta, BorderLayout.NORTH);
		c.add(bottomBeta, BorderLayout.SOUTH);

		add(c);

		updateOverview();
	}

	public void addOverviewItemRemovedListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener("OverviewItemRemoved", listener);
	}

	public void removeOverviewItemRemovedListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener("OverviewItemRemoved", listener);
	}

}
