package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.List;

import org.springframework.data.domain.Sort;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;

public interface AppDataJobRepositoryCustom {

    public List<AppDataJob> search(List<FilterCriterion> filters,
            ProductCategory category, Sort sort);

    public void udpateJobGeneration(Long jobId,
            AppDataJobGeneration newGeneration);
}
