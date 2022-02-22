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
        Box namePanel = Box.createHorizontalBox();
        namePanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        namePanel.setOpaque(false);

        namePanel.add(participantNameLabel);
        namePanel.add(participantsUsernameLabel);

        content.add(namePanel, BorderLayout.LINE_START);

        this.directMessageButton = new JButton(this.dict.get("button.directMessage"));
        this.directMessageButton.setEnabled(false);
        this.directMessageButton.setVisible(false);

        JPanel controlPanel = new JPanel();
        controlPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
        controlPanel.setOpaque(false);
        controlPanel.add(directMessageButton);

        content.add(controlPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusPanel.setOpaque(false);

        statusPanel.add(this.streamingIndicatorIcon);
        statusPanel.add(this.messengerIndicatorIcon);

        content.add(statusPanel, BorderLayout.LINE_END);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParticipantsPanel) {
            return super.userId.equals(((ParticipantsPanel) obj).userId);
        }

        return false;
    }
}
