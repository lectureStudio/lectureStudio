package org.lecturestudio.swing.components;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.border.RoundedBorder;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.core.view.Action;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public abstract class ParticipantsPanel extends JPanel {

    protected static final Icon STREAMING_ICON = AwtResourceLoader.getIcon("stream-indicator.svg", 25);

    protected static final Icon MESSENGER_ICON = AwtResourceLoader.getIcon("messenger-indicator.svg", 25);

    protected final Dictionary dict;

    protected JLabel participantNameLabel;

    protected JLabel participantsUsernameLabel;

    protected String userId;

    protected JLabel streamingIndicatorIcon;

    protected JLabel messengerIndicatorIcon;

    protected JButton directMessageButton;


    abstract protected void createContent(JPanel content);


    public ParticipantsPanel(Dictionary dict, String userId) {
        super();

        this.userId = userId;
        this.dict = dict;

        initialize();
    }

    public void setOnDirectMessage(org.lecturestudio.core.view.Action action) {
        SwingUtils.bindAction(directMessageButton, action);
    }


    public void setParticipantNameLabel(String name) {
        participantNameLabel.setText(name);
        this.revalidate();
    }

    public void setParticipantsUsernameLabel(String username) {
        participantsUsernameLabel.setText("(" + username + ")");
        this.revalidate();
    }

    public void setStreamingIconVisible(boolean visible) {
        this.streamingIndicatorIcon.setVisible(visible);
        this.revalidate();
    }

    public boolean getStreamingIconVisible() {
        return this.streamingIndicatorIcon.isVisible();
    }

    public void setMessengerIconVisible(boolean visible) {
        this.messengerIndicatorIcon.setVisible(visible);
        this.directMessageButton.setEnabled(visible);
        this.directMessageButton.setVisible(visible);
        this.revalidate();
        this.repaint();
    }

    public boolean getMessengerIconVisible() {
        return this.messengerIndicatorIcon.isVisible();
    }

    public String getUserId() {
        return this.userId;
    }

    public void pack() {
        setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height));
        setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
        setMinimumSize(new Dimension(200, getPreferredSize().height));
    }

    private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

        JPanel content = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                if (this.getBorder() instanceof RoundedBorder) {
                    g.setColor(getBackground());
                    Shape borderShape = ((RoundedBorder) getBorder())
                            .getBorderShape(getWidth(), getHeight());
                    ((Graphics2D) g).fill(borderShape);
                }

                super.paintComponent(g);
            }
        };
        content.setLayout(new BorderLayout(1,1));
        content.setBackground(Color.WHITE);
        content.setBorder(new RoundedBorder(Color.LIGHT_GRAY, 5));
        content.setOpaque(false);

        participantNameLabel = new JLabel();
        participantNameLabel.setBorder(new EmptyBorder(0,0,0,5));
        participantNameLabel.setFont(new Font(participantNameLabel.getFont().getName(), Font.BOLD, participantNameLabel.getFont().getSize()));
        participantsUsernameLabel = new JLabel();

        streamingIndicatorIcon = new JLabel(STREAMING_ICON);
        messengerIndicatorIcon = new JLabel(MESSENGER_ICON);
        streamingIndicatorIcon.setVisible(false);
        messengerIndicatorIcon.setVisible(false);

        createContent(content);

        add(content);
    }
}
