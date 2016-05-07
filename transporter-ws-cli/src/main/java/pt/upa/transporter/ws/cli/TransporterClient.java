package pt.upa.transporter.ws.cli;


import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobView;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

public class TransporterClient {

	private TransporterPortType port;
	private TransporterService service =null;

	private String uddiURL = null;
	private String wsName = null;
	private String wsURL = null;
	
	public String getWsURL(){
		return wsURL;
	}
	
	private boolean verbose =false;
	public boolean isVerbose(){
		return verbose;
	}
	
	public void setVerbose(boolean verbose){
		this.verbose=verbose;
	}
	
	public TransporterClient(String wsURL)throws TransporterClientException{
		this.wsURL = wsURL;
		System.out.println("Created TransporterClient with :"+wsURL);
		createStub();
	}
	
	public TransporterClient(String uddiURL, String wsName) throws TransporterClientException{
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		uddiLookup();
		createStub();
	}
	
	
	private void uddiLookup() throws TransporterClientException{
		try{
			if(verbose)
				System.out.printf("Contacting UDDI at %s%n", uddiURL);
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			
			if(verbose)
				System.out.printf("Looking for '%s'%n", wsName);
			wsURL = uddiNaming.lookup(wsName);
		}catch(Exception e){
			String msg = String.format("Client failed lookup on UDDI at %s!", uddiURL);
			throw new TransporterClientException(msg);
		}
		
		if(wsURL == null){
			String msg = String.format("Service with name %s not found on UDDI at %s", wsName, uddiURL);
			throw new TransporterClientException(msg);
			}
	}
	
	private void createStub(){
		if(verbose)
			System.out.println("Creating stub ...");
		service = new TransporterService();
		port = service.getTransporterPort();
		
		if(wsURL != null){
			if(verbose)
				System.out.println("Setting endpoint address ...");
			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();
			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		}
	}
	

	public String ping(String name){
		return port.ping(name);
	}


    public JobView requestJob(String origin,String destination,int price) throws BadLocationFault_Exception, BadPriceFault_Exception{
    	return port.requestJob(origin, destination, price);
    }

    public JobView decideJob(String id,boolean accept) throws BadJobFault_Exception{
    	return port.decideJob(id, accept);
    }

   	public JobView jobStatus(String id){
   		return port.jobStatus(id);
   	}

    public List<JobView> listJobs(){
    	return port.listJobs();
    }

    public void clearJobs(){
    	port.clearJobs();
    }

}
