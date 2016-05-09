package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.Date;
import java.util.TreeMap;

import java.util.concurrent.ThreadLocalRandom;

import javax.jws.WebService;
@WebService(
		endpointInterface="pt.upa.transporter.ws.TransporterPortType",
		wsdlLocation="transporter.1_0.wsdl",
		name="TransporterWebService",
		portName="TransporterPort",
		targetNamespace="http://ws.transporter.upa.pt/",
		serviceName="TransporterService"
		)
public class TransporterPort implements TransporterPortType {

	private List<String> _NorthRegion=Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Braganca");
	private List<String> _CenterRegion=Arrays.asList("Lisboa", "Leiria", "Santarem", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
	private List<String> _SouthRegion=Arrays.asList("Setúbal","Évora", "Portalegre", "Beja", "Faro");
	
	//private Map<String,Date> timers=new TreeMap<String,Date>();
	private LinkedHashMap<String,JobView> jobs=new LinkedHashMap<String,JobView>();
	private int _jobId=0;
	private TransporterEndpointManager endpoint;
	private int _transporterNumber;
	private Timer threadTimer=new Timer();
	
	public TransporterPort(TransporterEndpointManager endpoint){
		this.endpoint=endpoint;
		_transporterNumber=endpoint.getTransporterNumber();
		
		System.out.println("PORT OF SERVER NUMBER:::"+_transporterNumber);
	}

	public TransporterPort(int i){
		this.endpoint=null;
		_transporterNumber=i;
		
		System.out.println("PORT OF SERVER NUMBER:::"+_transporterNumber);
	}
	
	private void jobIdInc(){
		_jobId=_jobId+1;
	}
	private void jobIdDec(){
		_jobId=_jobId-1;
	}
	
	
	 class changeTask extends TimerTask {
		private JobView _jobToChange;
		private Timer	_time;
		public changeTask(JobView job,Timer t) {
			_jobToChange=job;
			_time=t;
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ChangeStatus(_jobToChange);
			this.cancel();
		}

		public void ChangeStatus(JobView j){
			int delay;
			switch(j.getJobState()){
			case ACCEPTED : j.setJobState(JobStateView.HEADING);
							System.out.println(j.getJobIdentifier() +" from Accepted to Heading");
							//this.cancel();
							//threadTimer.purge();
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case HEADING : j.setJobState(JobStateView.ONGOING);
							System.out.println(j.getJobIdentifier() +" from Heading to Ongoing");
							//this.cancel();
							//threadTimer.purge();
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case ONGOING : j.setJobState(JobStateView.COMPLETED);
							System.out.println(j.getJobIdentifier() +" from Ongoing to Completed");
							//this.cancel();
							//threadTimer.purge();
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case COMPLETED :System.out.println(j.getJobIdentifier() +"Is already Completed");
							//this.cancel();
							//threadTimer.purge();
							break;
			default:break;
			}
		}
	}
	
	public void stop(){
		threadTimer.cancel();
		threadTimer.purge();
	}
	private int priceCalculator(int price){
		int proposal;
		int maximum =10;
		int minimum =1;

		Random rn = new Random();
		int range;
		//int randomNum;

		
		System.out.println("CALCULATING PRICE");
		if(price <=10){
			do{
				proposal=0;
				maximum =10;
				minimum =1;
				range=maximum-minimum;
				
				proposal=rn.nextInt(range) + minimum;
			}while(proposal<0||proposal>price);
			return proposal;
		}		
			//int topPercentage = 100;
			Double result;
			do{
			result=rn.nextDouble() * price;
			}while(result<0||result>100);
			proposal= (int)Math.round(result);
			System.out.println("\n\nProposta inicial com desconto:"+proposal+ " (preço original: "+price+")");
			if(price%2 ==0){
				System.out.print("Preço Par:");
				if(_transporterNumber%2==0){
					proposal=(int)(price - result);
					System.out.println("\tEven Transporter: "+proposal);
				}else{
					proposal=(int)(price + result);
					System.out.println("\tOdd Transporter: "+proposal);
				}
			}else{
				System.out.print("Odd Price: ");
				if(_transporterNumber%2==0){
					proposal=(int)(price + result);
					System.out.println("\tEven Transporter: "+proposal);
				}else{
					proposal=(int)(price - result);
					System.out.println("\tOdd Transporter: "+proposal);
				}
			}
			
			return proposal;
	
	}

	private String jobIdString(){
		return "UpaTransporter"+_transporterNumber+".Transport number:"+_jobId;
	}
	public String ping(String name){
		System.out.println("Incoming request from: "+name);
		System.out.println( "Requested Connection from: "+ name+ ". Accepted, Now Connected to UpaTransporter"+_transporterNumber);
		return "UpaTransporter"+_transporterNumber;
	}

	public JobView requestJob(String origin,String destination,int price)throws BadLocationFault_Exception, BadPriceFault_Exception{
		System.out.println("INCOMING JOB REQUEST:: ORIGIN - "+origin+ " DESTINATION - "+destination + " PRICE - "+ price);
		
			if(!(_NorthRegion.contains(origin)||
					_CenterRegion.contains(origin)||
					_SouthRegion.contains(origin))){
				throw new BadLocationFault_Exception(origin,new BadLocationFault());
			}else{
				if(!(_NorthRegion.contains(destination)||
						_CenterRegion.contains(destination)||
						_SouthRegion.contains(destination))){
					throw new BadLocationFault_Exception(destination,new BadLocationFault());
				}
			}
			//String operate = (_transporterNumber%2 == 0) ? "NORTE|CENTER":"CENTER|SUL";
			//System.out.println("ORIGIN AND DESTINATION ARE KNOWN TO THE SYSTEM\n\n"+"Transporter number:"+ _transporterNumber+"\nOperates on "+ operate);
		//CHECKING IF PRICE IS GREATER THAN 0
		if(price<0){
			throw new BadPriceFault_Exception("Invalid price: "+price, new BadPriceFault());
		}
		if(price >100){
			return null; 
		}
		//CHECKING IF CURRENT TRANSPORTER OPERATES WITHIN BOTH THE ORIGIN AND DESTINATION REGIONS
		if(_transporterNumber%2==0){
			if(!((_NorthRegion.contains(origin)||_CenterRegion.contains(origin))
					&&(_NorthRegion.contains(destination)||_CenterRegion.contains(destination))
					)){
				return null;
			}
		}else{
			if(!((_SouthRegion.contains(origin)||_CenterRegion.contains(origin))
						&&(_SouthRegion.contains(destination)||_CenterRegion.contains(destination))
						)){
					return null;
			}
		}
		int priceProposal= priceCalculator(price);
		
		JobView job =new JobView();
		job.setCompanyName("UpaTransporter"+_transporterNumber);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(priceProposal);
		job.setJobIdentifier(jobIdString());
		job.setJobState(JobStateView.PROPOSED);
		
		//_jobs.add(job);
		jobs.put(job.getJobIdentifier(), job);
		jobIdInc();
		
		//
		System.out.println(jobtoString(job));
		return job;

	}
	
public JobView decideJob(String id,boolean accept)throws BadJobFault_Exception{
		
		if(id==null || id.equals(""))
				throw new BadJobFault_Exception("Job: "+id+" NOT FOUND", new BadJobFault());

		
		//if(jobs.containsKey(id)){
			JobView job =jobs.get(id);
			if(job.getJobState()!= JobStateView.PROPOSED)
				throw new BadJobFault_Exception("Job: "+id+" NOT FOUND", new BadJobFault());

			if(accept){
				job.setJobState(JobStateView.ACCEPTED);
				int delay=ThreadLocalRandom.current().nextInt(1000, 5001);
				threadTimer.schedule(new changeTask(job, threadTimer), delay);
				return job;
			}else{
				
				job.setJobState(JobStateView.REJECTED);
				return job;
			}
		//}
		

	}

	public JobView jobStatus(String id){
		
		if(id==null || id.equals(""))
			return null;

		if(jobs.containsKey(id)){
			return jobs.get(id);
		}
		
		return null;
	}

	public List<JobView> listJobs(){
		List<JobView> jobList=new ArrayList<JobView>();
			jobList.addAll(jobs.values());
			return jobList;
	}

	public void clearJobs(){
		jobs.clear();
		_jobId=0;
	}
	
	public String jobtoString(JobView trip){
		return "JOB PROPOSAL:\nCompany Name: "+trip.getCompanyName()+
				"\nTransPortID: "+trip.getJobIdentifier()+
				"\nOrigin: "+trip.getJobOrigin()+
				"\nDestination: "+trip.getJobDestination()+
				"\nPrice: "+trip.getJobPrice()+
				"\nStatus: "+trip.getJobState();  
				
				
	}
}
