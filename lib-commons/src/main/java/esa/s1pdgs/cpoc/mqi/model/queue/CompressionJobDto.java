package esa.s1pdgs.cpoc.mqi.model.queue;

/**
 * A DTO containing a request for a compression job. This class does
 * basically just contain the input of the products that should be processed.
 * 
 * @author florian_sievert
 *
 */
public class CompressionJobDto {
    /**
     * List of inputs needed to execute the job.<br/>
     * They contain the absolute name on the target host and where we can find
     * the file according the input family
     */
	// TODO: Maybe LevelJobInputDto is not a good name or needs to have its own class
    private LevelJobInputDto inputs;

	public LevelJobInputDto getInputs() {
		return inputs;
	}

	public void setInputs(LevelJobInputDto inputs) {
		this.inputs = inputs;
	}

    
}
