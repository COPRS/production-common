package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;

@Repository
public class AppDataJobRepositoryImpl implements AppDataJobRepositoryCustom {
	
	
    private final MongoTemplate mongoTemplate;

    @Autowired
    public AppDataJobRepositoryImpl(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<AppDataJob> search(final List<FilterCriterion> filters, final Sort sort) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();
        for (final FilterCriterion criterion : filters) {
            switch (criterion.getOperator()) {
                case LT:
                    criteria.add(Criteria.where(criterion.getKey()).
                    		lt(criterion.getValue()));
                    break;
                case LTE:
                    criteria.add(Criteria.where(criterion.getKey())
                            .lte(criterion.getValue()));
                    break;
                case GT:
                    criteria.add(Criteria.where(criterion.getKey())
                            .gt(criterion.getValue()));
                    break;
                case GTE:
                    criteria.add(Criteria.where(criterion.getKey())
                            .gte(criterion.getValue()));
                    break;
                case NEQ:
                    criteria.add(Criteria.where(criterion.getKey())
                            .ne(criterion.getValue()));
                    break;
                default:
                    criteria.add(Criteria.where(criterion.getKey())
                            .is(criterion.getValue()));
                    break;
            }
        }
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteria.toArray(new Criteria[criteria.size()])));
        }
        if (sort != null) {
            query.with(sort);
        }
        return mongoTemplate.find(query, AppDataJob.class);
    }
}
