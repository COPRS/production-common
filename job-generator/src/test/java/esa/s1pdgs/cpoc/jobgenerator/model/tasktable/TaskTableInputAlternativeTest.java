package esa.s1pdgs.cpoc.jobgenerator.model.tasktable;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TaskTableInputAlternativeTest {

    /**
     * Check equals and hascode methods
     */
    @Test
    public void equalsDtoTaskTableInputAlternative() {
        EqualsVerifier.forClass(TaskTableInputAlternative.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

}
