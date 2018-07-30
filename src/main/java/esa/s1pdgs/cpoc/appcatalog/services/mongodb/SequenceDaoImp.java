package esa.s1pdgs.cpoc.appcatalog.services.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.model.SequenceId;

/**
 * 
 * @author Viveris Technologies
 *
 */
@Service
public class SequenceDaoImp implements SequenceDao {

    /**
     * Mongo client
     */
    private final MongoTemplate mongoClient;
    
    /**
     * Constructor
     * @param mongoClient
     */
    @Autowired
    public SequenceDaoImp(final MongoTemplate mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Get the next sequence id for the given key
     */
    @Override
	public long getNextSequenceId(final String key) throws SequenceException {
		
	  //get sequence id
	  Query query = new Query(Criteria.where("_id").is(key));

	  //increase sequence id by 1
	  Update update = new Update();
	  update.inc("seq", 1);

	  //return new increased id
	  FindAndModifyOptions options = new FindAndModifyOptions();
	  options.returnNew(true);

	  //this is the magic happened.
	  SequenceId seqId = 
			  mongoClient.findAndModify(query, update, options, SequenceId.class);

	  //if no id, throws SequenceException
          //optional, just a way to tell user when the sequence id is failed to generate.
	  if (seqId == null) {
		throw new SequenceException("Unable to get sequence id for key : " + key);
	  }

	  return seqId.getSeq();

	}
}
