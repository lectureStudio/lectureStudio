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

package org.lecturestudio.core.service;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Singleton;

@Singleton
public class SearchFileService {

	public CompletableFuture<Collection<Path>> search(String startDirPath, String pattern) {
		return CompletableFuture.supplyAsync(() -> {
			Finder finder = new Finder(pattern);
			Path path = Paths.get(startDirPath);

			try {
				Files.walkFileTree(path, finder);
			}
			catch (IOException e) {
				throw new CompletionException(e);
			}

			return finder.getFoundFiles();
		});
	}



	static class Finder extends SimpleFileVisitor<Path> {

		private final PathMatcher matcher;

		private final List<Path> foundList;


		Finder(String pattern) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
			foundList = new ArrayList<>();
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			String name = dir.getFileName().toString();

			if (name.startsWith(".") || name.startsWith("webrtc") || name.equals("Library")) {
				return FileVisitResult.SKIP_SUBTREE;
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			return FileVisitResult.CONTINUE;
		}

		public List<Path> getFoundFiles() {
			return foundList;
		}

		private void find(Path file) {
			Path name = file.getFileName();

			if (nonNull(name) && matcher.matches(name)) {
				foundList.add(file);
			}
		}
	}
}
