package esa.s1pdgs.cpoc.appcatalog.server.mqi;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageRepository;

/**
 * Component that will clean the mongoDB entries that are older
 * Configurable 
 *
 * @author Viveris Technologies
 */
@Component
public class DatabaseCleaningTask {

    private MqiMessageRepository mqiMessageRepository;
    
    /**
     * Second after an entry is considered old
     */
    private int oldms;
    
    /**
     * Constructor for the Services
     * 
     * @param mongoDBDAO
     */
    @Autowired
    public DatabaseCleaningTask(final MqiMessageRepository mqiMessageRepository,
            @Value("${mongodb.old-entry-ms}") int oldms) {
        this.mqiMessageRepository = mqiMessageRepository;
        this.oldms = oldms;
    }
    
    /**
     * Function that will clean all the old message (configurable to be launch using cron notaion)
     */
    @Scheduled(cron = "${mongodb.clean-cron}")
    public void clean() {
        Date oldDate = new Date(System.currentTimeMillis() - oldms);
        mqiMessageRepository.truncateBefore(oldDate);
    }
    
    
}
