package pt.upa.broker;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.BrokerPortType;

import pt.upa.broker.ws.cli.BrokerClient;


import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class BrokerClientApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(BrokerClientApplication.class.getSimpleName() + " starting...");


// Check arguments
//		if (args.length < 2) {
//			System.err.println("Argument(s) missing!");
//			System.err.printf("Usage: java %s uddiURL name%n", BrokerClient.class.getName());
//			return;
//		}
//
//		String uddiURL = args[0];
//		String name = args[1];
//
//		System.out.printf("Contacting UDDI at %s%n", uddiURL);
//		UDDINaming uddiNaming = new UDDINaming(uddiURL);
//
//		System.out.printf("Looking for '%s'%n", name);
//		String endpointAddress = uddiNaming.lookup(name);
//
//		if (endpointAddress == null) {
//			System.out.println("Not found!");
//			return;
//		} else {
//			System.out.printf("Found %s%n", endpointAddress);
//		}
//
//		System.out.println("Creating stub ...");
//		BrokerService service = new BrokerService();
//		BrokerPortType port = service.getBrokerPort();
//
//		System.out.println("Setting endpoint address ...");
//		BindingProvider bindingProvider = (BindingProvider) port;
//		Map<String, Object> requestContext = bindingProvider.getRequestContext();
//		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
//
//		try {
//			//BrokerClient client = new BrokerClient(port);
////			BrokerClient client = new BrokerClient(endpointAddress);
////			System.out.println(client.ping("Client1"));
//			//System.out.println("Request:");
//			//String id = client.requestTransport("Lisboa", "Santarem", 12);
//			//System.out.println(id);
//			
//			//client.requestTransport("Santarem", "Lisboa", 10);
//		} catch (Exception pfe) {
//			System.out.println("Caught: " + pfe);
//		}
		
		if (args.length == 0) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + BrokerClientApplication.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		if (args.length == 1) {
			wsURL = args[0];
		} else if (args.length >= 2) {
			uddiURL = args[0];
			wsName = args[1];
		}

		// Create client
		BrokerClient client = null;

		if (wsURL != null) {
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new BrokerClient(wsURL);
		} else if (uddiURL != null) {
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new BrokerClient(uddiURL, wsName);
		}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		String result = client.ping("BROKER CLIENT 1");
		System.out.println(result);

	}

}
