/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.type.spp;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;

public class SppMbuTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SppMbuTypeAdapter.class);

    private final MetadataClient metadataClient;
    
    public SppMbuTypeAdapter(
			final MetadataClient metadataClient
	) {
		this.metadataClient = metadataClient;
	}

    @Override
    public Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdapter) {
    	final L2Product product = L2Product.of(job);
    	try {
			final SearchMetadata metadata = metadataClient.queryByFamilyAndProductName(ProductFamily.L2_SLICE.name(), product.getProductName());
			product.setStartTime(metadata.getValidityStart());
			product.setStopTime(metadata.getValidityStop());
		} catch (final MetadataQueryException e) {
			LOGGER.debug("L2 product for {} not found in MDC (error was {}). Trying next time...", product.getProductName(), Exceptions.messageOf(e));
		}

    	final List<AppDataJobTaskInputs> appDataJobTaskInputs = QueryUtils.buildInitialInputs(tasktableAdapter);
    	final AppDataJobTaskInputs originalInput = appDataJobTaskInputs.get(0);
    	final AppDataJobInput first = originalInput.getInputs().get(0);
    	final AppDataJobFile file = new AppDataJobFile(
    			product.getProductName(),
    			product.getProductName(),
    			TaskTableAdapter.convertDateToJobOrderFormat(product.getStartTime()),
    			TaskTableAdapter.convertDateToJobOrderFormat(product.getStopTime()),
    			null
    	);
    	final AppDataJobInput mbuInput = new AppDataJobInput(
    			first.getTaskTableInputReference(),
    			product.getProductType(),
    			TaskTableFileNameType.PHYSICAL.toString(),
    			first.isMandatory(),
    			Collections.singletonList(file)
    	);
  		originalInput.setInputs(Collections.singletonList(mbuInput));
    	product.overridingInputs(appDataJobTaskInputs);
    	return product;
    }

    @Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException {
    	final L2Product product = L2Product.of(job);
    	try {
			metadataClient.queryByFamilyAndProductName(ProductFamily.L2_SLICE.name(), product.getProductName()); // just to validate that metadata exists
		} catch (final MetadataQueryException e) {
			LOGGER.debug("L2 product for {} not found in MDC (error was {}). Trying next time...", product.getProductName(), Exceptions.messageOf(e));
			throw new IpfPrepWorkerInputsMissingException(Collections.singletonMap(product.getProductName(), "No WV_OCN__2S: " + product.getProductName()));
		}
    	LOGGER.info("Found WV_OCN__2S {}", product.getProductName());
	}

	@Override
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
        return Collections.singletonList(appDataJob);
	}

	@Override
	public final void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
	}

	@Override
	public final void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {
	}
}
