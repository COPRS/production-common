package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;

public interface JobGenerator extends Runnable {

	File getTasktable();
}
