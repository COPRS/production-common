/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterOperator;

/**
 * Test class for app data job repository implementation
 *
 * @author Viveris Technologies
 */
public class AppDataJobRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;
    
    private AppDataJobRepositoryImpl appDataJobRepositoryImpl;
    
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.appDataJobRepositoryImpl = new AppDataJobRepositoryImpl(mongoTemplate);
    }
    
    @Test 
    public void searchTest() {
        List<FilterCriterion> filterCriterion = new ArrayList<>();
        filterCriterion.add(new FilterCriterion("key-filter", 1, FilterOperator.LTE));
        filterCriterion.add(new FilterCriterion("key-filter2", 2, FilterOperator.LT));
        filterCriterion.add(new FilterCriterion("key-filter3", 3, FilterOperator.GTE));
        filterCriterion.add(new FilterCriterion("key-filter4", 4, FilterOperator.GT));
        filterCriterion.add(new FilterCriterion("key-filter5", 5, FilterOperator.NEQ));
        filterCriterion.add(new FilterCriterion("key-filter6", 6, FilterOperator.EQ));
        
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("category").is(ProductCategory.LEVEL_JOBS));
        criteria.add(Criteria.where("key-filter").lte(1));
        criteria.add(Criteria.where("key-filter2").lt(2));
        criteria.add(Criteria.where("key-filter3").gte(3));
        criteria.add(Criteria.where("key-filter4").gt(4));
        criteria.add(Criteria.where("key-filter5").ne(5));
        criteria.add(Criteria.where("key-filter6").is(6));
        
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                criteria.toArray(new Criteria[criteria.size()])));
        
        Sort sort = new Sort(Direction.ASC, "valueFilter");
        
        query.with(sort);
        
        doReturn(new ArrayList<AppDataJob>()).when(mongoTemplate).find(Mockito.any(), Mockito.any());
        this.appDataJobRepositoryImpl.search(filterCriterion, ProductCategory.LEVEL_JOBS, sort);
        
        verify(mongoTemplate, times(1)).find(Mockito.eq(query), Mockito.eq(AppDataJob.class));
        
        
    }
    
}
