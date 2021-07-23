package org.lecturestudio.swing.components;

import org.lecturestudio.swing.components.previews.ToolPreview;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class ToolWidthChooser extends JPanel {
    private JSlider toolWidthSlider;

    private ToolPreview toolPreview;

    private JButton closeButton;

    public ToolWidthChooser(ResourceBundle resources, ToolPreview toolPreview) {
        super();

        initialize(resources, toolPreview);
    }

    public JSlider getToolWidthSlider() {
        return toolWidthSlider;
    }

    public void setOnClose(ActionListener listener) {
        closeButton.addActionListener(listener);
    }

    private void initialize(ResourceBundle dict, ToolPreview toolPreview) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.lightGray),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        toolWidthSlider = new JSlider(JSlider.HORIZONTAL);
        toolWidthSlider.setSnapToTicks(true);
        toolWidthSlider.setMinimum(1);
        toolWidthSlider.setMaximum(15);
        toolWidthSlider.setMinorTickSpacing(1);
        toolWidthSlider.setMajorTickSpacing(2);

        this.toolPreview = toolPreview;
        this.toolPreview.setPreferredSize(new Dimension(200, 80));
        this.toolPreview.setWidth(toolWidthSlider.getValue());

        toolWidthSlider.addChangeListener(e -> this.toolPreview.setWidth(toolWidthSlider.getValue())
        );


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BorderLayout(5, 5));

        toolPanel.add(new JLabel(dict.getString("toolbar.paint.size")), BorderLayout.NORTH);
        toolPanel.add(toolWidthSlider, BorderLayout.CENTER);

        toolPanel.add(toolPreview, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        closeButton = new JButton();
        closeButton.setText(dict.getString("button.close"));

        Dimension closeSize = closeButton.getPreferredSize();
        Dimension commonSize = new Dimension();
        commonSize.width = closeSize.width + 2;
        commonSize.height = closeSize.height + 2;

        closeButton.setPreferredSize(commonSize);

        buttonPanel.add(closeButton);

        mainPanel.add(toolPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
