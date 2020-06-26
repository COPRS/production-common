package esa.s1pdgs.cpoc.ipf.preparation.worker.type;

import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.JobsGeneratorFactory.JobGenType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.RoutingBasedTasktableMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;

public class LevelProductTypeFactory extends AbstractProductTypeFactory implements ProductTypeFactory  {	
	public LevelProductTypeFactory(final TasktableMapper taskTableMapper) {
		super(JobGenType.LEVEL_PRODUCT, taskTableMapper);	
	}
	
	public static final LevelProductTypeFactory newInstance(final XmlConverter converter, final String file) {
		final TasktableMapper mapper = new RoutingBasedTasktableMapper.Factory(converter, file)
			.newMapper();
		return new LevelProductTypeFactory(mapper);
	}

	@Override
	public ProductTypeAdapter typeAdapter() {
		return null;
	}
}
