package esa.s1pdgs.cproc.errorrepo.model.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class FailedProcessingDtoTest {
	
	@Test
	public void orderByCreationTimeAscending() {
		
	   FailedProcessingDto f1 = new FailedProcessingDto<>();
	   FailedProcessingDto f2 = new FailedProcessingDto<>();
	   FailedProcessingDto f3 = new FailedProcessingDto<>();
	   
	   f1.creationDate(new Date(1000));
	   f2.creationDate(new Date(2000));
	   f3.creationDate(new Date(3000));
	   
	   List<FailedProcessingDto> unsortedlist = new ArrayList<>();
	   
	   unsortedlist.add(f3);
	   unsortedlist.add(f1);
	   unsortedlist.add(f2);
	   
	   Collections.sort(unsortedlist, FailedProcessingDto.ASCENDING_CREATION_TIME_COMPERATOR);
	   
	   assertEquals(f1, unsortedlist.get(0));
	   assertEquals(f2, unsortedlist.get(1));
	   assertEquals(f3, unsortedlist.get(2));
	   
		
		
		
	}

}
