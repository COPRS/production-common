package fr.viveris.s1pdgs.level0.wrapper.services.task;

public class PoolProcessorTest {

	/*@Test
	public void testExecutionOk() {
		JobPoolDto dto = new JobPoolDto();
		dto.addTask(new JobTaskDto("mkdir"));
		PoolProcessor processor = new PoolProcessor(dto, "titi", "./");
		try {
			assertTrue(!(new File("./titi")).exists());
			processor.process();
			assertTrue((new File("./titi")).isDirectory());
		} catch (InterruptedException | ExecutionException e) {
			fail("Exception " + e.getMessage());
		}

		JobPoolDto dto1 = new JobPoolDto();
		dto1.addTask(new JobTaskDto("rmdir"));
		PoolProcessor processor1 = new PoolProcessor(dto1, "titi", "./");
		try {
			assertTrue((new File("./titi")).isDirectory());
			processor1.process();
			assertTrue(!(new File("./titi")).exists());
		} catch (InterruptedException | ExecutionException e) {
			fail("Exception " + e.getMessage());
		}
	}*/
}
