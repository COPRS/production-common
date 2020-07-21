package esa.s1pdgs.cpoc.ipf.preparation.worker.generator.state;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;

public interface JobGenerationTransition {
	public void mainInputSearch( AppDataJobGenerationState outputState) throws JobStateTransistionFailed;	
	public void auxSearch( AppDataJobGenerationState outputState) throws JobStateTransistionFailed;	
	public void send( AppDataJobGenerationState outputState) throws JobStateTransistionFailed;
}
