package esa.s1pdgs.cpoc.directorycleaner;

public class Application {

	public static void main(String[] args) {
		// at this point we only use this for myocean
		final DirectoryCleaner directoryCleaner = DirectoryCleanerFactory.newDirectoryCleaner("myocean");
		directoryCleaner.cleanDirectories();
	}

}
