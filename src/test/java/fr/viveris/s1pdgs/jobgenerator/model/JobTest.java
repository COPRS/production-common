package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataResult;
import fr.viveris.s1pdgs.jobgenerator.model.product.AbstractProduct;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object Job
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		ProductImpl product = new ProductImpl("id", "sat", "mission", null, null, "object-product", "");
		JobOrder order = new JobOrder();
		SearchMetadataQuery query1 = new SearchMetadataQuery();
		query1.setIdentifier(2);
		SearchMetadataQuery query2 = new SearchMetadataQuery();
		query2.setIdentifier(15);
		SearchMetadataResult result1 = new SearchMetadataResult(query1);
		SearchMetadataResult result2 = new SearchMetadataResult(query2);
		Map<Integer, SearchMetadataResult> metadata = new HashMap<>();
		metadata.put(2, result1);
		metadata.put(15, result2);

		Job<String> job = new Job<>(product, new ResumeDetails("topic-name", "dto-object"));

		job.setWorkDirectory("working-dir");
		job.setWorkDirectoryInc(15);
		job.setJobOrder(order);
		job.setTaskTableName("task-table");
		job.getStatus().updateStatus(GenerationStatusEnum.READY);
		job.setMetadataQueries(metadata);

		String str = job.toString();
		assertTrue(str.contains("taskTableName: task-table"));
		assertTrue(str.contains("product: " + product.toString()));
		assertTrue(str.contains("jobOrder: " + order.toString()));
		assertTrue(str.contains("status: " + job.getStatus().toString()));
		assertTrue(str.contains("workDirectory: working-dir"));
		assertTrue(str.contains("workDirectoryInc: 15"));
		assertTrue(str.contains("metadataQueries: " + metadata.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(Job.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	private class ProductImpl extends AbstractProduct<String> {

		public ProductImpl(String identifier, String satelliteId, String missionId, Date startTime, Date stopTime,
				String object, String productType) {
			super(identifier, satelliteId, missionId, startTime, stopTime, object, productType);
		}

	}
}
