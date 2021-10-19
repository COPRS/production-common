package esa.s1pdgs.cpoc.common.filter;

/**
 * Filter utilities
 * @author Viveris Technologies
 *
 */
public class FilterUtils {

    /**
     * Extract filter criterion from key and value
     * @param filterKey
     * @param filterValue
     * @return
     */
    public static FilterCriterion extractCriterion(final String filterKey,
            final Object filterValue) {
        FilterCriterion criterion = new FilterCriterion(filterKey, filterValue);
        for (FilterOperator op : FilterOperator.values()) {
            if (filterKey.endsWith("[" + op.name().toLowerCase() + "]")) {
                criterion =
                        new FilterCriterion(
                                filterKey.substring(0,
                                        filterKey.length()
                                                - op.name().length() - 2),
                                filterValue, op);
                break;
            }
        }
        return criterion;
    }
}
