package pt.upa.broker.ws.it;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.upa.broker.ws.TransportView;

public class ListTransportsIT extends AbstractIT {

	// tests
	// assertEquals(expected, actual);

	// public List<TransportView> listTransports();
	
	@Test
	public void testListTransports() throws Exception {
		CLIENT.clearTransports();// To start fresh
		String j1 = CLIENT.requestTransport(SOUTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j2 = CLIENT.requestTransport(NORTH_1, CENTER_1, PRICE_SMALLEST_LIMIT);
		String j3 = CLIENT.requestTransport(CENTER_1, CENTER_2, PRICE_SMALLEST_LIMIT);
		List<TransportView> jtvs = new ArrayList<>();
		jtvs.add(CLIENT.viewTransport(j1));
		jtvs.add(CLIENT.viewTransport(j2));
		CLIENT.viewTransport(j3);

		List<TransportView> tList = CLIENT.listTransports();
		assertEquals(3, tList.size());
		int counter = 0;
		
		for(TransportView tv : tList)
			for(TransportView jtv : jtvs)
				if((jtv.getId().equals(tv.getId())) && jtv.getOrigin().equals(tv.getOrigin())
						&& jtv.getDestination().equals(tv.getDestination()) && jtv.getPrice() == tv.getPrice()
						&& jtv.getState().toString().equals(tv.getState().toString()))
					counter++;
		assertEquals(2, counter);
	}

}
