package fr.viveris.s1pdgs.jobgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import fr.viveris.s1pdgs.jobgenerator.model.ProductFamily;

/**
 * Extraction class of "tasktables" configuration properties
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "job-generator")
public class JobGeneratorSettings {

	/**
	 * Maximal number of task tables
	 */
	private int maxnumberoftasktables;

	/**
	 * Maximal number of jobs waiting to be sent
	 */
	private int maxnumberofjobs;

	/**
	 * Delay configuration between 2 check of raw metadata presence for a session
	 */
	private WaitTempo waitprimarycheck;

	/**
	 * Delay configuration between 2 check of inputs metadata search for a job
	 */
	private WaitTempo waitmetadatainput;
	
	/**
	 * Location of task table XML files
	 */
	private String directoryoftasktables;
	
	/**
	 * 
	 */
	private int scheduledfixedrate;
	
	private String defaultoutputfamily;
	
	private String outputfamiliesstr;
	
	private Map<String, ProductFamily> outputfamilies;
	
	private String typeoverlapstr;
	/**
	 * Map of all the overlap for the different slice type
	 */
	private Map<String, Float> typeOverlap;
	
	private String typeslicelengthstr;
	/**
	 * Map of all the length for the different slice type
	 */
	private Map<String, Float> typeSliceLength;
	
	private String linkProducttypeMetadataindexStr;
	/**
	 * Map of all the length for the different slice type
	 */
	private Map<String, String> linkProducttypeMetadataindex;

	/**
	 * Default constructors
	 */
	public JobGeneratorSettings() {
		this.outputfamilies = new HashMap<>();
		this.typeOverlap = new HashMap<>();
		this.typeSliceLength = new HashMap<>(); 
		this.linkProducttypeMetadataindex = new HashMap<>(); 
	}
	
	@PostConstruct
	public void initMaps() {
		// Params
		if (!StringUtils.isEmpty(this.outputfamiliesstr)) {
			String[] paramsTmp = this.outputfamiliesstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.outputfamilies.put(tmp[0], ProductFamily.fromValue(tmp[1]));
					}
				}
			}
		}
		// TypeOverlap
		if (!StringUtils.isEmpty(this.typeoverlapstr)) {
			String[] paramsTmp = this.typeoverlapstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.typeOverlap.put(tmp[0], Float.valueOf(tmp[1]));
					}
				}
			}
		}
		// TypeSliceLength
		if (!StringUtils.isEmpty(this.typeslicelengthstr)) {
			String[] paramsTmp = this.typeslicelengthstr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.typeSliceLength.put(tmp[0], Float.valueOf(tmp[1]));
					}
				}
			}
		}
		// linkProductypeMetadataindex
		if (!StringUtils.isEmpty(this.linkProducttypeMetadataindexStr)) {
			String[] paramsTmp = this.linkProducttypeMetadataindexStr.split("\\|\\|");
			for (int i=0; i<paramsTmp.length; i++) {
				if (!StringUtils.isEmpty(paramsTmp[i])) {
					String[] tmp = paramsTmp[i].split(":", 2);
					if (tmp.length == 2) {
						this.linkProducttypeMetadataindex.put(tmp[0], tmp[1]);
					}
				}
			}
		}
	}


	/**
	 * Class of delay configuration
	 * @author Cyrielle Gailliard
	 *
	 */
	public static class WaitTempo {
		/**
		 * Delay between 2 retries
		 */
		private int tempo;
		
		/**
		 * Number of maximal retries
		 */
		private int retries;
		
		public WaitTempo() {
		}
		
		public WaitTempo(int tempo, int retries) {
			this.tempo = tempo;
			this.retries = retries;
		}

		/**
		 * @return the tempo
		 */
		public int getTempo() {
			return tempo;
		}

		/**
		 * @param tempo
		 *            the tempo to set
		 */
		public void setTempo(int tempo) {
			this.tempo = tempo;
		}

		/**
		 * @return the retries
		 */
		public int getRetries() {
			return retries;
		}

		/**
		 * @param retries
		 *            the retries to set
		 */
		public void setRetries(int retries) {
			this.retries = retries;
		}

	}

	/**
	 * @return the maxnumberoftasktables
	 */
	public int getMaxnumberoftasktables() {
		return maxnumberoftasktables;
	}

	/**
	 * @param maxnumberoftasktables
	 *            the maxnumberoftasktables to set
	 */
	public void setMaxnumberoftasktables(int maxnumberoftasktables) {
		this.maxnumberoftasktables = maxnumberoftasktables;
	}

	/**
	 * @return the waitprimarycheck
	 */
	public WaitTempo getWaitprimarycheck() {
		return waitprimarycheck;
	}

	/**
	 * @param waitprimarycheck the waitprimarycheck to set
	 */
	public void setWaitprimarycheck(WaitTempo waitprimarycheck) {
		this.waitprimarycheck = waitprimarycheck;
	}

	/**
	 * @return the waitmetadatainput
	 */
	public WaitTempo getWaitmetadatainput() {
		return waitmetadatainput;
	}

	/**
	 * @param waitmetadatainput
	 *            the waitmetadatainput to set
	 */
	public void setWaitmetadatainput(WaitTempo waitmetadatainput) {
		this.waitmetadatainput = waitmetadatainput;
	}

	/**
	 * @return the directoryoftasktables
	 */
	public String getDirectoryoftasktables() {
		return directoryoftasktables;
	}

	/**
	 * @param directoryoftasktables the directoryoftasktables to set
	 */
	public void setDirectoryoftasktables(String directoryoftasktables) {
		this.directoryoftasktables = directoryoftasktables;
	}

	/**
	 * @return the maxnumberofjobs
	 */
	public int getMaxnumberofjobs() {
		return maxnumberofjobs;
	}

	/**
	 * @param maxnumberofjobs
	 *            the maxnumberofjobs to set
	 */
	public void setMaxnumberofjobs(int maxnumberofjobs) {
		this.maxnumberofjobs = maxnumberofjobs;
	}

	/**
	 * @return the scheduledfixedrate
	 */
	public int getScheduledfixedrate() {
		return scheduledfixedrate;
	}

	/**
	 * @param scheduledfixedrate the scheduledfixedrate to set
	 */
	public void setScheduledfixedrate(int scheduledfixedrate) {
		this.scheduledfixedrate = scheduledfixedrate;
	}

	/**
	 * @return the defaultoutputfamily
	 */
	public String getDefaultoutputfamily() {
		return defaultoutputfamily;
	}

	/**
	 * @param defaultoutputfamily the defaultoutputfamily to set
	 */
	public void setDefaultoutputfamily(String defaultoutputfamily) {
		this.defaultoutputfamily = defaultoutputfamily;
	}

	/**
	 * @return the outputfamiliesstr
	 */
	public String getOutputfamiliesstr() {
		return outputfamiliesstr;
	}

	/**
	 * @param outputfamiliesstr the outputfamiliesstr to set
	 */
	public void setOutputfamiliesstr(String outputfamiliesstr) {
		this.outputfamiliesstr = outputfamiliesstr;
	}

	/**
	 * @return the outputfamilies
	 */
	public Map<String, ProductFamily> getOutputfamilies() {
		return outputfamilies;
	}

	/**
	 * @param outputfamilies the outputfamilies to set
	 */
	public void setOutputfamilies(Map<String, ProductFamily> outputfamilies) {
		this.outputfamilies = outputfamilies;
	}

	/**
	 * @return the typeoverlapstr
	 */
	public String getTypeoverlapstr() {
		return typeoverlapstr;
	}

	/**
	 * @param typeoverlapstr the typeoverlapstr to set
	 */
	public void setTypeoverlapstr(String typeoverlapstr) {
		this.typeoverlapstr = typeoverlapstr;
	}

	/**
	 * @return the typeOverlap
	 */
	public Map<String, Float> getTypeOverlap() {
		return typeOverlap;
	}

	/**
	 * @param typeOverlap the typeOverlap to set
	 */
	public void setTypeOverlap(Map<String, Float> typeOverlap) {
		this.typeOverlap = typeOverlap;
	}

	/**
	 * @return the typeslicelengthstr
	 */
	public String getTypeslicelengthstr() {
		return typeslicelengthstr;
	}

	/**
	 * @param typeslicelengthstr the typeslicelengthstr to set
	 */
	public void setTypeslicelengthstr(String typeslicelengthstr) {
		this.typeslicelengthstr = typeslicelengthstr;
	}

	/**
	 * @return the typeSliceLength
	 */
	public Map<String, Float> getTypeSliceLength() {
		return typeSliceLength;
	}

	/**
	 * @param typeSliceLength the typeSliceLength to set
	 */
	public void setTypeSliceLength(Map<String, Float> typeSliceLength) {
		this.typeSliceLength = typeSliceLength;
	}

	/**
	 * @return the linkProductypeMetadataindexStr
	 */
	public String getLinkProducttypeMetadataindexStr() {
		return linkProducttypeMetadataindexStr;
	}

	/**
	 * @param linkProductypeMetadataindexStr the linkProductypeMetadataindexStr to set
	 */
	public void setLinkProducttypeMetadataindexStr(String linkProducttypeMetadataindexStr) {
		this.linkProducttypeMetadataindexStr = linkProducttypeMetadataindexStr;
	}

	/**
	 * @return the linkProductypeMetadataindex
	 */
	public Map<String, String> getLinkProducttypeMetadataindex() {
		return linkProducttypeMetadataindex;
	}

	/**
	 * @param linkProductypeMetadataindex the linkProductypeMetadataindex to set
	 */
	public void setLinkProductypeMetadataindex(Map<String, String> linkProducttypeMetadataindex) {
		this.linkProducttypeMetadataindex = linkProducttypeMetadataindex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{maxnumberoftasktables: " + maxnumberoftasktables + ", maxnumberofjobs: " + maxnumberofjobs
				+ ", waitprimarycheck: " + waitprimarycheck + ", waitmetadatainput: " + waitmetadatainput
				+ ", directoryoftasktables: " + directoryoftasktables + ", scheduledfixedrate: " + scheduledfixedrate
				+ ", defaultoutputfamily: " + defaultoutputfamily + ", outputfamiliesstr: " + outputfamiliesstr
				+ ", outputfamilies: " + outputfamilies + ", typeoverlapstr: " + typeoverlapstr + ", typeOverlap: "
				+ typeOverlap + ", typeslicelengthstr: " + typeslicelengthstr + ", typeSliceLength: " + typeSliceLength
				+ ", linkProductypeMetadataindexStr: " + linkProducttypeMetadataindexStr
				+ ", linkProductypeMetadataindex: " + linkProducttypeMetadataindex + "}";
	}

}
