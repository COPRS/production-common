package esa.s1pdgs.cpoc.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;

/**
 * Utils for strings
 */
public final class StringUtil {

	public static final String EMPTY = "";

	// --------------------------------------------------------------------------

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isNotEmpty(String str) {
		return str != null && !str.isEmpty();
	}

	public static boolean isBlank(String str) {
		return trimToNull(str) == null;
	}

	public static boolean isNotBlank(String str) {
		return trimToNull(str) != null;
	}

	public static String getNotEmptyOrDefault(String str, String dflt) {
		if (isNotEmpty(str)) {
			return str;
		} else {
			return dflt;
		}
	}

	public static String trimToEmpty(String str) {
		return (isEmpty(str)) ? EMPTY : str.trim();
	}

	public static String trimToNull(String str) {
		if (str == null) {
			return null;
		}

		str = str.trim();
		return (str.length() > 0) ? str : null;
	}

	public static String makeListString(String separator, Object... elements) {
		return makeListString(separator, CollectionUtil.toList(elements));
	}

	public static String makeListString(String separator, Collection<?> elements) {
		final StringBuilder sb = new StringBuilder();

		if (CollectionUtil.isNotEmpty(elements)) {
			separator = getNotEmptyOrDefault(separator, ",");

			for (Object obj : elements) {
				String str = obj.toString();
				str = removeLeading(str, separator, " ");
				str = removeTrailing(str, separator, " ");

				if (isEmpty(str)) {
					continue;
				}

				sb.append(str).append(separator).append(" ");
			}
		}

		return removeTrailing(sb.toString(), separator, " ");
	}

	public static String concatenate(String separator, Object... elements) {
		return concatenate(separator, CollectionUtil.toList(elements));
	}

	public static String concatenate(String separator, Collection<?> elements) {
		final StringBuilder sb = new StringBuilder();

		if (CollectionUtil.isNotEmpty(elements)) {
			separator = getNotEmptyOrDefault(separator, ".");

			for (Object obj : elements) {
				String str = obj.toString().trim();
				str = removeLeading(str, separator);
				str = removeTrailing(str, separator);

				if (isEmpty(str)) {
					continue;
				}

				sb.append(str).append(separator);
			}
		}

		return removeTrailing(sb.toString(), separator);
	}

	public static String removeTrailing(String str, String... remove) {
		if (isNotEmpty(str) && ArrayUtil.isNotEmpty(remove)) {
			Optional<String> oEndsWith;
			while ((oEndsWith = getEndsWith(str, remove)).isPresent()) {
				str = str.substring(0, str.length() - oEndsWith.get().length());

				if (str.isEmpty()) {
					break;
				}
			}
		}

		return str;
	}

	public static String removeLeading(String str, String... remove) {
		if (isNotEmpty(str) && ArrayUtil.isNotEmpty(remove)) {
			Optional<String> oStartsWith;
			while ((oStartsWith = getStartsWith(str, remove)).isPresent()) {
				str = str.substring(oStartsWith.get().length());

				if (str.isEmpty()) {
					break;
				}
			}
		}

		return str;
	}

	public static Optional<String> getEndsWith(String str, String... endsWith) {
		if (isNotEmpty(str) && ArrayUtil.isNotEmpty(endsWith)) {
			for (String endsStr : endsWith) {
				if (isNotEmpty(endsStr) && str.endsWith(endsStr)) {
					return Optional.of(endsStr);
				}
			}
		}

		return Optional.empty();
	}

	public static boolean endsWith(String str, String... endsWith) {
		return getEndsWith(str, endsWith).isPresent();
	}

	public static Optional<String> getStartsWith(String str, String... startsWith) {
		if (isNotEmpty(str) && ArrayUtil.isNotEmpty(startsWith)) {
			for (String startsStr : startsWith) {
				if (isNotEmpty(startsStr) && str.startsWith(startsStr)) {
					return Optional.of(startsStr);
				}
			}
		}

		return Optional.empty();
	}

	public static boolean startsWith(String str, String... startsWith) {
		return getStartsWith(str, startsWith).isPresent();
	}

	public static String stackTraceToString(Throwable e) {
		final StringWriter sw = new StringWriter();
		final String str;

		try (final PrintWriter pw = new PrintWriter(sw)) {
			e.printStackTrace(pw);
			str = sw.toString();
		}

		return str;
	}

	/**
	 * Returns the part of the given string up to the first occurrence of the given
	 * upTo string.
	 */
	public static Optional<String> getStringUpTo(String str, String upTo) {
		if (isNotEmpty(str) && isNotEmpty(upTo)) {
			final int endIndex = str.indexOf(upTo);

			if (endIndex > 1) {
				return Optional.of(str.substring(0, endIndex));
			}
		}

		return Optional.empty();
	}

	// --------------------------------------------------------------------------

	private StringUtil() {
	}

}
