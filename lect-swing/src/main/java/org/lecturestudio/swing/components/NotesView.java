/*
 *
 *  * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 *  * Embedded Systems and Applications Group.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.lecturestudio.swing.components;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Defines the appearance of a notesView
 *
 * @author Dustin Ringel
 */
public class NotesView extends NotesPanel{
    private JTextArea textArea;

    public NotesView(Dictionary dict) {
        super(dict);
    }

    /**
     * Writes the given text into a text field
     *
     * @param noteText      the note that will display in the View
     */
    public void setNote(String noteText) {
        textArea.setText(noteText);
    }

    @Override
    protected void createContent(JPanel content) {

        Box controlPanel = Box.createHorizontalBox();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        controlPanel.setOpaque(false);
        controlPanel.add(Box.createHorizontalGlue());
        controlPanel.add(Box.createHorizontalStrut(5));
        controlPanel.add(Box.createHorizontalStrut(5));

        textArea = new JTextArea();
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(textArea.getFont().deriveFont(12f));

        content.add(controlPanel, BorderLayout.NORTH);
        content.add(textArea, BorderLayout.CENTER);

        content.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtils.invoke(() -> {
                    Insets insets = getInsets();
                    Insets cInsets = content.getInsets();
                    Dimension size = getPreferredSize();
                    Dimension textSize = textArea.getPreferredSize();

                    int height = controlPanel.getPreferredSize().height;
                    height += textSize.height;
                    height += insets.top + insets.bottom + cInsets.top + cInsets.bottom;

                    setSize(new Dimension(size.width, height));
                    setPreferredSize(new Dimension(size.width, height));
                    setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

                    revalidate();
                    repaint();
                });
            }
        });
    }
}
