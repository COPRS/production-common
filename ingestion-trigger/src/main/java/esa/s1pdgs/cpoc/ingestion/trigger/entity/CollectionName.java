package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Determines the mongodb collection name used for {@link InboxEntry} by this ingestion-trigger app.
 * see <a href="https://jira.spring.io/browse/DATAMONGO-525?focusedCommentId=89157&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-89157">https://jira.spring.io/browse/DATAMONGO-525</a>
 */
@Component
public class CollectionName {

    @Value("inboxEntry-${process.hostname}")
    private String name;

    public String getName() {
        return name;
    }

}
