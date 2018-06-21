package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;

/**
 * @author Viveris Technologies
 */
@Service
public class L0ReportProducer extends AbstractGenericProducer<ReportDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param topic
     */
    @Autowired
    public L0ReportProducer(
            @Qualifier("kafkaL0ReportTemplate") final KafkaTemplate<String, ReportDto> kafkaTemplate,
            @Value("${kafka.topic.l0-reports}") final String topic) {
        super(kafkaTemplate, topic);
    }

    /**
     * @see AbstractGenericProducer#extractProductName(Object)
     */
    @Override
    protected String extractProductName(final ReportDto obj) {
        return obj.getProductName();
    }

}
