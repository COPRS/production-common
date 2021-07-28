package esa.s1pdgs.cpoc.xbip.client.sardine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;

import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public class TestSardineXbipClient {
	
	private final static String ROOT_PATH = "https://s1pro-mock-webdav/NOMINAL/";
	
	private final static URI ROOT_URI = URI.create(ROOT_PATH);
	
	private SardineXbipClient uut;
	
	@Mock
	private Sardine sardine;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		uut = new SardineXbipClient(sardine, ROOT_URI, true);
	}
	
	@Test
	public void listAllRecursively_file() throws IOException {
		
		List<DavResource> files = new ArrayList<>();
		
		DavResource file1 = mock(DavResource.class);
		when(file1.getName()).thenReturn("file1");
		when(file1.getPath()).thenReturn("file1");
		when(file1.isDirectory()).thenReturn(false);
		when(file1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file1.getContentLength()).thenReturn(1L);
		when(file1.getHref()).thenReturn(URI.create(ROOT_PATH + "file1"));
	    files.add(file1);
	    
	    when(sardine.list(ROOT_PATH, 1)).thenReturn(files);
	    
	    List<XbipEntry> result = uut.listAllRecursively(ROOT_URI.toString(), XbipEntryFilter.ALLOW_ALL);
	    assertNotNull(result);
	    assertEquals(1, result.size());
	    assertEquals("file1", result.get(0).getName());
	}
	
	@Test
	public void listAllRecursively_empty_directory() throws IOException {
		
		List<DavResource> files = new ArrayList<>();
		
		DavResource dir1 = mock(DavResource.class);
		when(dir1.getName()).thenReturn("dir1");
		when(dir1.getPath()).thenReturn("dir1");
		when(dir1.isDirectory()).thenReturn(true);
		when(dir1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir1.getContentLength()).thenReturn(-1L);
		when(dir1.getHref()).thenReturn(URI.create(ROOT_PATH + "dir1"));
	    files.add(dir1);
	    
	    when(sardine.list(ROOT_PATH, 1)).thenReturn(files);
	    
	    List<XbipEntry> result = uut.listAllRecursively(ROOT_URI.toString(), XbipEntryFilter.ALLOW_ALL);
	    assertNotNull(result);
	    assertEquals(0, result.size());
		
	}
	
	@Test
	public void listAllRecursively_empty_directories_1() throws IOException {
		
	    DavResource dir1 = mock(DavResource.class);
		when(dir1.getName()).thenReturn("dir1");
		when(dir1.getPath()).thenReturn("dir1");
		when(dir1.isDirectory()).thenReturn(true);
		when(dir1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir1.getContentLength()).thenReturn(1L);
		when(dir1.getHref()).thenReturn(URI.create(ROOT_PATH + "dir1"));
	    
	    DavResource dir2 = mock(DavResource.class);
		when(dir2.getName()).thenReturn("dir2");
		when(dir2.getPath()).thenReturn("dir2");
		when(dir2.isDirectory()).thenReturn(true);
		when(dir2.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir2.getContentLength()).thenReturn(-1L);
		when(dir2.getHref()).thenReturn(URI.create(ROOT_PATH + "dir2"));
	    
	    DavResource dir3 = mock(DavResource.class);
		when(dir3.getName()).thenReturn("dir3");
		when(dir3.getPath()).thenReturn("dir3");
		when(dir3.isDirectory()).thenReturn(true);
		when(dir3.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir3.getContentLength()).thenReturn(0L);
		when(dir3.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3"));
	    
	    DavResource dir4 = mock(DavResource.class);
		when(dir4.getName()).thenReturn("dir4");
		when(dir4.getPath()).thenReturn("dir3/dir4");
		when(dir4.isDirectory()).thenReturn(true);
		when(dir4.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir4.getContentLength()).thenReturn(-1L);
		when(dir4.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3/dir4"));
		
		DavResource file1 = mock(DavResource.class);
		when(file1.getName()).thenReturn("file1");
		when(file1.getPath()).thenReturn("dir1/file1");
		when(file1.isDirectory()).thenReturn(false);
		when(file1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file1.getContentLength()).thenReturn(1L);
		when(file1.getHref()).thenReturn(URI.create(ROOT_PATH + "dir1/file1"));

	    
	    when(sardine.list(ROOT_PATH, 1)).thenReturn(Arrays.asList(dir1, dir2, dir3));
	    when(sardine.list(uut.toUri(dir1).toString(), 1)).thenReturn(Arrays.asList(file1));
	    when(sardine.list(uut.toUri(dir2).toString(), 1)).thenReturn(Arrays.asList(dir2));
	    when(sardine.list(uut.toUri(dir3).toString(), 1)).thenReturn(Arrays.asList(dir4));
	    when(sardine.list(uut.toUri(dir4).toString(), 1)).thenReturn(Arrays.asList(dir4));
	    
	    List<XbipEntry> result = uut.listAllRecursively(ROOT_URI.toString(), XbipEntryFilter.ALLOW_ALL);
	    assertNotNull(result);
	    assertEquals(1, result.size());
	    assertEquals("file1", result.get(0).getName());
		
	}
	
	@Test
	public void listAllRecursively_empty_directories_2() throws IOException {
		
	    DavResource dir1 = mock(DavResource.class);
		when(dir1.getName()).thenReturn("dir1");
		when(dir1.getPath()).thenReturn("dir1");
		when(dir1.isDirectory()).thenReturn(true);
		when(dir1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir1.getContentLength()).thenReturn(1L);
		when(dir1.getHref()).thenReturn(URI.create(ROOT_PATH + "dir1"));
	    
	    DavResource dir2 = mock(DavResource.class);
		when(dir2.getName()).thenReturn("dir2");
		when(dir2.getPath()).thenReturn("dir2");
		when(dir2.isDirectory()).thenReturn(true);
		when(dir2.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir2.getContentLength()).thenReturn(0L);
		when(dir2.getHref()).thenReturn(URI.create(ROOT_PATH + "dir2"));
	    
	    DavResource dir3 = mock(DavResource.class);
		when(dir3.getName()).thenReturn("dir3");
		when(dir3.getPath()).thenReturn("dir3");
		when(dir3.isDirectory()).thenReturn(true);
		when(dir3.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir3.getContentLength()).thenReturn(0L);
		when(dir3.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3"));
	    
	    DavResource dir4 = mock(DavResource.class);
		when(dir4.getName()).thenReturn("dir4");
		when(dir4.getPath()).thenReturn("dir3/dir4");
		when(dir4.isDirectory()).thenReturn(true);
		when(dir4.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir4.getContentLength()).thenReturn(1L);
		when(dir4.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3/dir4"));
		
		DavResource dir5 = mock(DavResource.class);
		when(dir5.getName()).thenReturn("dir5");
		when(dir5.getPath()).thenReturn("dir3/dir4/dir5");
		when(dir5.isDirectory()).thenReturn(true);
		when(dir5.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(dir5.getContentLength()).thenReturn(-1L);
		when(dir5.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3/dir4/dir5"));
		
		DavResource file1 = mock(DavResource.class);
		when(file1.getName()).thenReturn("file1");
		when(file1.getPath()).thenReturn("dir1/file1");
		when(file1.isDirectory()).thenReturn(false);
		when(file1.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file1.getContentLength()).thenReturn(1L);
		when(file1.getHref()).thenReturn(URI.create(ROOT_PATH + "dir1/file1"));
	    
	    DavResource file2 = mock(DavResource.class);
		when(file2.getName()).thenReturn(".file2");
		when(file2.getPath()).thenReturn("dir2/.file2");
		when(file2.isDirectory()).thenReturn(false);
		when(file2.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file2.getContentLength()).thenReturn(0L);
		when(file2.getHref()).thenReturn(URI.create(ROOT_PATH + "dir2/.file2"));
	    
	    DavResource file3 = mock(DavResource.class);
		when(file3.getName()).thenReturn("file3");
		when(file3.getPath()).thenReturn("dir3/dir4/file3");
		when(file3.isDirectory()).thenReturn(false);
		when(file3.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file3.getContentLength()).thenReturn(1L);
		when(file3.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3/dir4/file3"));
	    
	    DavResource file4 = mock(DavResource.class);
		when(file4.getName()).thenReturn(".file4");
		when(file4.getPath()).thenReturn("dir3/dir4/.file4");
		when(file4.isDirectory()).thenReturn(false);
		when(file4.getModified()).thenReturn(new Date(System.currentTimeMillis()));
		when(file4.getContentLength()).thenReturn(0L);
		when(file4.getHref()).thenReturn(URI.create(ROOT_PATH + "dir3/dir4/.file4"));

	    
	    when(sardine.list(ROOT_PATH, 1)).thenReturn(Arrays.asList(dir1, dir2, dir3));
	    when(sardine.list(uut.toUri(dir1).toString(), 1)).thenReturn(Arrays.asList(file1));
	    when(sardine.list(uut.toUri(dir2).toString(), 1)).thenReturn(Arrays.asList(file2));
	    when(sardine.list(uut.toUri(dir3).toString(), 1)).thenReturn(Arrays.asList(dir4));
	    when(sardine.list(uut.toUri(dir4).toString(), 1)).thenReturn(Arrays.asList(file3, file4, dir5));
	    when(sardine.list(uut.toUri(dir5).toString(), 1)).thenReturn(Arrays.asList(dir5));
	    
	    List<XbipEntry> result = uut.listAllRecursively(ROOT_URI.toString(), XbipEntryFilter.ALLOW_ALL);
	    assertNotNull(result);
	    assertEquals(2, result.size());
	    assertEquals("file1", result.get(0).getName());
	    assertEquals("file3", result.get(1).getName());
	    assertEquals(Paths.get("dir3/dir4/file3"), result.get(1).getPath());
	    assertEquals(URI.create(ROOT_PATH + "dir3/dir4/file3"), result.get(1).getUri());
		
	}
	
	

}
