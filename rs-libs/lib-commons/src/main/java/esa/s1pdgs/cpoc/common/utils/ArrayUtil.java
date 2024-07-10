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
