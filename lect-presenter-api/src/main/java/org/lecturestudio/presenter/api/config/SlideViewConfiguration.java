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

package org.lecturestudio.presenter.api.config;

import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.presenter.api.model.MessageBarPosition;

public class SlideViewConfiguration {

	private final ObjectProperty<MessageBarPosition> messageBarPosition = new ObjectProperty<>(
			MessageBarPosition.BOTTOM);

	private final ObjectProperty<MessageBarPosition> participantsPosition = new ObjectProperty<>(
			MessageBarPosition.LEFT);

	private final DoubleProperty leftSliderPosition = new DoubleProperty(0.375);

	private final DoubleProperty rightSliderPosition = new DoubleProperty(0.8);

	private final DoubleProperty bottomSliderPosition = new DoubleProperty(0.7);


	public DoubleProperty leftSliderPositionProperty() {
		return leftSliderPosition;
	}

	public double getLeftSliderPosition() {
		return leftSliderPosition.get();
	}

	public void setLeftSliderPosition(double value) {
		leftSliderPosition.set(value);
	}

	public DoubleProperty rightSliderPositionProperty() {
		return rightSliderPosition;
	}

	public double getRightSliderPosition() {
		return rightSliderPosition.get();
	}

	public void setRightSliderPosition(double value) {
		rightSliderPosition.set(value);
	}

	public DoubleProperty bottomSliderPositionProperty() {
		return bottomSliderPosition;
	}

	public double getBottomSliderPosition() {
		return bottomSliderPosition.get();
	}

	public void setBottomSliderPosition(double value) {
		bottomSliderPosition.set(value);
	}

	/**
	 * @return Message bar's position
	 */
	public MessageBarPosition getMessageBarPosition() {
		return messageBarPosition.get();
	}

	/**
	 * @param position Message bar's position
	 */
	public void setMessageBarPosition(MessageBarPosition position) {
		messageBarPosition.set(position);
	}

	public MessageBarPosition getParticipantsPosition() {
		return participantsPosition.get();
	}

	public void setParticipantsPosition(MessageBarPosition position) {
		participantsPosition.set(position);
	}
}
