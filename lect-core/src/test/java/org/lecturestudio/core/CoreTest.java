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

package org.lecturestudio.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.audio.DummyAudioSystemProvider;

public abstract class CoreTest {

	protected AudioSystemProvider audioSystemProvider;

	protected Path testPath;

	protected Dictionary dict;

	@BeforeEach
	public void init() {
		audioSystemProvider = new DummyAudioSystemProvider();
	}

	@BeforeEach
	public void setUpDictionary() {
		dict = new Dictionary() {

			@Override
			public String get(String key) throws NullPointerException {
				return key;
			}

			@Override
			public boolean contains(String key) {
				return true;
			}
		};
	}

	protected void deletePath(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.deleteIfExists(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.deleteIfExists(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	protected Path getResourcePath(String path) throws URISyntaxException {
		return Path.of(Objects.requireNonNull(
				getClass().getClassLoader().getResource(path)).toURI());
	}


	/**
	 * Pauses execution until either the supplied function returns true or the timeout runs out.
	 * Can be used as a convenience method to wait for async tasks to complete.
	 *
	 * @param booleanSupplier  Waits for this function to return true
	 * @param timeoutInSeconds The timeout in seconds
	 * @return True, if the boolean function returns true and false if the timeout runs out.
	 * @throws InterruptedException
	 */
	protected boolean awaitTrue(Supplier<Boolean> booleanSupplier, int timeoutInSeconds) throws InterruptedException {
		CountDownLatch trueLatch = new CountDownLatch(1);
		CompletableFuture.runAsync(() -> {
			while (!booleanSupplier.get()) {
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			trueLatch.countDown();
		});

		return trueLatch.await(timeoutInSeconds, TimeUnit.SECONDS);
	}

	protected abstract ApplicationContext getApplicationContext();
}
