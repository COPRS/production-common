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

package esa.s1pdgs.cpoc.prip.frontend.utils;

import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;

public class OlingoUtil {

	private OlingoUtil() {
	}
	
	public static void validate(final UriInfo uriInfo) throws ODataApplicationException {
      if(uriInfo.getCustomQueryOptions() != null && uriInfo.getCustomQueryOptions().size() > 0) {
         
         StringBuffer query = new StringBuffer();
         for(CustomQueryOption co : uriInfo.getCustomQueryOptions()) {
            query.append(co.getName()).append("=").append(co.getText());
         }
         
         throw new ODataApplicationException("Invalid Query Options: "+query.toString(), HttpStatusCode.BAD_REQUEST.getStatusCode(),
               Locale.ROOT);
      }
   }
	
	public static ContextURL getContextUrl(final EdmEntitySet entitySet, final EdmEntityType entityType,
			final boolean isSingleEntity) throws ODataLibraryException {

		ContextURL.Builder builder = ContextURL.with();

		if (null != entitySet) {
		   builder.entitySet(entitySet);
		   if (isSingleEntity) {
		      builder.suffix(Suffix.ENTITY);
		   }
		} else {
		   builder.type(entityType);
		   if (!isSingleEntity) {
		      builder.asCollection();
		   }
		}

		return builder.build();
	}
	
	public static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEdmEntitySet,
			EdmNavigationProperty edmNavigationProperty) throws ODataApplicationException {
		final String navPropName = edmNavigationProperty.getName();
		final EdmBindingTarget edmBindingTarget = startEdmEntitySet.getRelatedBindingTarget(navPropName);

		if (edmBindingTarget instanceof EdmEntitySet) {
		   return (EdmEntitySet) edmBindingTarget;
		}
		throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
		      Locale.ROOT);
	}

}
