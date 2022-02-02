package org.lecturestudio.presenter.swing.combobox;

import org.lecturestudio.broadcast.server.QuarkusServer;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.web.api.model.messenger.MessengerConfig;
import org.lecturestudio.web.api.stream.model.Course;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.Locale;

import static java.util.Objects.nonNull;

public class MessengerModeRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        MessengerConfig.MessengerMode mode = (MessengerConfig.MessengerMode) value;

        if (nonNull(mode)) {
            setText(String.format("%s", mode.toString().toLowerCase(Locale.ROOT)));
        }
        else {
            setText("");
        }

        return this;
    }
}
