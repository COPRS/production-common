package esa.s1pdgs.cpoc.errorrepo.seq;

public interface SequenceDao {
	
	public static final SequenceDao NULL = new SequenceDao() {		
		@Override
		public long getNextSequenceId(String key) throws SequenceException {
			return 0;
		}
	};

	long getNextSequenceId(String key) throws SequenceException;

}
