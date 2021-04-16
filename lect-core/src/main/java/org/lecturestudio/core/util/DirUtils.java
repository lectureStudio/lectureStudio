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

package org.lecturestudio.core.util;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import org.lecturestudio.core.io.file.visitor.CleanDirVisitor;
import org.lecturestudio.core.io.file.visitor.CopyDirVisitor;
import org.lecturestudio.core.io.file.visitor.DeleteDirVisitor;

public final class DirUtils {

	private DirUtils() {
		
	}

	/**
	 * Walks file tree starting at the given path and deletes all files
	 * but leaves the directory structure intact.
	 *
	 * @param path the base path to start from.
	 * 
	 * @throws IOException
	 */
	public static void clean(Path path) throws IOException {
		validate(path);
		Files.walkFileTree(path, new CleanDirVisitor());
	}
    
	/**
	 * Completely removes given file tree starting at and including the given path.
	 *
	 * @param path
	 * 
	 * @throws IOException
	 */
	public static void delete(Path path) throws IOException {
		validate(path);
		Files.walkFileTree(path, new DeleteDirVisitor());
	}
    
	/**
	 * Copies a directory tree
	 *
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	public static void copy(Path from, Path to) throws IOException {
		validate(from);
		Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new CopyDirVisitor(from, to));
	}

	/**
	 * Copies a directory tree by excluding files having a extension that is
	 * noted in the provided skip-list.
	 *
	 * @param from     The source path.
	 * @param to       The target path.
	 * @param skipList The list containing extensions that should be excluded.
	 *
	 * @throws IOException If the path could not be copied.
	 */
	public static void copy(Path from, Path to, List<String> skipList) throws IOException {
		validate(from);
		Files.walkFileTree(from, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new CopyDirVisitor(from, to, skipList));
	}
    
    /**
     * Moves one directory tree to another.  Not a true move operation in that the
     * directory tree is copied, then the original directory tree is deleted.
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public static void move(Path from, Path to) throws IOException {
        validate(from);
        Files.walkFileTree(from, new CopyDirVisitor(from, to));
        Files.walkFileTree(from, new DeleteDirVisitor());
    }

	public static void createIfNotExists(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
	}
    
	private static void validate(Path... paths) {
		for (Path path : paths) {
			if (!Files.isDirectory(path)) {
				throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
			}
		}
	}
	
}
