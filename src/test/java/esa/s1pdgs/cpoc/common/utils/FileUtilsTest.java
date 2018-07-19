package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

public class FileUtilsTest {
    
    private File testFile;
    
    @Before
    public void init() throws InternalErrorException {
        testFile = new File("test-file.xml");
        FileUtils.writeFile("test-file.xml", "Helloword !");
    }
    
    @After
    public void clean() {
        testFile.delete();
    }

    @Test
    public void testWriteFileWithPath() throws InternalErrorException {
        File file = new File("test.xml");
        assertFalse(file.exists());
        FileUtils.writeFile("test.xml", "Ceci est une chaine de test");
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testWriteFile() throws InternalErrorException {
        File file = new File("test.xml");
        assertFalse(file.exists());
        FileUtils.writeFile(file, "Ceci est une chaine de test");
        assertTrue(file.exists());
        file.delete();
    }

    @Test(expected = InternalErrorException.class)
    public void testWriteFileWhenDirectoryNotExist()
            throws InternalErrorException {
        File file = new File("tutu/test.xml");
        FileUtils.writeFile(file, "Ceci est une chaine de test");
    }

    /**@Test
    public void testWriteFileWhenLock()
            throws InternalErrorException, IOException {
        RandomAccessFile raFile = new RandomAccessFile("test-file.xml", "rw");
        raFile.getChannel().lock();
        try {
            FileUtils.writeFile(testFile, "Ceci est une chaine de test");
            fail("An exception shall be raised");
        } catch (InternalErrorException iee) {

        } finally {
            raFile.close();
        }
    }*/

    @Test
    public void testReadFile() throws InternalErrorException {
        FileUtils.writeFile("test.xml", "Ceci est une chaine de test");
        String ret = FileUtils.readFile(new File("test.xml"));
        assertEquals("Ceci est une chaine de test", ret);
        (new File("test.xml")).delete();
    }

    /**@Test
    public void testReadFileWhenException() throws InternalErrorException, IOException {
        RandomAccessFile raFile = new RandomAccessFile("test-file.xml", "rw");
        raFile.getChannel().lock();
        try {
            FileUtils.readFile(testFile);
            fail("An exception shall be raised");
        } catch (InternalErrorException iee) {

        } finally {
            raFile.close();
        }
    }*/

    @Test
    public void testDelete() throws IOException {
        (new File("dir1/dir2")).mkdirs();
        (new File("dir1/dir3")).mkdirs();
        (new File("dir1/file1")).createNewFile();
        (new File("dir1/file2")).createNewFile();
        (new File("dir1/dir2/file3")).createNewFile();
        (new File("dir1/dir3/file3")).createNewFile();
        (new File("dir1/dir3/file4")).createNewFile();
        assertTrue((new File("dir1")).exists());

        FileUtils.delete("dir1");
        assertFalse((new File("dir1")).exists());
    }
}
