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

package esa.s1pdgs.cpoc.preparation.worker.model.tasktable;

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
