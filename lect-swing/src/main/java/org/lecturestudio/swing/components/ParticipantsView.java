package org.lecturestudio.swing.components;

import org.lecturestudio.core.app.dictionary.Dictionary;

import javax.swing.*;
import java.awt.*;

public class ParticipantsView extends ParticipantsPanel {

    public ParticipantsView(Dictionary dict, String userId) {
        super(dict, userId);
    }

    @Override
    protected void createContent(JPanel content) {
        Box controlPanel = Box.createHorizontalBox();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        controlPanel.setOpaque(false);
        controlPanel.add(participantNameLabel);

        content.add(controlPanel, BorderLayout.NORTH);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParticipantsPanel) {
            return super.userId.equals(((ParticipantsPanel) obj).userId);
        }

        return false;
    }
}
