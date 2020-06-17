package esa.s1pdgs.cpoc.prip.frontend.utils;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataLibraryException;

public class OlingoUtil {

	   
	   private OlingoUtil() {
	   }
	   
	   public static ContextURL getContextUrl(final EdmEntitySet entitySet, final EdmEntityType entityType, final boolean isSingleEntity)
	         throws ODataLibraryException {

	      ContextURL.Builder builder = ContextURL.with();
	      builder = entitySet == null ? isSingleEntity ? builder.type(entityType) : builder.asCollection().type(entityType)
	            : builder.entitySet(entitySet);

	      builder = builder.suffix(isSingleEntity && entitySet != null ? Suffix.ENTITY : null);
	      return builder.build();
	   }
	   
}
