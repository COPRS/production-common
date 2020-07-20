package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

public interface JobGenerationTransition {
	public void mainInputSearch() throws JobStateTransistionFailed;	
	public void auxSearch() throws JobStateTransistionFailed;	
	public void send() throws JobStateTransistionFailed;
}
