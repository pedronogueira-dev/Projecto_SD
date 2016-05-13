package pt.upa.broker.ws;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import pt.upa.transporter.ws.JobStateView;
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

	private Map<String, TransporterClient> associatedTransporters=new TreeMap<String, TransporterClient>();//new TreeMap<String, TransporterClient>();
	private Map<String, TransportView> transportList=new TreeMap<String, TransportView>();
	private Map<String,String> transportToJob=new TreeMap<String,String>();
	private Map<String, JobView> acceptedJobs=new TreeMap<String, JobView>();

	private int transpId=0;
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
	
 	public String ping(String name){
 		String s="";
 		System.out.println("Incoming Connection from: "+name);
 		if(!isBackup){
 			if(associatedTransporters ==null)
 				this.associatedTransporters=endpoint.getTransporters();
 			System.out.println("Transporters List: "+endpoint.getTransporters());

 			for(TransporterClient t : associatedTransporters.values()){
 				s+=t.ping(name) + "Connected" + "\n";
 				System.out.println(t.ping(name));
 			}
 		}
 		return name+" Connected to Broker Server.\nLinked Transporters: \n"+s;
 	}

	private String stateToString(JobStateView s){
		switch(s){
		case PROPOSED : return "P";
		case ACCEPTED : return "A";
		case REJECTED : return "R";
		case HEADING : return "H";
		case ONGOING : return "O";
		case COMPLETED : return "C";
		default: return null;
		}
	}
 	
 	
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
		if(!(endpoint.getSecundaryBroker()==null))
			endpoint.getSecundaryBroker().port.updateTransportId(transpId);
		return transportName;
	}
	
	public String requestTransport(String origin, String destination, int price) throws UnknownLocationFault_Exception,
	InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception
	{	
		if(associatedTransporters.isEmpty())
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
		//endpoint.getSecundaryBroker().port.updateTransportList(transp.getId(), transp);
		
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
			//endpoint.getSecundaryBroker().port.updateTransportList(transp.id, transp);
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
			//endpoint.getPort().updateTransportList(transp.id, transportList.get(transp.id));
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
		//endpoint.getSecundaryBroker().port.updateTransportToDo(transp.getId(), bestOption.getJobIdentifier());
		
		acceptedJobs.put(transp.getId(), bestOption);
		//endpoint.getSecundaryBroker().port.updateAcceptedJobs(bestOption.getCompanyName(), bestOption.getJobIdentifier(), bestOption.getJobOrigin(), bestOption.getJobDestination(), bestOption.getJobPrice(), stateToString(bestOption.getJobState()));
		
		transp=transportList.get(transp.getId());
		//endpoint.getSecundaryBroker().port.updateTransportList(transp.getId(), transp);
		
		System.out.println("Result:"
							+ "\nT-ID: "+ transp.getId()
							+ "\nTRANSPORTER COMPANY: "+ transp.getTransporterCompany()
							+ "\nFROM: "+transp.getOrigin()+ " TO: "+ transp.getDestination()
							+ "\nPRICE: "+transp.getPrice()
							+ "\nT-STATE: "+transp.getState()
							+"\n");
		
		//endpoint.getSecundaryBroker().port.updateTransportList(transp.getId(), transp);
		brokerConsistencyManagement();
		return transp.getId();
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception
	{
		System.out.println("INPUT: "+id);
		System.out.println(acceptedJobsToString());
		if(associatedTransporters.isEmpty())
			this.associatedTransporters=endpoint.getTransporters();
		if(id==null){
			throw new UnknownTransportFault_Exception("Invalid id. Transport Id must not be null", new UnknownTransportFault());
		}
		TransportView t = transportList.get(id);
		
		if(t==null){
			throw new UnknownTransportFault_Exception("Couldn't find Transport: "+id, new UnknownTransportFault());
		}
		
		//String jId = transportToJob.get(id);
		JobView j=acceptedJobs.get(id);
		System.out.println("ID: "+j.getJobPrice() +"\nCompanyName: "+j.getCompanyName()+"\n");
		if(!associatedTransporters.isEmpty()){
			System.out.println(associatedTransporters);
			j=associatedTransporters.get(j.getCompanyName()).jobStatus(j.getJobIdentifier());
			acceptedJobs.replace(id, j);
			}
		t=convertJobIntoTransport(j, t.getId());
		transportList.replace(t.getId(), t);
		
		//endpoint.getPort().updateTransportList(t.getId(), t);
		brokerConsistencyManagement();
		return t;
	}

	public List<TransportView> listTransports(){
		if(associatedTransporters.isEmpty())
			this.associatedTransporters=endpoint.getTransporters();
		List<TransportView> list= new ArrayList<TransportView>();
		list.addAll(transportList.values());
		brokerConsistencyManagement();
		return list;
	}

	public void clearTransports(){
		if(!isBackup)
		for(TransporterClient t : associatedTransporters.values()){
			t.clearJobs();
		}
		transportList.clear();
		transportToJob.clear();
		acceptedJobs.clear();

		transpId=1;
//		if(endpoint.getSecundaryBroker()!=null)
//			endpoint.getSecundaryBroker().clear();
		//System.out.println("has secundary broker? "+endpoint.getSecundaryBroker());
		if(endpoint.getSecundaryBroker()!=null){
			endpoint.getSecundaryBroker().port.clearTransports();
			System.out.println("clearing Broker Information on Backup server.");
		}
		System.out.println(transportListToString());
		System.out.println(acceptedJobsToString());
		System.out.println("<<<<End OF ClearTransports>>>>>");
		return;
	}

	private String transportListToString(){
		String list ="TransportList:";
		for(TransportView t : transportList.values()){
			list+="\n<Id>=" + t.getId() + " <Origin|Dest>= "+t.getOrigin()+"|"+t.getDestination();
			list+="\t<State>="+t.getState();
		}
		list +="\n===== number of transports : "+transportList.size();
		return list;
	}

	private String transportToJobToString(){
		String list ="TransportToJobList:";
		list+=transportToJob.toString();
		list +="\n==== number of transport to job correspondence entries : "+transportToJob.size();
		return list;
	}
	
	public String acceptedJobsToString(){
		String list ="acceptedJob List:";
		for(JobView j : acceptedJobs.values()){
			list+="\n<Id>=" + j.getJobIdentifier() + " <Origin|Dest>= "+j.getJobOrigin()+"|"+j.getJobDestination();
			list+="\t<State>="+j.getJobState();
		}
		list +="\n===== number of transports : "+acceptedJobs.size();
		return list;
	}
	
	@Override
	public void brokerConsistencyManagement(){
		// TODO Auto-generated method stub
		if(endpoint.getSecundaryBroker()==null)
			return;
		System.out.print("----------------------------------------------------------");
		System.out.println("Replicating Primary Server information to the Backup");
		System.out.println("Current System trannsportID: "+transpId);
		System.out.println(transportListToString());
		System.out.println(acceptedJobsToString());
		System.out.println("_______________________________________________________");
		if(endpoint.getSecundaryBroker()!=null){
			endpoint.getSecundaryBroker().port.updateTransportId(transpId);
			for(String s : transportList.keySet())
				endpoint.getSecundaryBroker().port.updateTransportList(s, transportList.get(s));
			for(String s : acceptedJobs.keySet()){
				//System.out.println("JOB ID:"+j.getJobIdentifier());
				JobView j=acceptedJobs.get(s);
				endpoint.getSecundaryBroker().port.updateAcceptedJobs(s, j.getCompanyName(), j.getJobIdentifier(), j.getJobOrigin(), j.getJobDestination(), j.getJobPrice(), convertJobStateToString(j.getJobState()));
				}
		}
		return;
	}

	//FIX
	@Override
	public void promoteToMain() throws PromoteToMainFault_Exception {
		// TODO Auto-generated method stub
		if(!getIsBackup())
			return;
		
		//brokerConsistencyManagement();
		
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
			throw new PromoteToMainFault_Exception("Unable to Rebind", new PromoteToMainFault());
		}
		setIsBackup(false);
		try {
			endpoint.registerTransporter();
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		associatedTransporters=getAssociatedTransporters();
		
		System.out.println("Current System trannsportID: "+transpId);

//		System.out.println(transportListToString());
//		System.out.println(acceptedJobsToString());
		
		endpoint.awaitConnections();
		
		
		
	}



	@Override
	public void updateTransportId(int id) {
		// TODO Auto-generated method stub
		System.out.println("-----Updating Transport number. Input was: "+id);
		transpId=id;
		System.out.println("----------------------------------------");
	}


	@Override
	public void updateTransportToDo(String transportId, String jobID) {
		// TODO Auto-generated method stub
		System.out.println("-----Updating TransportToDo Map. Input was: TransportID = "+transportId + "<> JobID = "+jobID);
		transportToJob.put(transportId, jobID);
		System.out.println("----------------------------------------");
	}

	private JobStateView convertStringToJobState(String state){
		switch(state){
		case "P":return JobStateView.PROPOSED;
		case "A":return JobStateView.ACCEPTED;
		case "O":return JobStateView.ONGOING;
		case "H":return JobStateView.HEADING;
		case "C":return JobStateView.COMPLETED;
		case "R":return JobStateView.REJECTED;
		}
		return null;
	}

	private String convertJobStateToString(JobStateView state){
		switch(state){
		case PROPOSED : return "P";
		case ACCEPTED : return "A";
		case ONGOING  : return "O";
		case HEADING  :	return "H";
		case COMPLETED: return "C";
		case REJECTED : return "R";
		}
		return null;
	}
	
	@Override
	public void updateAcceptedJobs(String id,String companyName, String jobId, String origin, String destination, int price,
			String state) {
		// TODO Auto-generated method stub
		System.out.println("-----Updating AcceptedJobs List. New Transport was:\nCompanyName: "+companyName+"\nJobId: "+jobId+"\nOrigin: "+origin+"\nDestination: "+destination+"\nPrice: "+price+"\nState: "+state);
		JobView job = new JobView();
		job.setCompanyName(companyName);
		job.setJobIdentifier(jobId);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(price);
		job.setJobState(convertStringToJobState(state));
		acceptedJobs.put(id, job);
		System.out.println("----------------------------------------------------");
	}

	private TransportStateView convertStringToTransportState(String state){
		switch(state){
		case "R":return TransportStateView.REQUESTED;
		case "Booked":return TransportStateView.BOOKED;
		case "Budgeted":return TransportStateView.BUDGETED;
		case "H":return TransportStateView.HEADING;
		case "O":return TransportStateView.ONGOING;
		case "C":return TransportStateView.COMPLETED;
		case "F":return TransportStateView.FAILED;
		}
		return null;
	}

	@Override
	public void updateTransportState(String transportId, String transportState) {
		// TODO Auto-generated method stub
		System.out.println("-----Updating Transport State. TransportId was: "+transportId+" new State: "+transportState);
		transportList.get(transportId).setState(convertStringToTransportState(transportState));
		System.out.println("----------------------------------------------");
	}


	@Override
	public void updateTransportList(String transportId, TransportView transport) {
		// TODO Auto-generated method stub
		System.out.println("-----Updating Transport List. Transport Id was: "+transportId);
		if(transportList.containsKey(transportId))
			transportList.replace(transportId, transport);
		else transportList.put(transportId, transport);
		System.out.println("--------------------------------------------------------------");
	}

	Timer t=new Timer();
	
	private class lifeProof extends TimerTask{
		public void run(){
			if(isBackup)
				isAlive();
			else amAlive();
		}
	}
	
	@Override
	public void amAlive() {
		// TODO Auto-generated method stub
		if(isBackup)
			endpoint.setMainServerIsAlive(true);
		else{
			endpoint.getSecundaryBroker().port.amAlive();
		//System.out.println("|||Confirmed Life");
		t.schedule(new lifeProof(), 1000);
		}
	}

	public void stop(){
		t.cancel();
		t.purge();
	}

	public void setMainServerIsAlive(boolean b){
		if(isBackup){
			endpoint.setMainServerIsAlive(b);
		}
	}
	@Override
	public void isAlive() {
		// TODO Auto-generated method stub
		if(endpoint.isMainServerIsAlive()){
			//System.out.println(">>>>>>>>>>>>>>>>>>>>LIFE CONFIRMED");
			endpoint.setMainServerIsAlive(false);
			t.schedule(new lifeProof(), 1500);
		}else{
			stop();
			try {
				promoteToMain();
			} catch (PromoteToMainFault_Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Couldnt Promote");
			}
		}
	}
	
}
