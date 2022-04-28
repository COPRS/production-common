package esa.s1pdgs.cpoc.ingestion.filter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;
import esa.s1pdgs.cpoc.mqi.model.control.DemandType;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("esa.s1pdgs.cpoc")
public class ITIngestionFilter {

	@Autowired
	private IngestionFilterService ingestionFilterService;

	@Test
	public void testIngestionFilterService() {
		List<IngestionJob> ingestionJobs = new ArrayList<>();

		IngestionJob jobThatShallBeAccepted = new IngestionJob();
		jobThatShallBeAccepted.setMissionId("S3");
		jobThatShallBeAccepted.setProductFamily(ProductFamily.AUXILIARY_FILE);
		jobThatShallBeAccepted.setKeyObjectStorage("S3__AX___MA1_AX_20150117T210000_20150118T090000_20150118T054046___________________ECW___SN____.SEN3");
		jobThatShallBeAccepted.setUid(UUID.fromString("00000000-0000-000c-0000-000000000001"));
		jobThatShallBeAccepted.setCreationDate(DateUtils.toDate("2015-01-18T05:40:45.000Z"));
		jobThatShallBeAccepted.setAllowedActions(Arrays.asList(AllowedAction.RESTART));
		jobThatShallBeAccepted.setDemandType(DemandType.NOMINAL);
		jobThatShallBeAccepted.setPickupBaseURL("file:///data/inbox/AUX");
		jobThatShallBeAccepted.setInboxType("file");
		jobThatShallBeAccepted.setRelativePath("S3__AX___MA1_AX_20150117T210000_20150118T090000_20150118T054046___________________ECW___SN____.SEN3");
		jobThatShallBeAccepted.setProductName("S3__AX___MA1_AX_20150117T210000_20150118T090000_20150118T054046___________________ECW___SN____.SEN3");
		jobThatShallBeAccepted.setProductSizeByte(415447L);
		jobThatShallBeAccepted.setLastModified(Date.from(DateUtils.parse("2021-11-24T08:00:00").toInstant(ZoneOffset.UTC)));
		ingestionJobs.add(jobThatShallBeAccepted);

		IngestionJob jobThatShallBeRected = new IngestionJob();
		jobThatShallBeRected.setMissionId("S3");
		jobThatShallBeRected.setProductFamily(ProductFamily.AUXILIARY_FILE);
		jobThatShallBeRected.setKeyObjectStorage("S3__AX___MA1_AX_20150118T030000_20150118T150000_20150118T173546___________________ECW___SN____.SEN3");
		jobThatShallBeRected.setUid(UUID.fromString("00000000-0000-000c-0000-000000000002"));
		jobThatShallBeRected.setCreationDate(DateUtils.toDate("2015-01-18T17:35:46.000Z"));
		jobThatShallBeRected.setAllowedActions(Arrays.asList(AllowedAction.RESTART));
		jobThatShallBeRected.setDemandType(DemandType.NOMINAL);
		jobThatShallBeRected.setPickupBaseURL("file:///data/inbox/AUX");
		jobThatShallBeRected.setInboxType("file");
		jobThatShallBeRected.setRelativePath("S3__AX___MA1_AX_20150118T030000_20150118T150000_20150118T173546___________________ECW___SN____.SEN3");
		jobThatShallBeRected.setProductName("S3__AX___MA1_AX_20150118T030000_20150118T150000_20150118T173546___________________ECW___SN____.SEN3");
		jobThatShallBeRected.setProductSizeByte(415447L);
		jobThatShallBeRected.setLastModified(Date.from(DateUtils.parse("2021-11-24T09:00:00").toInstant(ZoneOffset.UTC)));
		ingestionJobs.add(jobThatShallBeRected);

		List<Message<IngestionJob>> filteredJobs = ingestionFilterService.apply(ingestionJobs);

		assertEquals(1, filteredJobs.size());
		assertEquals(jobThatShallBeAccepted, filteredJobs.get(0).getPayload());
	}

}
