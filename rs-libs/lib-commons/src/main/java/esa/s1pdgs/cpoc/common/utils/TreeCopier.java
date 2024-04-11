/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.common.utils;

import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.nio.file.FileVisitResult.*;
import java.io.IOException;

/**
 * @author birol_colak@net.werum
 *
 */
public class TreeCopier implements FileVisitor<Path> {

	private static final Logger LOGGER = LogManager.getLogger(TreeCopier.class);

	private final Path source;
	private final Path target;
	private final boolean preserve;
	private final boolean replaceExisting;

	/**
	 * Copies directories and files recursively
	 * 
	 * @param source - as {@link Path}
	 * @param target - as {@link Path}
	 * @param preserve - if to preserve file attributes
	 * @param replaceExisting - if to replace existing files in target
	 */
	public TreeCopier(Path source, Path target, boolean preserve, boolean replaceExisting) {
		this.source = source;
		this.target = target;
		this.preserve = preserve;
		this.replaceExisting = replaceExisting;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

		CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];

		Path newdir = target.resolve(source.relativize(dir));
		try {
			Files.copy(dir, newdir, options);
		} catch (FileAlreadyExistsException x) {
			// ignore
		} catch (IOException x) {
			LOGGER.error("Unable to create: {}: {}", newdir, x);
			return SKIP_SUBTREE;
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		copyFile(file, target.resolve(source.relativize(file)), preserve);
		return CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		// fix up modification time of directory when done
		if (exc == null && preserve) {
			Path newdir = target.resolve(source.relativize(dir));
			try {
				FileTime time = Files.getLastModifiedTime(dir);
				Files.setLastModifiedTime(newdir, time);
			} catch (IOException x) {
				LOGGER.error("Unable to copy all attributes to: {}: {}", newdir, x);
			}
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		if (exc instanceof FileSystemLoopException) {
			LOGGER.error("cycle detected: {}", file);
		} else {
			LOGGER.error("Unable to copy: {}: {}", file, exc);
		}
		return CONTINUE;
	}

	void copyFile(Path source, Path target, boolean preserve) throws IOException {
		CopyOption[] options = (preserve) ? new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING }
				: new CopyOption[] { REPLACE_EXISTING };
		if (replaceExisting || Files.notExists(target)) {
			try {
				Files.copy(source, target, options);
			} catch (IOException x) {
				LOGGER.error("Unable to copy: {}: {}", source, x);
				throw x;
			}
		}
	}

}
