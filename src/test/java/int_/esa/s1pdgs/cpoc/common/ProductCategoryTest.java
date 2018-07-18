package int_.esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import int_.esa.s1pdgs.cpoc.common.ProductCategory;
import int_.esa.s1pdgs.cpoc.common.ProductFamily;
import int_.esa.s1pdgs.cpoc.common.errors.InternalErrorException;

/**
 * Test the enumeration ProductCategory
 * 
 * @author Viveris Technologies
 */
public class ProductCategoryTest {

    /**
     * Test the functions of an enumeration
     */
    @Test
    public void testNominalEnumFunctions() {
        assertEquals(5, ProductCategory.values().length);
        assertEquals(ProductCategory.AUXILIARY_FILES,
                ProductCategory.valueOf("AUXILIARY_FILES"));
        assertEquals(ProductCategory.EDRS_SESSIONS,
                ProductCategory.valueOf("EDRS_SESSIONS"));
        assertEquals(ProductCategory.LEVEL_PRODUCTS,
                ProductCategory.valueOf("LEVEL_PRODUCTS"));
        assertEquals(ProductCategory.LEVEL_REPORTS,
                ProductCategory.valueOf("LEVEL_REPORTS"));
        assertEquals(ProductCategory.LEVEL_JOBS,
                ProductCategory.valueOf("LEVEL_JOBS"));
    }

    /**
     * Test the function to get category from family
     * 
     * @throws InternalErrorException
     */
    @Test
    public void testValueFromFamily() throws InternalErrorException {
        assertEquals(ProductCategory.AUXILIARY_FILES, ProductCategory
                .fromProductFamily(ProductFamily.AUXILIARY_FILE));
        assertEquals(ProductCategory.EDRS_SESSIONS, ProductCategory
                .fromProductFamily(ProductFamily.EDRS_SESSION));
        assertEquals(ProductCategory.LEVEL_REPORTS,
                ProductCategory.fromProductFamily(ProductFamily.L0_REPORT));
        assertEquals(ProductCategory.LEVEL_REPORTS,
                ProductCategory.fromProductFamily(ProductFamily.L1_REPORT));
        assertEquals(ProductCategory.LEVEL_PRODUCTS,
                ProductCategory.fromProductFamily(ProductFamily.L0_ACN));
        assertEquals(ProductCategory.LEVEL_PRODUCTS,
                ProductCategory.fromProductFamily(ProductFamily.L0_PRODUCT));
        assertEquals(ProductCategory.LEVEL_PRODUCTS,
                ProductCategory.fromProductFamily(ProductFamily.L1_ACN));
        assertEquals(ProductCategory.LEVEL_PRODUCTS,
                ProductCategory.fromProductFamily(ProductFamily.L1_PRODUCT));
        assertEquals(ProductCategory.LEVEL_JOBS,
                ProductCategory.fromProductFamily(ProductFamily.L0_JOB));
        assertEquals(ProductCategory.LEVEL_JOBS,
                ProductCategory.fromProductFamily(ProductFamily.L1_JOB));

        try {
            ProductCategory.fromProductFamily(null);
            fail("an InternalErrorException shall be raised");
        } catch (InternalErrorException iee) {
            assertTrue(iee.getMessage().contains("null"));
        }

        try {
            ProductCategory.fromProductFamily(ProductFamily.BLANK);
            fail("an InternalErrorException shall be raised");
        } catch (InternalErrorException iee) {
            assertTrue(iee.getMessage().contains("BLANK"));
        }

        try {
            ProductCategory.fromProductFamily(ProductFamily.JOB_ORDER);
            fail("an InternalErrorException shall be raised");
        } catch (InternalErrorException iee) {
            assertTrue(iee.getMessage().contains("JOB"));
        }
    }
}
