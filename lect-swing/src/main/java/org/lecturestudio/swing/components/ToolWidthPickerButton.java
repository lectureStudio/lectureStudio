package org.lecturestudio.swing.components;

import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.components.previews.ToolPreview;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class ToolWidthPickerButton extends ToggleComboButton<Stroke> {

    private ToolWidthChooser chooser;

    ResourceBundle resourceBundle;

    @Inject
    public ToolWidthPickerButton(ResourceBundle resourceBundle) {
        super();

        this.resourceBundle = resourceBundle;
    }

    public void initialize(ToolPreview toolPreview) {
        chooser = new ToolWidthChooser(resourceBundle, toolPreview);
        chooser.setOnClose(e -> hidePopup());

        setContent(chooser);
    }

    public ToolWidthChooser getChooser() {
        return chooser;
    }
}
