package esa.s1pdgs.cpoc.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class TreeCopierTest {

	@Test
	public void testCopyFile() throws IOException {

		String pickupDirectory = "target/pickup";
		String backupDirectory = "target/backup";
		String file = "file";
		Path pickupPath = new File(pickupDirectory).toPath();
		Path backupPath = new File(backupDirectory).toPath();
		Path filePath = pickupPath.resolve(file);

		if (Files.notExists(pickupPath)) {
			Files.createDirectory(pickupPath);
		}
		if (Files.notExists(backupPath)) {
			Files.createDirectory(backupPath);
		}
		if (Files.notExists(filePath)) {
			Files.createFile(filePath);
		}

		Path productDir = pickupPath.relativize(filePath).subpath(0, 1);
		Path dirToCopy = pickupPath.resolve(productDir);
		Path target = backupPath.resolve(productDir);
		TreeCopier tc = new TreeCopier(dirToCopy, target, true, false);
		Files.walkFileTree(dirToCopy, tc);

		Files.delete(filePath);

		Assert.assertTrue(Files.notExists(filePath));
		Assert.assertTrue(Files.exists(backupPath.resolve(file)));

		Files.deleteIfExists(backupPath.resolve(file));
		Files.deleteIfExists(backupPath);
		Files.deleteIfExists(pickupPath);
	}

	@Test
	public void testCopyDirectory() throws IOException {

		String pickupDirectory = "target/pickup";
		String backupDirectory = "target/backup";
		String dir = "dir";
		String file = "file";
		Path pickupPath = new File(pickupDirectory).toPath();
		Path backupPath = new File(backupDirectory).toPath();
		Path dirPath = pickupPath.resolve(dir);
		Path filePath = dirPath.resolve(file);

		if (Files.notExists(pickupPath)) {
			Files.createDirectory(pickupPath);
		}
		if (Files.notExists(backupPath)) {
			Files.createDirectory(backupPath);
		}
		if (Files.notExists(dirPath)) {
			Files.createDirectory(dirPath);
		}
		if (Files.notExists(filePath)) {
			Files.createFile(filePath);
		}

		Path productDir = pickupPath.relativize(filePath).subpath(0, 1);
		Path dirToCopy = pickupPath.resolve(productDir);
		Path target = backupPath.resolve(productDir);
		TreeCopier tc = new TreeCopier(dirToCopy, target, true, false);
		Files.walkFileTree(dirToCopy, tc);

		Files.delete(filePath);
		Files.delete(dirPath);

		Assert.assertTrue(Files.notExists(dirPath));
		Assert.assertTrue(Files.notExists(filePath));
		Assert.assertTrue(Files.exists(backupPath.resolve(dir)));
		Assert.assertTrue(Files.exists(backupPath.resolve(dir).resolve(file)));

		Files.deleteIfExists(backupPath.resolve(dir).resolve(file));
		Files.deleteIfExists(backupPath.resolve(dir));
		Files.deleteIfExists(backupPath);
		Files.deleteIfExists(pickupPath);
	}

}
