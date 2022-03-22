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
	
	// --------------------------------------------------------------------------

	public static ContextURL getContextUrl(final EdmEntitySet entitySet, final EdmEntityType entityType,
			final boolean isSingleEntity) throws ODataLibraryException {

		ContextURL.Builder builder = ContextURL.with();
		builder = entitySet == null
				? isSingleEntity ? builder.type(entityType) : builder.asCollection().type(entityType)
				: builder.entitySet(entitySet);

		builder = builder.suffix(isSingleEntity && entitySet != null ? Suffix.ENTITY : null);
		return builder.build();
	}
	
	public static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEdmEntitySet,
			EdmNavigationProperty edmNavigationProperty) throws ODataApplicationException {
		final String navPropName = edmNavigationProperty.getName();
		final EdmBindingTarget edmBindingTarget = startEdmEntitySet.getRelatedBindingTarget(navPropName);

		if (null == edmBindingTarget) {
			throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
					Locale.ROOT);
		}

		if (edmBindingTarget instanceof EdmEntitySet) {
			return (EdmEntitySet) edmBindingTarget;
		}

		throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
				Locale.ROOT);
	}

}
