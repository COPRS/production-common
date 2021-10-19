package esa.s1pdgs.cpoc.reqrepo.consumption;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.message.Acknowledgement;
import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.message.Message;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

public class ErrorQueueConsumer implements MessageConsumer<FailedProcessingDto> {

    private static final Logger LOG =
            LoggerFactory.getLogger(ErrorQueueConsumer.class);

    private final RequestRepository requestRepository;
    private final String errorTopic;

    public ErrorQueueConsumer(final RequestRepository requestRepository, final String errorTopic) {
        this.requestRepository = requestRepository;
        this.errorTopic = errorTopic;
    }

    @Override
    public void onMessage(Message<FailedProcessingDto> message, Acknowledgement acknowledgement, Consumption consumption) {
        try {
            requestRepository.saveFailedProcessing(message.data());
            acknowledgement.acknowledge();
        } catch (final Exception e) {
            LOG.error("[code {}] Exception occurred during acknowledgment {}", ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e));
        }
    }

    @Override
    public Class<FailedProcessingDto> messageType() {
        return FailedProcessingDto.class;
    }

    @Override
    public String topic() {
        return errorTopic;
    }
}
