package pt.upa.transporter.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


public class TransporterEndpointManager {
	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}
	
	/** Transporter Number*/
	private int transporterNumber;
	public int getTransporterNumber(){
		return transporterNumber;
	}
	
	/**ExtractTransporterNum*/
	private void extractTransporterNum(String url){
		String parsed = url.replaceAll("[\\D]*","" );
		parsed=parsed.trim();
		
		this.transporterNumber=Character.getNumericValue(parsed.charAt(parsed.length()-1));
		
		//System.out.println("Parsed URL:: "+parsed + "Length:"+parsed.length()+"\nTRANSPORTERNUMBER FOUND: "+transporterNumber);
		portImpl=new TransporterPort(this);
	}
	
	/** Web Service location to publish */
	private String wsURL = null;

	/** Port implementation */
	private TransporterPort portImpl; //= new TransporterPort(this);

	/** Obtain Port implementation */
	public TransporterPortType getPort() {
		return portImpl;
	}
	/**
	 * UDDI
	 */
	
	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
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
	
	
	/**
	 *	ENDPOINT MANAGER: 
	 */
	
	/** constructor with provided UDDI location, WS name, and WS URL */
	public TransporterEndpointManager(String uddiURL, String wsName, String wsURL) {
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
		
		extractTransporterNum(wsURL);
	}

	/** constructor with provided web service URL */
	public TransporterEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
		extractTransporterNum(wsURL);
		
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
		this.portImpl.stop();
		this.portImpl = null;
		unpublishFromUDDI();
	}
	
}
