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
    wsdlLocation="broker.1_0.wsdl",
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
	private BrokerEndpointManager endpoint;
	
	
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
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
		System.out.println(endpoint.getTransporters());

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
		TransportView transp = new TransportView();
		
		transp.setOrigin(j.getJobOrigin());
		transp.setDestination(j.getJobDestination());
		transp.setPrice(j.getJobPrice());
		transp.setTransporterCompany(j.getCompanyName());
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
		
		if(price<0)
			throw new InvalidPriceFault_Exception("Desired price mus be greater than 10, given price was: "+price, new InvalidPriceFault());
		System.out.println("TRANSPORT REQUESTED:\n FROM: "+origin+" TO: "+destination+"\nPRICE: "+price);
		
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
			//throw new UnavailableTransportFault_Exception(e.getMessage(), new UnavailableTransportFault());
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
		
		try {
			bestOption=selectedTransporters.get(bestOption.getJobIdentifier()).decideJob(bestOption.getJobIdentifier(), true);
		} catch (BadJobFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			////////////////////////////////////////////
		}
		
		transportList.replace(transp.getId(), convertJobIntoTransport(bestOption, transp.getId()));
		transportToJob.put(transp.getId(), bestOption.getJobIdentifier());
		acceptedJobs.put(bestOption.getJobIdentifier(), bestOption);

		System.out.println("TRANSPORT:\n"
							+ "T-ID: "+ transp.getId()
							+ "TRANSPORTER COMPANY: "+ transp.getTransporterCompany()
							+ "FROM: "+transp.getOrigin()+ "TO: "+ transp.getDestination()
							+ "PRICE: "+transp.getPrice()
							+ "T-STATE: "+transp.getState());
		
		return transp.getId();
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception
	{
		if(associatedTransporters ==null)
			this.associatedTransporters=endpoint.getTransporters();
			
		TransportView t = transportList.get(id);
		if(id==null||t==null){
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
}
