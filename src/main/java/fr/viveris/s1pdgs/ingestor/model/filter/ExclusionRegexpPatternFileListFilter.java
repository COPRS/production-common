package fr.viveris.s1pdgs.ingestor.model.filter;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.integration.file.filters.AbstractRegexPatternFileListFilter;

public class ExclusionRegexpPatternFileListFilter extends AbstractRegexPatternFileListFilter<File> {

	

	public ExclusionRegexpPatternFileListFilter(String pattern) {
		super(pattern);
	}
	
	public ExclusionRegexpPatternFileListFilter(Pattern pattern) {
		super(pattern);
	}

	@Override
	public boolean accept(File file) {
		return alwaysAccept(file) || (!super.accept(file));
	}


	@Override
	protected String getFilename(File file) {
		return (file != null) ? file.getName() : null;
	}

	@Override
	protected boolean isDirectory(File file) {
		return file.isDirectory();
	}

}
