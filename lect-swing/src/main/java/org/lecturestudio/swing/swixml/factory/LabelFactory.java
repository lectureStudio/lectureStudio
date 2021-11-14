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

import java.awt.Font;

import javax.swing.JLabel;

import org.swixml.LogAware;
import org.swixml.factory.BeanFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class LabelFactory extends BeanFactory implements LogAware {

	public LabelFactory(Class<?> beanClass) {
		super(beanClass);
	}

	@Override
	public Object create(Object owner, Element element) throws Exception {
		JLabel label = (JLabel) super.create(owner, element);
		Node node = element.getAttributeNode("font-scale");

		if (nonNull(node)) {
			String scaleStr = node.getNodeValue();

			if (nonNull(scaleStr) && !scaleStr.isEmpty() && !scaleStr.isBlank()) {
				try {
					float scale = Float.parseFloat(scaleStr);
					Font font = label.getFont();

					label.setFont(font.deriveFont(font.getSize2D() * scale));
				}
				catch (Throwable e) {
					logger.warning("Failed to set scale: " + scaleStr);
				}
			}
		}

		return label;
	}
}
