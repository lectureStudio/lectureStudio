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

package org.lecturestudio.core.recording;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.lecturestudio.core.model.Interval;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.recording.action.ActionFactory;
import org.lecturestudio.core.recording.action.ActionType;
import org.lecturestudio.core.recording.action.BaseStrokeAction;
import org.lecturestudio.core.recording.action.PanningAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;
import org.lecturestudio.core.recording.action.ToolBeginAction;
import org.lecturestudio.core.recording.action.ToolEndAction;
import org.lecturestudio.core.recording.action.ToolExecuteAction;

public class RecordedPage implements RecordedObject, Cloneable {

	private final List<StaticShapeAction> staticActions = new ArrayList<>();

	private final List<PlaybackAction> playback = new ArrayList<>();

	private int number;

	private int timestamp;


	public RecordedPage() {

	}

	public RecordedPage(byte[] input) throws IOException {
		parseFrom(input);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public void addStaticAction(StaticShapeAction action) {
		staticActions.add(action);
	}

	public void addPlaybackAction(PlaybackAction action) {
		playback.add(action);
	}

	public void removeStaticAction(StaticShapeAction action) {
		staticActions.remove(action);
	}

	public List<StaticShapeAction> getStaticActions() {
		return staticActions;
	}

	public List<PlaybackAction> getPlaybackActions() {
		return playback;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int staticCount = staticActions.size();
		int playbackCount = playback.size();
		
		byte[][] staticData = new byte[staticCount][];
		byte[][] playbackData = new byte[playbackCount][];

		int clonedSize = 0;
		int playbackSize = 0;

		for (int i = 0; i < staticCount; i++) {
			StaticShapeAction action = staticActions.get(i);
			staticData[i] = action.toByteArray();
			clonedSize += staticData[i].length;
		}

		for (int i = 0; i < playbackCount; i++) {
			PlaybackAction action = playback.get(i);
			playbackData[i] = action.toByteArray();
			playbackSize += playbackData[i].length;
		}

		int totalSize = 20 + clonedSize + playbackSize;

		ByteBuffer buffer = ByteBuffer.allocate(totalSize);
		// Write header
		buffer.putInt(totalSize - 4);
		buffer.putInt(number);
		buffer.putInt(timestamp);

		// Write data
		// Static actions
		buffer.putInt(clonedSize);
		for (int i = 0; i < staticCount; i++) {
			buffer.put(staticData[i]);
		}
		// Playback actions
		buffer.putInt(playbackSize);
		for (int i = 0; i < playbackCount; i++) {
			buffer.put(playbackData[i]);
		}

		return buffer.array();
	}
	
	@Override
	public void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		// Read page header.
		number = buffer.getInt();
		timestamp = buffer.getInt();

		// Read static/cloned actions.
		int clonedSize = buffer.getInt();
		if (clonedSize > 0) {
			// Get cloned action chunk.
			byte[] clonedData = new byte[clonedSize];
			buffer.get(clonedData);

			ByteBuffer clonedBuffer = ByteBuffer.wrap(clonedData);
			clonedBuffer.clear();

			while (clonedBuffer.hasRemaining()) {
				int length = clonedBuffer.getInt();
				int type = clonedBuffer.get();
				int timestamp = clonedBuffer.getInt();
				
				byte[] actionData = null;
				int dataLength = length - 5;
				if (dataLength > 0) {
					actionData = new byte[dataLength];
					clonedBuffer.get(actionData);
				}

				PlaybackAction action = ActionFactory.createAction(type, timestamp, actionData);

				StaticShapeAction staticAction = new StaticShapeAction(action);
				staticActions.add(staticAction);
			}
		}

		int playbackSize = buffer.getInt();
		if (playbackSize > 0) {
			// Get playback action chunk.
			byte[] playbackData = new byte[playbackSize];
			buffer.get(playbackData);

			ByteBuffer playbackBuffer = ByteBuffer.wrap(playbackData);
			playbackBuffer.clear();

			while (playbackBuffer.hasRemaining()) {
				int length = playbackBuffer.getInt();
				int type = playbackBuffer.get();
				int timestamp = playbackBuffer.getInt();

				// Get event chunk.
				byte[] actionData = null;
				int dataLength = length - 5;
				if (dataLength > 0) {
					actionData = new byte[dataLength];
					playbackBuffer.get(actionData);
				}

				PlaybackAction action = ActionFactory.createAction(type, timestamp, actionData);

				playback.add(action);
			}
		}
	}

