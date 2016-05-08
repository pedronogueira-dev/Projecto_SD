package pt.upa.broker.ws;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

public class BrokerEndpointManager {

	private Map<String, TransporterClient> associatedTransporters = new TreeMap<String,TransporterClient>();
	
	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}

	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implementation */
	private BrokerPort portImpl = new BrokerPort(this);

	/** Obtain Port implementation */
	public BrokerPortType getPort() {
		return portImpl;
	}

	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided UDDI location, WS name, and WS URL */
	public BrokerEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public BrokerEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	/**				*/
	private String extractTransporterNum(String url){
		String parsed = url.replaceAll("[\\D]*","" );
		int transporterNumber;
		parsed=parsed.trim();
		
		transporterNumber=Character.getNumericValue(parsed.charAt(parsed.length()-1));
		return "UpaTransporter" + transporterNumber;
	}
	
	/** Transporter register 
	 * @throws JAXRException */
	public void registerTransporter() throws JAXRException{
		String wsName;
		
		Collection<String> registeredTransportServers = uddiNaming.list("UpaTransporter%");
		System.out.println("Transporter List:\n"+registeredTransportServers);
		System.out.println("\n\nCONNECTING TO TRANSPORTERS:");
		for(String wsURL : registeredTransportServers){
		//	System.out.println("\n\n\n wsURL:"+wsURL+"\n endpointAddress:"+endpointAddress);
			TransporterClient c;
			wsName = extractTransporterNum(wsURL);
			
			try {
				c = new TransporterClient(uddiURL,wsName);
				String upaTransporterName =c.ping("BROKER SERVER");
				System.out.println(">>>"+upaTransporterName);
				associatedTransporters.put(upaTransporterName, c);
			} catch (TransporterClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public Map<String,TransporterClient> getTransporters(){
		return associatedTransporters;
	}
	/* endpoint management */

	public void start() throws Exception {
		try {
			// publish endpoint
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		publishToUDDI();
		registerTransporter();
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop endpoint
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		this.portImpl = null;
		unpublishFromUDDI();
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if (verbose) {
					System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
				}
				uddiNaming = new UDDINaming(uddiURL);
				uddiNaming.rebind(wsName, wsURL);
			}
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}

	void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}
}