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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils for collections
 */
public class CollectionUtil {

	public static boolean isEmpty(Collection<?> coll) {
		return (coll == null) || coll.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> coll) {
		return !isEmpty(coll);
	}

	public static <T> Collection<T> nullToEmpty(Collection<T> coll) {
		return (coll != null) ? coll : new ArrayList<>(0);
	}

	public static <T> List<T> nullToEmptyList(Collection<T> coll) {
		return (coll != null) ? new ArrayList<>(coll) : new ArrayList<>(0);
	}

	public static int size(Collection<?> coll) {
		return (coll != null) ? coll.size() : 0;
	}

	@SafeVarargs
	public static <T> List<T> toList(T... array) {
		if (ArrayUtil.isEmpty(array)) {
			return new ArrayList<>(0);
		}

		final List<T> list = new ArrayList<>(array.length);
		Collections.addAll(list, array);
		return list;
	}

	@SafeVarargs
	public static <T> Set<T> toSet(T... array) {
		if (ArrayUtil.isEmpty(array)) {
			return new LinkedHashSet<>(0);
		}

		final Set<T> set = new LinkedHashSet<>((int) (array.length / 0.75) + 1);
		Collections.addAll(set, array);
		return set;
	}

	// --------------------------------------------------------------------------

	private CollectionUtil() {
	}

}
