package esa.s1pdgs.cpoc.prip.model.filter;

/**
 * An interface for filters that can be nested.
 */
public interface NestableQueryFilter {

	void makeNested(final String path);

	boolean isNested();

	String getPath();

}
