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

public class OlingoUtil {

	private OlingoUtil() {
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
