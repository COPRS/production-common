package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RobustFtpClient {
	
	static final Logger LOG = LogManager.getLogger(RobustFtpClient.class);
	
	private final FTPClient ftpClient;
	private final CompletionService<List<FTPFile>> completionServiceList;
	private final CompletionService<Boolean> completionServiceAbort;
	private final ExecutorService executor;
	private final int timeoutSec;
	
	public RobustFtpClient(final FTPClient ftpClient, final int timeoutSec) throws SocketException {
		
		this.ftpClient = ftpClient;
		this.timeoutSec = timeoutSec;
		
		this.executor = Executors.newFixedThreadPool(5);
		this.completionServiceList = new ExecutorCompletionService<>(executor);
		this.completionServiceAbort = new ExecutorCompletionService<>(executor);
	}
	
	public List<FTPFile> listRecursively(Path path) throws InterruptedException, ExecutionException, IOException {
		List<FTPFile> result = new ArrayList<>();
		
		for (FTPFile f: this.listFiles(path)) {
			if (f.getName().startsWith("..")) {
				LOG.trace("ignoring {}", f.getName());
				continue;
			}
			if (f.isDirectory()) {
				LOG.trace("found dir {}", f.getName());
				result.addAll(this.listRecursively(path.resolve(f.getName())));
			} else {
				LOG.trace("found file {}", f.getName());
				result.add(f);
			}
		}
		
		return result;
	}
	
	public List<FTPFile> listFiles(Path path) throws InterruptedException, ExecutionException, IOException {
		Future<List<FTPFile>> submitted = completionServiceList.submit(listCall(path));
		ftpClient.setSoTimeout(timeoutSec * 1000);
		Future<List<FTPFile>> result = completionServiceList.poll(timeoutSec, TimeUnit.SECONDS);
		if (result != null) {
			return result.get();
		} else {
			LOG.warn("timeout while listing files in path {}", path);
			submitted.cancel(true);
			Future<Boolean> submittedAbort = completionServiceAbort.submit(abortCall());
			Future<Boolean> abortResult = completionServiceAbort.poll(timeoutSec, TimeUnit.SECONDS);
			if (abortResult != null) {
				LOG.warn("list command aborted: {}", abortResult.get());
			} else {
				LOG.warn("timeout while aborting list command");
				submittedAbort.cancel(true);
			}
			return Collections.emptyList();
		}
	}
	
	public void shutdownExecution() {
		executor.shutdownNow();
	}
	
	private Callable<List<FTPFile>> listCall(Path path) {
		return () -> {
			return (Arrays.asList(ftpClient.listFiles(path.toString())));
		};
	}
	
	private Callable<Boolean> abortCall() {
		return () -> {
			return ftpClient.abort();
		};
	}

}
