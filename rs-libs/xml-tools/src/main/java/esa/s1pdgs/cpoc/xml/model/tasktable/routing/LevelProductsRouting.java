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

package esa.s1pdgs.cpoc.xml.model.tasktable.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Routing<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "level_products_routing")
@XmlAccessorType(XmlAccessType.NONE)
public class LevelProductsRouting {

	/**
	 * List of configured routes
	 */
	@XmlElement(name = "level_products_route")
	private List<LevelProductsRoute> routes;

	/**
	 * Default constructor
	 */
	public LevelProductsRouting() {
		this.routes = new ArrayList<>();
	}

	/**
	 * @return the routes
	 */
	public List<LevelProductsRoute> getRoutes() {
		return routes;
	}

	/**
	 * @param routes
	 *            the routes to set
	 */
	public void setRoutes(final List<LevelProductsRoute> routes) {
		this.routes = routes;
	}

	/**
	 * @param routes
	 *            the routes to set
	 */
	public void addRoute(final LevelProductsRoute route) {
		this.routes.add(route);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{routes: %s}", routes);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(routes);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			LevelProductsRouting other = (LevelProductsRouting) obj;
			ret = Objects.equals(routes, other.routes);
		}
		return ret;
	}

}