	public void cut(Interval<Integer> interval) {
		ListIterator<PlaybackAction> iter = playback.listIterator();

		PlaybackAction toolAction = null;
		ToolBeginAction toolBeginAction = null;
		ToolExecuteAction toolExecAction = null;
		int insertIndex = -1;

		while (iter.hasNext()) {
			PlaybackAction action = iter.next();

			boolean contains = interval.contains(action.getTimestamp());

			if (contains) {
				// Look ahead to determine the action tool type.
				int nextIndex = iter.nextIndex();

				PlaybackAction nextAction = nextIndex < playback.size() - 1 ?
						playback.get(nextIndex) :
						null;

				boolean isStrokeAction = action instanceof BaseStrokeAction;
				boolean isPanAction = action instanceof PanningAction;

				// Do not remove actions that are mandatory to execute the
				// corresponding tool.
				if (isStrokeAction || isPanAction && nonNull(nextAction)
						&& nextAction instanceof ToolBeginAction) {
					// Save tool state.
					toolAction = action;
					toolBeginAction = (ToolBeginAction) nextAction;
				}
				else if (action instanceof ToolEndAction) {
					// Cut-interval encloses complete tool action. Reset state.
					toolAction = null;
					toolBeginAction = null;
				}

				insertIndex = iter.nextIndex() - 1;

				iter.remove();
			}
			else if (action.getTimestamp() > interval.getEnd()) {
				if (action instanceof ToolExecuteAction) {
					toolExecAction = (ToolExecuteAction) action;
				}

				// Done removing actions.
				break;
			}
		}

		if (nonNull(toolAction)) {
			// Adjust tool action state.
			toolAction.setTimestamp(interval.getEnd());
			toolBeginAction.setTimestamp(interval.getEnd());

			if (toolAction.getType() != ActionType.ZOOM &&
				toolAction.getType() != ActionType.PANNING) {
				toolBeginAction.setPoint(toolExecAction.getPoint());
			}

			playback.add(insertIndex, toolAction);
			playback.add(insertIndex + 1, toolBeginAction);
		}
	}

	public void shift(Interval<Integer> interval) {
		int pageTime = getTimestamp();
		if (pageTime >= interval.getEnd()) {
			setTimestamp(pageTime - interval.lengthInt());
		}

		for (PlaybackAction action : getPlaybackActions()) {
			int actionTime = action.getTimestamp();
			if (actionTime >= interval.getEnd()) {
				action.shift(interval.lengthInt());
			}
		}
	}

	public void shiftRight(Interval<Integer> interval) {
		int pageTime = getTimestamp();
		if (pageTime >= interval.getStart()) {
			setTimestamp(pageTime + interval.lengthInt());
		}

		for (PlaybackAction action : getPlaybackActions()) {
			int actionTime = action.getTimestamp();
			if (actionTime >= interval.getStart()) {
				action.shift(-interval.lengthInt());
			}
		}
	}

	public RecordedPage clone() {
		RecordedPage page = new RecordedPage();
		page.setNumber(getNumber());
		page.setTimestamp(getTimestamp());

		for (PlaybackAction action : playback) {
			page.addPlaybackAction(action.clone());
		}

		for (StaticShapeAction action : staticActions) {
			page.addStaticAction(action.clone());
		}

		return page;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[ Page ]");
		sb.append("\n");
		sb.append("Number: \t" + number);
		sb.append("\n");
		sb.append("Time: \t\t" + new Time(timestamp, true));
		sb.append("\n");
		sb.append("Static actions: " + staticActions.size());
		sb.append("\n");
		sb.append(" - Playback actions");
		sb.append("\n");

		for (PlaybackAction action : playback) {
			sb.append("\t" + action.getClass().getSimpleName() + "\t\t" + new Time(action.getTimestamp(), true));
			sb.append("\n");
		}

		return sb.toString();
	}

}
