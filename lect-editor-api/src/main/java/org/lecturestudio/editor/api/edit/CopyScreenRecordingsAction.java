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

package org.lecturestudio.editor.api.edit;

import java.nio.file.*;
import java.util.List;

import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.action.ScreenAction;
import org.lecturestudio.core.recording.edit.EditAction;

/**
 * An implementation of {@link EditAction} that handles copying screen recording files
 * from a source directory to a target directory.
 * <p>
 * This action copies screen recording files based on a list of {@link ScreenAction} objects,
 * each containing a file name. The action can be undone (deleting copied files) or redone.
 * </p>
 */
public class CopyScreenRecordingsAction implements EditAction {

	private final Path sourcePath;

	private final Path targetPath;

	private final List<ScreenAction> screenActions;


	/**
	 * Constructs a new CopyScreenRecordingsAction.
	 *
	 * @param sourcePath    the source directory path containing the screen recordings.
	 * @param targetPath    the target directory path where screen recordings will be copied.
	 * @param screenActions the list of screen actions containing file names to be copied.
	 */
	public CopyScreenRecordingsAction(Path sourcePath, Path targetPath, List<ScreenAction> screenActions) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.screenActions = screenActions;
	}

	@Override
	public void undo() throws RecordingEditException {
		for (ScreenAction screenAction : screenActions) {
			Path targetFile = targetPath.resolve(screenAction.getFileName());
			try {
				Files.deleteIfExists(targetFile);
			}
			catch (Exception e) {
				throw new RecordingEditException("Failed to delete screen recording file: " + targetFile, e);
			}
		}
	}

	@Override
	public void redo() throws RecordingEditException {
		execute();
	}

	@Override
	public void execute() throws RecordingEditException {
		if (!Files.isDirectory(sourcePath)) {
			throw new RecordingEditException("Source path is not a directory: " + sourcePath);
		}
		if (!Files.isDirectory(targetPath)) {
			throw new RecordingEditException("Target path is not a directory: " + targetPath);
		}

		for (ScreenAction screenAction : screenActions) {
			Path sourceFile = Paths.get(sourcePath.toString(), screenAction.getFileName());
			if (Files.exists(sourceFile)) {
				Path targetFile = targetPath.resolve(sourceFile.getFileName());
				try {
					Files.copy(sourceFile, targetFile);
				}
				catch (FileAlreadyExistsException e) {
					// Ignore to be safe.
				}
				catch (Exception e) {
					throw new RecordingEditException("Failed to copy screen recording file: " + sourceFile, e);
				}
			}
		}
	}
}
