/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.swing.view;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.model.Stopwatch;
import org.lecturestudio.presenter.api.view.StopwatchConfigView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;

@SwingView(name = "stopwatchConfig")
public class SwingStopwatchConfigView extends JPanel implements StopwatchConfigView {

    private ConsumerAction<Boolean> viewVisibleAction;

    private ConsumerAction<Stopwatch.StopwatchType> stopwatchTypeAction;

    private Container contentContainer;

    private JRadioButton stopwatchRadioButton;

    private JRadioButton timerRadioButton;

    private JButton closeButton;

    private JButton startButton;

    private JTextField setTimerTextField;

    public javax.swing.Action typeAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            Stopwatch.StopwatchType type = Stopwatch.StopwatchType.valueOf(e.getActionCommand());

            executeAction(stopwatchTypeAction, type);
        }
    };

    SwingStopwatchConfigView() {
        super();
    }

    @Override
    public void setStopwatchTime(StringProperty path) {
        SwingUtils.bindBidirectional(setTimerTextField, path);
    }

    @Override
    public void setOnClose(Action action) {
        SwingUtils.bindAction(closeButton, action);
    }

    @Override
    public void setOnStart(Action action) {
        SwingUtils.bindAction(startButton, action);
    }

    @Override
    public void setOnStopwatchType(ConsumerAction<Stopwatch.StopwatchType> action) {
        stopwatchTypeAction = action;
    }

    @ViewPostConstruct
    private void initialize() {
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                executeAction(viewVisibleAction, true);
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                executeAction(viewVisibleAction, false);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
}
