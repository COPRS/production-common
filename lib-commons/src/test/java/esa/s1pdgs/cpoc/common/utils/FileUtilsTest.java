package esa.s1pdgs.cpoc.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;

public class FileUtilsTest { 
	private final File testDir = FileUtils.createTmpDir();
	
	@After
    public void clean() {
        FileUtils.delete(testDir.getPath());        
    }
	
//    
//    @Before
//    public void init() throws InternalErrorException {
//        final File testFile = new File("test-file.xml");
//        FileUtils.writeFile("test-file.xml", "Helloword !");
//    }
    
    

    @Test
    public void testWriteFileWithPath() throws InternalErrorException {
        final File file = new File(testDir, "test.xml");
        assertFalse(file.exists());
        FileUtils.writeFile(file.getPath(), "Ceci est une chaine de test");
        assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testWriteFile() throws InternalErrorException {
        final File file = new File(testDir, "test.xml");
        assertFalse(file.exists());
        FileUtils.writeFile(file, "Ceci est une chaine de test");
        assertTrue(file.exists());
        file.delete();
    }

    @Test(expected = InternalErrorException.class)
    public void testWriteFileWhenDirectoryNotExist() throws InternalErrorException {
        FileUtils.writeFile(new File("tutu/test.xml"), "Ceci est une chaine de test");
    }

    @Test
    public void testReadFile() throws InternalErrorException {
    	final File file = new File(testDir, "test.xml");    	
        FileUtils.writeFile(file, "Ceci est une chaine de test");
        String ret = FileUtils.readFile(file);
        assertEquals("Ceci est une chaine de test", ret);
        file.delete();
    }

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
    
    @Test
    public final void testSize() throws Exception {
    	final File file = new File(testDir, "test.xml"); 
        FileUtils.writeFile(file, "Ceci est une chaine de test");
       	final File subdir = new File(testDir, "testDir");
       	assertTrue(subdir.mkdir());
    	final File file2 = new File(subdir, "test.xml");
    	FileUtils.writeFile(file2, "Ceci est une chaine de test");       			
    	System.out.println(FileUtils.size(testDir));
        
    }
}
