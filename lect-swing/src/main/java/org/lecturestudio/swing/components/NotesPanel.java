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
import org.lecturestudio.swing.border.RoundedBorder;

import javax.swing.*;
import java.awt.*;
/**
 * Defines the appearance of a NotesPanel
 *
 * @author Dustin Ringel
 */
public abstract class NotesPanel  extends JPanel {

    protected final Dictionary dict;

    abstract protected void createContent(JPanel content);

    public NotesPanel(Dictionary dict) {
        super();

        this.dict = dict;

        initialize();
    }

    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

        JPanel content = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                if (getBorder() instanceof RoundedBorder) {
                    g.setColor(getBackground());
                    Shape borderShape = ((RoundedBorder) getBorder())
                            .getBorderShape(getWidth(), getHeight());
                    ((Graphics2D) g).fill(borderShape);
                }

                super.paintComponent(g);
            }
        };
        content.setLayout(new BorderLayout(1, 1));
        content.setBackground(Color.WHITE);
        content.setBorder(new RoundedBorder(Color.LIGHT_GRAY, 5));
        content.setOpaque(false);

        createContent(content);
        add(content);
    }

    public void pack() {
        setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height));
        setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
        setMinimumSize(new Dimension(200, getPreferredSize().height));
    }
}
