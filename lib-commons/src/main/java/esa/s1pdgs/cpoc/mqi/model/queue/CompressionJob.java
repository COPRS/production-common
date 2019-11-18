package esa.s1pdgs.cpoc.mqi.model.queue;

public class CompressionJob {
	
	private String inputFileName;
	private String inputObsKey;	
	private String outputFileName;
	private String outputObsKey;
	private CompressionDirection compressionDirection;
	
	public String getInputFileName() {
		return inputFileName;
	}
	
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	
	public String getInputObsKey() {
		return inputObsKey;
	}
	
	public void setInputObsKey(String inputObsKey) {
		this.inputObsKey = inputObsKey;
	}
	
	public String getOutputFileName() {
		return outputFileName;
	}
	
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	public String getOutputObsKey() {
		return outputObsKey;
	}
	
	public void setOutputObsKey(String outputObsKey) {
		this.outputObsKey = outputObsKey;
	}
	
	public CompressionDirection getCompressionDirection() {
		return compressionDirection;
	}
	
	public void setCompressionDirection(CompressionDirection compressionDirection) {
		this.compressionDirection = compressionDirection;
	}

	@Override
	public String toString() {
		return "CompressionJob [inputFileName=" + inputFileName + ", inputObsKey=" + inputObsKey + ", outputFileName="
				+ outputFileName + ", outputObsKey=" + outputObsKey + ", compressionDirection=" + compressionDirection
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compressionDirection == null) ? 0 : compressionDirection.hashCode());
		result = prime * result + ((inputFileName == null) ? 0 : inputFileName.hashCode());
		result = prime * result + ((inputObsKey == null) ? 0 : inputObsKey.hashCode());
		result = prime * result + ((outputFileName == null) ? 0 : outputFileName.hashCode());
		result = prime * result + ((outputObsKey == null) ? 0 : outputObsKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompressionJob other = (CompressionJob) obj;
		if (compressionDirection != other.compressionDirection)
			return false;
		if (inputFileName == null) {
			if (other.inputFileName != null)
				return false;
		} else if (!inputFileName.equals(other.inputFileName))
			return false;
		if (inputObsKey == null) {
			if (other.inputObsKey != null)
				return false;
		} else if (!inputObsKey.equals(other.inputObsKey))
			return false;
		if (outputFileName == null) {
			if (other.outputFileName != null)
				return false;
		} else if (!outputFileName.equals(other.outputFileName))
			return false;
		if (outputObsKey == null) {
			if (other.outputObsKey != null)
				return false;
		} else if (!outputObsKey.equals(other.outputObsKey))
			return false;
		return true;
	}

}
