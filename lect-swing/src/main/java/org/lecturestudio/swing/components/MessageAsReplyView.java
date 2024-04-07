package org.lecturestudio.swing.components;

import org.lecturestudio.core.app.dictionary.Dictionary;

import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.Box;

public class MessageAsReplyView extends MessageView {

    private JLabel userToReplyToLabel;

    private JLabel replyToLabel;


    public MessageAsReplyView(Dictionary dict) {
        super(dict);
    }

    public void setUserToReplyTo(final String userToReplyTo) {
        userToReplyToLabel.setText(userToReplyTo);
    }

    @Override
    protected Box createUserPanel() {
        final Box userPanel = Box.createHorizontalBox();

        userPanel.setOpaque(false);
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(replyToLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(userToReplyToLabel);
        userPanel.add(privateLabel);
        userPanel.add(Box.createHorizontalGlue());

        return userPanel;
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        userToReplyToLabel = new JLabel();

        replyToLabel = new JLabel(dict.get("label.reply"));
        replyToLabel.setFont(replyToLabel.getFont().deriveFont(Font.ITALIC));
    }
}
