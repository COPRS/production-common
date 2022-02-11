package esa.s1pdgs.cpoc.message;

public interface ConsumptionController {

    void pause(String topic);

    boolean isPaused(String topic);

    void resume(String topic);


}
