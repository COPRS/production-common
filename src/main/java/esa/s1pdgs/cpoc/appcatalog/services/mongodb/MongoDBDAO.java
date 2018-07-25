/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.services.mongodb;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.model.MqiMessage;


/**
 * @author Viveris Technologies
 *
 */
@Service
public class MongoDBDAO {

    @Autowired
    private MongoTemplate mongoClient;
    
    
    public List<MqiMessage> find(Query query){
        return mongoClient.find(query, MqiMessage.class);
    }
    
    public void insert(MqiMessage messageToInsert) {
        mongoClient.insert(messageToInsert);
    }
    
    public void updateFirst(Query query, Update update) {
        mongoClient.updateFirst(query, update, MqiMessage.class);
    }
    
    public void findAllAndRemove(Query query) {
        mongoClient.findAllAndRemove(query, MqiMessage.class);
    }
}
