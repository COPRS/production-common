package fr.viveris.s1pdgs.level0.wrapper.services.task;

public class TaskCallableTest {
	
	/*@Test
	public void testRun() throws InterruptedException, ExecutionException {
		assertTrue(!(new File("./titi")).exists());
		ExecutorService service = Executors.newSingleThreadExecutor();
		CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(service);
		completionService.submit(new TaskCallable("mkdir", "titi", "./"));
		Future<TaskResult> future = completionService.take();
		TaskResult r = future.get();
		assertEquals("mkdir", r.getBinary());
		assertEquals(0, r.getExitCode());
		assertTrue((new File("./titi")).isDirectory());
		
		completionService.submit(new TaskCallable("rmdir", "titi", "./"));
		completionService.take();
		assertTrue(!(new File("./titi")).exists());
	}
	
	@Test
	public void testExitCode() throws InterruptedException, ExecutionException {
		// Command dir/ls
		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		String command = "ls";
		if (isWindows) {
			command = "dir";
		}
		// Test when folder do not exist
		ExecutorService service = Executors.newSingleThreadExecutor();
		CompletionService<TaskResult> completionService = new ExecutorCompletionService<>(service);
		completionService.submit(new TaskCallable(command, "not_exist", "./src/main/"));
		Future<TaskResult> future = completionService.take();
		TaskResult r = future.get();
		assertEquals(1, r.getExitCode());
		// Test when folder exist
		completionService.submit(new TaskCallable(command, "resources", "./src/main/"));
		Future<TaskResult> future2 = completionService.take();
		TaskResult r2 = future2.get();
		assertEquals(0, r2.getExitCode());
	}*/

}
