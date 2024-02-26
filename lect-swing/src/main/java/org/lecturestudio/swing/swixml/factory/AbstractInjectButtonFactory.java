/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.swixml.factory;

import static java.util.Objects.nonNull;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.lecturestudio.core.inject.Injector;

import org.swixml.LogAware;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AbstractInjectButtonFactory extends InjectViewFactory implements LogAware {

	public AbstractInjectButtonFactory(Class<?> beanClass, Injector injector) {
		super(beanClass, injector);
	}

	@Override
	public Object create(Object owner, Element element) {
		AbstractButton button = (AbstractButton) injector.getInstance(getTemplate());
		Node node = element.getAttributeNode("accelerator");

		if (nonNull(node)) {
			String[] accelerators = node.getNodeValue().split(",|,\\s");

			for (String accelerator : accelerators) {
				KeyStroke stroke = KeyStroke.getKeyStroke(accelerator);

				if (nonNull(stroke)) {
					button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
							.put(stroke, stroke.toString());
					button.getActionMap()
							.put(stroke.toString(), new AbstractAction() {

								@Override
								public void actionPerformed(ActionEvent e) {
									AbstractButton b = (AbstractButton) e.getSource();
									b.doClick();
								}
							});
				}
				else {
					logger.warning("Failed to set assigned accelerator: " + accelerator);
				}
			}
		}

		return button;
	}
}
