package esa.s1pdgs.cpoc.preparation.worker.model.generator;

import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;

@FunctionalInterface
public interface ThrowingRunnable
{
	void run() throws IpfPrepWorkerInputsMissingException;
}