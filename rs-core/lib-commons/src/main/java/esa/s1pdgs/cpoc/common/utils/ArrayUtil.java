package esa.s1pdgs.cpoc.common.utils;

/**
 * Utils for arrays
 */
public final class ArrayUtil {

	public static boolean isEmpty(Object[] array) {
		return (array == null) || (array.length == 0);
	}

	public static boolean isNotEmpty(Object[] array) {
		return (array != null) && (array.length > 0);
	}

	@SafeVarargs
	public static <T> T[] toArray(T... elements) {
		return elements;
	}

	public static <T> T[] emptyArray() {
		return toArray();
	}

	public static <T> T[] nullToEmpty(T[] array) {
		return (array != null) ? array : emptyArray();
	}

	// --------------------------------------------------------------------------

	private ArrayUtil() {
	}

}
