package esa.s1pdgs.cpoc.appcatalog.server.sequence.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;

import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDaoImp;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceId;

public class SequenceDaoImplTest {

    @Mock
    private MongoTemplate mongoClient;

    private SequenceDaoImp dao;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        dao = new SequenceDaoImp(mongoClient);
    }

    @Test(expected = SequenceException.class)
    public void testGetSequenceWhenExcpetion() {
        doReturn(null).when(mongoClient).findAndModify(Mockito.any(),
                Mockito.any(), Mockito.any(FindAndModifyOptions.class), Mockito.any());
        dao.getNextSequenceId("key");
    }

    @Test
    public void testGetSequence() {
        SequenceId seq = new SequenceId();
        seq.setId("key");
        seq.setSeq(125L);
        doReturn(seq).when(mongoClient).findAndModify(Mockito.any(),
                Mockito.any(), Mockito.any(FindAndModifyOptions.class), Mockito.any());
        assertEquals(125L, dao.getNextSequenceId("key"));
    }
}
