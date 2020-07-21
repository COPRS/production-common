package esa.s1pdgs.cpoc.ipf.preparation.worker.generator;

import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;

@FunctionalInterface
public interface ThrowingRunnable
{
	void run() throws IpfPrepWorkerInputsMissingException;
}