package esa.s1pdgs.cpoc.scaler.k8s.model.converter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extended converter for converting object and list of object
 * @author Cyrielle Gailliard
 *
 * @param <A> from object of the conversion
 * @param <B> to object of the conversion
 */
public interface SuperConverter<A, B> extends Function<A, B> {
	
	/**
	 * Convert a list of A into a list of B
	 * @param input
	 * @return
	 */
    default List<B> convertToList(final List<A> input) {
        return input.stream().map(this::apply).collect(Collectors.<B>toList());
    }
}
