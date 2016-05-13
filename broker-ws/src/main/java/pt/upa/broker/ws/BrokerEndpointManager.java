package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.cli.TransporterClientException;

public class BrokerEndpointManager {

	private Map<String, TransporterClient> associatedTransporters = new TreeMap<String,TransporterClient>();
	
	private boolean MainServerIsAlive=false;
	
	public boolean isMainServerIsAlive() {
		return MainServerIsAlive;
	}
	
	
	private String mainUddiUrl=null;

	public String getMainUddiUrl(){
		return mainUddiUrl;
	}

	/**
	 * @param mainServerIsAlive the mainServerIsAlive to set
	 */
	public void setMainServerIsAlive(boolean mainServerIsAlive) {
		MainServerIsAlive = mainServerIsAlive;
	}
	
	/** Broker port type*/
	private boolean brokerIsMain=false;
	
	public void setBrokerType(boolean isMain){
		brokerIsMain=isMain;
	}
	
	/**
	 * @return the uddiURL
	 */
	public String getUddiURL() {
		return uddiURL;
	}

	/**
	 * @param uddiURL the uddiURL to set
	 */
	public void setUddiURL(String uddiURL) {
		this.uddiURL = uddiURL;
	}

	/**
	 * @return the wsURL
	 */
	public String getWsURL() {
		return wsURL;
	}

	/**
	 * @param wsURL the wsURL to set
	 */
	public void setWsURL(String wsURL) {
		this.wsURL = wsURL;
	}

	/**
	 * @param wsName the wsName to set
	 */
	public void setWsName(String wsName) {
		this.wsName = wsName;
	}

	public boolean brokerIsMain(){
		return brokerIsMain;
	}
	
	/** Broker secundary server*/
	private communicationPort secundaryBroker = null;
	//private BrokerClient	secundaryBroker = null;
	
	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;

	/**
	 * @return the secundaryBroker
	 */
	public communicationPort getSecundaryBroker() {
		return secundaryBroker;
	}

	/**
	 * @param secundaryBroker the secundaryBroker to set
	 */

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
		
		this.uddiURL=uddiURL;
		this.wsURL=wsURL;
		if(!wsURL.contains(":8080")){
			System.out.println("\nSecundary Server");
			this.wsName="UpaBrokerBackup";
			//portImpl=new BrokerPort(this);
			portImpl.setIsBackup(true);
		}
		else{
			this.wsName=wsName;
		}
		
		System.out.println("\n\nIS BACKUP: "+portImpl.getIsBackup());
	}
	
	
	private void registerSecundaryServer() throws JAXRException, Exception {
		// TODO Auto-generated method stub
		
		if(portImpl.getIsBackup())
			return;
		Collection<String> serverUddiURL= uddiNaming.list("UpaBroker");
		Collection<String> registeredBrokers = uddiNaming.list("UpaBrokerBackup");
		System.out.println("Broker List:\n"+registeredBrokers);
		System.out.println("\n\nCONNECTING TO SecundaryBroker:");
		for(String s : registeredBrokers){
			if(!s.equals(wsURL)){
				secundaryBroker= new communicationPort(s);
				//secundaryBroker.setMainUddiUrl(this.getUddiURL());
				continue;
			}
		}
		if(secundaryBroker!=null){
			secundaryBroker.port.ping("::Main Broker Server::");
			
			portImpl.amAlive();
			secundaryBroker.port.isAlive();
			
			return;}
		throw new Exception("");
	}

//	/** constructor with provided UDDI location, WS name, and WS URL */
//	public BrokerEndpointManager(String uddiURL, String wsName, String wsURL) {
//		this.uddiURL = uddiURL;
//		this.wsName = wsName;
//		this.wsURL = wsURL;
//	}

	/** constructor with provided web service URL */
	public BrokerEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	/**	*/
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
		if(portImpl.getIsBackup())
			return;
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
		secundaryBroker=null;
		try{
			registerSecundaryServer();
		}catch(Exception e){
			System.out.println("NO SECUNDARY/BACKUP BROKERS REGISTERED ON UDDI!");
		}
		
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
		portImpl.stop();
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
		//portImpl.brokerConsistencyManagement();

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
	
	
	
	/////////////////////////////////////////////////////////
	////	Inner Class to establish communications with
	////				Broker Servers
	/////////////////////////////////////////////////////////
	public class communicationPort{

		//private String mainUddiUrl=null;
		public void setMainUddiUrl(String s){
			mainUddiUrl=s;
		}
		public String getMainUddiUrl(){
			return mainUddiUrl;
		}
		/** WS service */
		BrokerService service = null;

		/** WS port (port type is the interface, port is the implementation) */
		BrokerPortType port = null;

		/** UDDI server URL */
		private String uddiURL = null;

		/** WS name */
		private String wsName = null;

		/** WS endpoint address */
		private String wsURL = null; // default value is defined inside WSDL

		public String getWsURL() {
			return wsURL;
		}

		/**
		 * @return the uddiURL
		 */
		public String getUddiURL() {
			return uddiURL;
		}

		/**
		 * @param uddiURL the uddiURL to set
		 */
		public void setUddiURL(String uddiURL) {
			this.uddiURL = uddiURL;
		}

		/**
		 * @return the wsName
		 */
		public String getWsName() {
			return wsName;
		}

		/**
		 * @param wsName the wsName to set
		 */
		public void setWsName(String wsName) {
			this.wsName = wsName;
		}

		/**
		 * @param wsURL the wsURL to set
		 */
		public void setWsURL(String wsURL) {
			this.wsURL = wsURL;
		}

		/** output option **/
		private boolean verbose = false;

		public boolean isVerbose() {
			return verbose;
		}

		public void setVerbose(boolean verbose) {
			this.verbose = verbose;
		}
		
		/** constructor with provided web service URL */
		public communicationPort(String wsURL) {
			this.wsURL = wsURL;
			createStub();
		}

		/** constructor with provided UDDI location and name */
		public communicationPort(String uddiURL, String wsName){
			this.uddiURL = uddiURL;
			this.wsName = wsName;
			uddiLookup();
			createStub();
		}

		/** UDDI lookup */
		private void uddiLookup(){
			try {
				if (verbose)
					System.out.printf("Contacting UDDI at %s%n", uddiURL);
				UDDINaming uddiNaming = new UDDINaming(uddiURL);

				if (verbose)
					System.out.printf("Looking for '%s'%n", wsName);
				wsURL = uddiNaming.lookup(wsName);

			} catch (Exception e) {
				String msg = String.format("Client failed lookup on UDDI at %s!",
						uddiURL);
			}

			if (wsURL == null) {
				String msg = String.format(
						"Service with name %s not found on UDDI at %s", wsName,
						uddiURL);
			}
		}
		
		/** Stub creation and configuration */
		private void createStub() {
			if (verbose)
				System.out.println("Creating stub ...");
			service = new BrokerService();
			port = service.getBrokerPort();

			if (wsURL != null) {
				if (verbose)
					System.out.println("Setting endpoint address ...");
				BindingProvider bindingProvider = (BindingProvider) port;
				Map<String, Object> requestContext = bindingProvider
						.getRequestContext();
				requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
			}
		}
		
	}
		
	
}