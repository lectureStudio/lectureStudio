package org.lecturestudio.presenter.swing.view;

import org.lecturestudio.core.PageMetrics;
import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.presenter.MenuPresenter;
import org.lecturestudio.presenter.api.view.DLZSettingsView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.web.api.model.Room;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

@SwingView(name = "dlz-settings", presenter = org.lecturestudio.presenter.api.presenter.DLZSettingsPresenter.class)
public class SwingDLZSettingsView extends JPanel implements DLZSettingsView {

    private JComboBox<Room> roomsCombo;

    private JButton closeButton;

    private JButton resetButton;


    SwingDLZSettingsView() {
        super();
    }



    @Override
    public void setRooms(List<Room> rooms) {
        SwingUtils.invoke(() -> roomsCombo
                .setModel(new DefaultComboBoxModel<>(new Vector<>(rooms))));
        System.out.println(rooms);
    }

    @Override
    public void setRoom(ObjectProperty<Room> room) { SwingUtils.bindBidirectional(roomsCombo, room);
    }

    @Override
    public void setOnClose(org.lecturestudio.core.view.Action action) {
        SwingUtils.bindAction(closeButton, action);
    }

    @Override
    public void setOnReset(Action action) {
        SwingUtils.bindAction(resetButton, action);
    }



    /**
     * Extended slide space to number and vice-versa converter.
     */
    private static class SlideSpaceConverter implements Converter<Dimension2D, Integer> {

        static final SlideSpaceConverter INSTANCE = new SlideSpaceConverter();

        private final PageMetrics metrics = new PageMetrics(4, 3);


        @Override
        public Integer to(Dimension2D value) {
            return (int) (Math.abs((value.getWidth() - metrics.getWidth()) / metrics.getWidth()) * 100);
        }

        @Override
        public Dimension2D from(Integer value) {
            double width = metrics.getWidth() - (value / 100.d * metrics.getWidth());
            double height = metrics.getHeight(width);

            return new Dimension2D(width, height);
        }
    }
}

