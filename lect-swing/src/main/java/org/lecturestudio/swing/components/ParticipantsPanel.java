package org.lecturestudio.swing.components;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.border.RoundedBorder;

import javax.swing.*;
import java.awt.*;

public abstract class ParticipantsPanel extends JPanel {

    protected final Dictionary dict;

    protected JLabel participantNameLabel;

    protected String userId;


    abstract protected void createContent(JPanel content);


    public ParticipantsPanel(Dictionary dict, String userId) {
        super();

        this.userId = userId;
        this.dict = dict;

        initialize();
    }


    public void setParticipantNameLabel(String name) {
        participantNameLabel.setText(name);
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

        createContent(content);

        add(content);
    }
}
