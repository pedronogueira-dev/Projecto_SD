package pt.upa.broker.ws;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.jws.WebService;
import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.*;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.2_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType{

	private Map<String, TransporterClient> associatedTransporters=null;//new TreeMap<String, TransporterClient>();
	private Map<String, TransportView> transportList=new TreeMap<String, TransportView>();
	private Map<String,String> transportToJob=new TreeMap<String,String>();
	private Map<String, JobView> acceptedJobs=new TreeMap<String, JobView>();

	private int transpId=1;
	/**
	 * @return the associatedTransporters
	 */
	public Map<String, TransporterClient> getAssociatedTransporters() {
		return associatedTransporters;
	}


	/**
	 * @param associatedTransporters the associatedTransporters to set
	 */
	public void setAssociatedTransporters(Map<String, TransporterClient> associatedTransporters) {
		this.associatedTransporters = associatedTransporters;
	}


	/**
	 * @return the transportList
	 */
	public Map<String, TransportView> getTransportList() {
		return transportList;
	}


	/**
	 * @param transportList the transportList to set
	 */
	public void setTransportList(Map<String, TransportView> transportList) {
		this.transportList = transportList;
	}


	/**
	 * @return the transportToJob
	 */
	public Map<String, String> getTransportToJob() {
		return transportToJob;
	}


	/**
	 * @param transportToJob the transportToJob to set
	 */
	public void setTransportToJob(Map<String, String> transportToJob) {
		this.transportToJob = transportToJob;
	}


	/**
	 * @return the acceptedJobs
	 */
	public Map<String, JobView> getAcceptedJobs() {
		return acceptedJobs;
	}


	/**
	 * @param acceptedJobs the acceptedJobs to set
	 */
	public void setAcceptedJobs(Map<String, JobView> acceptedJobs) {
		this.acceptedJobs = acceptedJobs;
	}


	/**
	 * @return the transpId
	 */
	public int getTranspId() {
		return transpId;
	}


	/**
	 * @param transpId the transpId to set
	 */
	public void setTranspId(int transpId) {
		this.transpId = transpId;
	}


	private BrokerEndpointManager endpoint;
	
	private boolean isBackup=false;
	
	public void setIsBackup(boolean isBackup){
		this.isBackup=isBackup;
	}
	
	public boolean getIsBackup(){
		return isBackup;
	}
	
	public BrokerPort(BrokerEndpointManager endpoint){
		this.endpoint=endpoint;
		//System.out.println(endpoint.getTransporters());

	}


//	@Override
//	public String ping(String name) {
//		System.out.println("INCOMING REQUEST OF [" + name+"]");
//		return "Connected to Broker Server";
//	}

	
 	public String ping(String name){
 		String s="";
 		System.out.println("Incoming Connection from: "+name);
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
		System.out.println("Transporters List: "+endpoint.getTransporters());

 		for(TransporterClient t : associatedTransporters.values()){
			s+=t.ping(name) + "Connected" + "\n";
 			System.out.println(t.ping(name));
 		}
 		return name+" Connected to Broker Server.\nLinked Transporters: \n"+s;
 	}
// 	
 	
//	public void registerTransporter() throws JAXRException{
//		String uddiURL="http://localhost:9090";
//		UDDINaming uddiNaming = new UDDINaming(uddiURL);
//		Collection<String> registeredTransportServers = uddiNaming.list("UpaTransporter%");
//		
//		for(String name : registeredTransportServers){
//			//System.out.println("CONNECTING TO: "+name);
//			//String endpointAddress = uddiNaming.lookup(name);
//			TransporterClient c;
//			try {
//				c = new TransporterClient(name);
//				associatedTransporters.put(name, c);
//			} catch (TransporterClientException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}
//	
	
	private TransportStateView convertState(JobView j){
		TransportStateView ts;
		switch(j.getJobState()){
		case PROPOSED: return TransportStateView.BUDGETED;
		case ACCEPTED: return TransportStateView.BOOKED;
		case REJECTED: return TransportStateView.FAILED;
		case HEADING: return TransportStateView.HEADING;
		case ONGOING: return TransportStateView.ONGOING;
		case COMPLETED: return TransportStateView.COMPLETED;
		default: return null;
		}
	}
	
	private TransportView convertJobIntoTransport(JobView j, String id){
		//System.out.println("\n\nCONVERTING JOB INTO TRANSPORT");
		
		TransportView transp = new TransportView();
		
		transp.setOrigin(j.getJobOrigin());
		transp.setDestination(j.getJobDestination());
		transp.setPrice(j.getJobPrice());
		
		//System.out.println("CompanyName (job)"+j.getCompanyName());
		transp.setTransporterCompany(j.getCompanyName());
		//System.out.println("CompanyName (transport, after conversion)"+j.getCompanyName()+"\n\n");
		
		transp.setState(convertState(j));
		transp.setId(id);
		return transp;
	}
	
	private String generateId(){
		String transportName= "Transport number: "+ transpId;
		transpId+=1;
		return transportName;
	}
	
	public String requestTransport(String origin, String destination, int price) throws UnknownLocationFault_Exception,
	InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception
	{	
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
		if(origin==null)
			throw new UnknownLocationFault_Exception("Origin must be provided. On input as: "+origin, new UnknownLocationFault());
		if(destination==null)
			throw new UnknownLocationFault_Exception("Destination must be provided. On input as: "+destination, new UnknownLocationFault());
		if(price<0)
			throw new InvalidPriceFault_Exception("Desired price mus be greater than 10, given price was: "+price, new InvalidPriceFault());
		
		System.out.println(">>>TRANSPORT REQUESTED:\n FROM: "+origin+" TO: "+destination+" PRICE: "+price+"\n");
		
		TransportView transp =new TransportView();
		transp.setOrigin(origin);
		transp.setDestination(destination);
		transp.setPrice(price);
		transp.setState(TransportStateView.REQUESTED);
		transp.setId(generateId());
		
		
		transportList.put(transp.getId(), transp);
		
		////////////////////////////////////
		//		REQUESTING JOB
		///////////////////////////////////
		
		List<JobView> jobList = new ArrayList<JobView>();
		Map<String,TransporterClient> selectedTransporters = new TreeMap<String, TransporterClient>();
		JobView bestOption=null;
		
		for(TransporterClient c : associatedTransporters.values()){
			try {
				bestOption=c.requestJob(origin, destination, price);
			} catch (BadLocationFault_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new UnknownLocationFault_Exception(e.getMessage(), new UnknownLocationFault());
				} catch (BadPriceFault_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new UnavailableTransportPriceFault_Exception(e.getMessage(), new UnavailableTransportPriceFault());				
			}
			if(bestOption!=null){
				jobList.add(bestOption);
				selectedTransporters.put(bestOption.getJobIdentifier(), c);
			}
		}
		
		if(jobList.isEmpty()){
			transportList.get(transp.id).setState(TransportStateView.FAILED);
			throw new UnavailableTransportFault_Exception("", new UnavailableTransportFault());
		}
		
		/////////////////////////////////
		///		Comparing Prices
		////////////////////////////////
		bestOption=null;
		for(JobView j : jobList){
			if(bestOption == null){
				bestOption=j;
			}else{
				if(bestOption.getJobPrice()<j.getJobPrice()){
					try {
						selectedTransporters.get(j.getJobIdentifier()).decideJob(j.getJobIdentifier(), false);
					} catch (BadJobFault_Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						//throw new UnavailableTransportFault_Exception(e.getMessage(), new UnavailableTransportFault());
						//////////////////////////////////////////
					}
				}else{
					try {
						selectedTransporters.get(bestOption.getJobIdentifier()).decideJob(bestOption.getJobIdentifier(), false);
					} catch (BadJobFault_Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						/////////////////////////////////////////
					}
					bestOption=j;
				}
			}
		}
		
		if(bestOption.getJobPrice()>price){
			transportList.get(transp.id).setState(TransportStateView.FAILED);
			throw new UnavailableTransportPriceFault_Exception("", new UnavailableTransportPriceFault());
		}
		
		try {
			bestOption=selectedTransporters.get(bestOption.getJobIdentifier()).decideJob(bestOption.getJobIdentifier(), true);
		} catch (BadJobFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			////////////////////////////////////////////
		}
		
		System.out.println("BEST JOB FOUND:\n"
							+"ID: "+bestOption.getJobIdentifier()
							+"\nFrom: "+bestOption.getJobOrigin()+" To: "+bestOption.getJobDestination()
							+"\nCompany: "+bestOption.getCompanyName()
							+"\nPrice: "+bestOption.getJobPrice()
							+"\nState: "+bestOption.getJobState()+
							"\n");
		
		transp=convertJobIntoTransport(bestOption, transp.getId());
		
		//transportList.replace(transp.getId(), convertJobIntoTransport(bestOption, transp.getId()));
		
		transportList.replace(transp.getId(), transp);
		
		transportToJob.put(transp.getId(), bestOption.getJobIdentifier());
		
		acceptedJobs.put(bestOption.getJobIdentifier(), bestOption);

		transp=transportList.get(transp.getId());
		
		System.out.println("Result:"
							+ "\nT-ID: "+ transp.getId()
							+ "\nTRANSPORTER COMPANY: "+ transp.getTransporterCompany()
							+ "\nFROM: "+transp.getOrigin()+ " TO: "+ transp.getDestination()
							+ "\nPRICE: "+transp.getPrice()
							+ "\nT-STATE: "+transp.getState()
							+"\n");
		
		return transp.getId();
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception
	{
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
		if(id==null){
			throw new UnknownTransportFault_Exception("Invalid id. Transport Id must not be null", new UnknownTransportFault());
		}
		TransportView t = transportList.get(id);
		
		if(t==null){
			throw new UnknownTransportFault_Exception("Couldn't find Transport: "+id, new UnknownTransportFault());
		}
		
		String jId = transportToJob.get(id);
		JobView j=acceptedJobs.get(jId);
		j=associatedTransporters.get(j.getCompanyName()).jobStatus(j.getJobIdentifier());
		acceptedJobs.replace(jId, j);
		t=convertJobIntoTransport(j, t.getId());
		transportList.replace(t.getId(), t);
		
		return t;
	}

	public List<TransportView> listTransports(){
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
		List<TransportView> list= new ArrayList<TransportView>();
		list.addAll(transportList.values());
		return list;
	}

	public void clearTransports(){
		
		for(TransporterClient t : associatedTransporters.values()){
			t.clearJobs();
		}
		transportList.clear();
		transportToJob.clear();
		acceptedJobs.clear();

		transpId=1;
		
		return;
	}


	@Override
	public void brokerConsistencyManagement() throws BrokerConsistencyManagementFault_Exception {
		// TODO Auto-generated method stub
		System.out.println("Replicating Primary Server information to the Backup");
		
		return;
	}


	@Override
	public void promoteToMain() throws PromoteToMainFault_Exception {
		// TODO Auto-generated method stub
		if(!getIsBackup())
			return;

		endpoint.unpublishFromUDDI();
		endpoint.setWsName("UpaBroker");
		endpoint.setWsURL("http://localhost:8080/broker-ws/endpoint");
		try {
			//endpoint.publishToUDDI();
			System.out.println("\n\nPROMOTING SERVER FROM BACKUP TO MAIN");
			endpoint.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Unable to Rebind");
			throw new PromoteToMainFault_Exception("Só que não", new PromoteToMainFault());
		}
		setIsBackup(false);
		try {
			endpoint.registerTransporter();
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		associatedTransporters=getAssociatedTransporters();
		
		endpoint.awaitConnections();
		
	}
	
	
}
