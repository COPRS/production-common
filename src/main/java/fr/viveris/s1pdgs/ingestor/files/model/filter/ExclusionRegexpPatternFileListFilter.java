package fr.viveris.s1pdgs.ingestor.files.model.filter;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.integration.file.filters.AbstractRegexPatternFileListFilter;

/**
 * File filter to exclude file and directories respecting given pattern
 */
public class ExclusionRegexpPatternFileListFilter extends AbstractRegexPatternFileListFilter<File> {

	/**
	 * 
	 */
	public ExclusionRegexpPatternFileListFilter(final Pattern pattern) {
		super(pattern);
	}

	/**
	 * 
	 */
	@Override
	public boolean accept(final File file) {
		boolean ret;
		if (file == null) {
			ret = false;
		} else {
			ret = alwaysAccept(file) || (!super.accept(file));
		}
		return ret;
	}


	/**
	 * 
	 */
	@Override
	protected String getFilename(final File file) {
		return (file != null) ? file.getName() : null;
	}

	/**
	 * 
	 */
	@Override
	protected boolean isDirectory(final File file) {
		return file.isDirectory();
	}

}
