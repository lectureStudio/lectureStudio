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

package org.lecturestudio.core.io.file.visitor;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.lecturestudio.core.util.FileUtils;

public class CopyDirVisitor extends SimpleFileVisitor<Path> {

	private final Path fromPath;

	private final Path toPath;

	private final StandardCopyOption copyOption;

	private final List<String> skipList;


	public CopyDirVisitor(Path fromPath, Path toPath) {
		this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING, null);
	}

	public CopyDirVisitor(Path fromPath, Path toPath, List<String> skipList) {
		this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING, skipList);
	}

	public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption, List<String> skipList) {
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.copyOption = copyOption;
		this.skipList = skipList;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if (!Files.exists(targetPath)) {
			Files.createDirectory(targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		boolean copy = !nonNull(skipList) || !skipList.contains(
				FileUtils.getExtension(file.toString()));

		if (copy) {
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
		}

		return FileVisitResult.CONTINUE;
	}

}
