package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProcParam;

public abstract class AbstractProductTypeAdapter implements ProductTypeAdapter {		
    
	protected final AppDataJob toAppDataJob(final IpfPreparationJob prepJob) {
        final AppDataJob job = new AppDataJob();
        job.setLevel(prepJob.getLevel());
        job.setPod(prepJob.getHostname());
        job.getMessages().add(prepJob.getEventMessage());
        job.setProduct(newProductFor(prepJob.getEventMessage())); 
    	job.setTaskTableName(prepJob.getTaskTableName());     
    	job.setStartTime(prepJob.getStartTime());
    	job.setStopTime(prepJob.getStopTime());
    	job.setProductName(prepJob.getKeyObjectStorage());     
    	return job;    	
	}
	
	protected final void updateProcParam(final JobOrder jobOrder, final String name, final String newValue) {
        if (jobOrder.getConf().getProcParams() == null) {
        	return;
        }
        
        boolean update = false;
        for (final JobOrderProcParam param : jobOrder.getConf().getProcParams()) {
            if (name.equals(param.getName())) {
                param.setValue(newValue);
                update = true;
            }
        }
        if (!update) {
            jobOrder.getConf().addProcParam(new JobOrderProcParam(name, newValue));
        }
    }
	
	private final AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();
	    final AppDataJobProduct productDto = new AppDataJobProduct();
	    
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(mqiMessage);		
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put("missionId", eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		// S1PRO-1772: user productSensing accessors here to make start/stop optional here (RAWs don't have them)
		productDto.getMetadata().put("startTime", eventAdapter.productSensingStartDate());
		productDto.getMetadata().put("stopTime", eventAdapter.productSensingStopDate());     
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
	    return productDto;
	}
}
