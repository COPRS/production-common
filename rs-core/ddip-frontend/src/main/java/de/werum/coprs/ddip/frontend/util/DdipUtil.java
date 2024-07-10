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

package de.werum.coprs.ddip.frontend.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils for strings, arrays, ...
 */
public final class DdipUtil {

	public static final String EMPTY = "";

	private DdipUtil() {
	}

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

	public static boolean isNotEmpty(Object[] array) {
		return (array != null) && (array.length > 0);
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

	public static Optional<String> getEndsWith(String str, String... endsWith) {
		if (isNotEmpty(str) && isNotEmpty(endsWith)) {
			for (final String endsStr : endsWith) {
				if (isNotEmpty(endsStr) && str.endsWith(endsStr)) {
					return Optional.of(endsStr);
				}
			}
		}

		return Optional.empty();
	}

	public static Optional<String> getStartsWith(String str, String... startsWith) {
		if (isNotEmpty(str) && isNotEmpty(startsWith)) {
			for (final String startsStr : startsWith) {
				if (isNotEmpty(startsStr) && str.startsWith(startsStr)) {
					return Optional.of(startsStr);
				}
			}
		}

		return Optional.empty();
	}

	public static String removeTrailing(String str, String... remove) {
		if (isNotEmpty(str) && isNotEmpty(remove)) {
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
		if (isNotEmpty(str) && isNotEmpty(remove)) {
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

	public static int getFirstOccuranceOf(final Pattern regex, final String inText) {
		final Matcher matcher = regex.matcher(inText);

		if (matcher.find()) {
			return matcher.start();
		}

		return -1;
	}

}
