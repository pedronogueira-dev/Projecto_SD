package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
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
	private Map<String,JobView> jobs=new TreeMap<String,JobView>();
	private int _transporterNumber=0;
	private List<JobView> _jobs=new ArrayList<JobView>();
	private int _jobId=0;
	private int _maxSeconds=5000;
	
	private Timer threadTimer=new Timer();
	
	public TransporterPort(int number){
		_transporterNumber=number;
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
		}

		public void ChangeStatus(JobView j){
			int delay;
			switch(j.getJobState()){
			case ACCEPTED : j.setJobState(JobStateView.HEADING);
							System.out.println("CHANGED 1");
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case HEADING : j.setJobState(JobStateView.ONGOING);
							System.out.println("CHANGED 2");
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case ONGOING : j.setJobState(JobStateView.COMPLETED);
							System.out.println("CHANGED 3");
							delay=ThreadLocalRandom.current().nextInt(1000, 5001);
							_time.schedule(new changeTask(j,_time), delay);
							break;
			case COMPLETED :System.out.println("ARRIVED");
							threadTimer.purge();
							break;
			default: break;
			}
		}
	}
	
	
	private int priceCalculator(int price){
		int proposal;
		int maximum =10;
		int minimum =1;

		Random rn = new Random();
		int range;
		//int randomNum;

		if(price > 100){
			return price;//no proposal is made
		}
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
			if(price%2 ==0){
				if(_transporterNumber%2==0){
					proposal=(int)(price - result);
				}else{
					proposal=(int)(price + result);
				}
			}else{
				if(_transporterNumber%2==0){
					proposal=(int)(price + result);
				}else{
					proposal=(int)(price - result);
				}
			}
			return proposal;
	
	}

	private String jobIdString(){
		return "Transporter"+_transporterNumber+".Transport number:"+_jobId;
	}
	public String ping(String name){
		return "Transporter: "+ name+ " Connected to TransporterServer";
	}

	public JobView requestJob(String origin,String destination,int price)throws BadLocationFault_Exception, BadPriceFault_Exception{
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
		//CHECKING IF PRICE IS GREATER THAN 0
		if(price<0){
			throw new BadPriceFault_Exception("Invalid price: "+price, new BadPriceFault());
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
		job.setCompanyName(""+_transporterNumber);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(priceProposal);
		job.setJobIdentifier(jobIdString());
		job.setJobState(JobStateView.PROPOSED);
		
		//_jobs.add(job);
		jobs.put(job.getJobIdentifier(), job);
		jobIdInc();
		
		new changeTask(job,threadTimer);
		
		return job;

	}
	

	private Date getChangeTime(){
		Date d;
		Random r=new Random();
		long delay =(long) (r.nextDouble()*_maxSeconds);
		d=new Date(System.currentTimeMillis()+delay);
		return d;
	}
	public JobView decideJob(String id,boolean accept)throws BadJobFault_Exception{
		/*for(JobView job : _jobs){
			if(job.getJobIdentifier().equals(id)){
				if(accept){
					job.setJobState(JobStateView.ACCEPTED);
					timers.put(job.getJobIdentifier(),getChangeTime());
					return job;
				}else{
					job.setJobState(JobStateView.REJECTED);
					_jobs.remove(job);
					return job;
					}
			}
			*/
		if(jobs.containsKey(id)){
			JobView job =jobs.get(id);
			if(accept){
				job.setJobState(JobStateView.ACCEPTED);
				int delay=ThreadLocalRandom.current().nextInt(1000, 5001);
				threadTimer.schedule(new changeTask(job, threadTimer), delay);
				return job;
			}else{
				
				job.setJobState(JobStateView.REJECTED);
				//jobs.remove(id);
				return job;
			}
		}

		throw new BadJobFault_Exception("Job: "+id+" NOT FOUND", new BadJobFault());
	}

	public JobView jobStatus(String id){
		JobView chosenJob=null;
		Date time;
		/*
		for(JobView j : _jobs){
			if(j.getJobIdentifier().equals(id)){
				chosenJob=j;
				time=timers.get(j.getJobIdentifier());
				if(time.compareTo(new Date(System.currentTimeMillis()))>0){
					ChangeStatus(j);
				}
				return j;
			}
		}*/
		
		if(jobs.containsKey(id)){
			//time=timers.get(id);
			//if(time.compareTo(new Date(System.currentTimeMillis()))>0){
				//ChangeStatus(jobs.get(id));
			//}
			return jobs.get(id);
		}
		return null;
	}

	public List<JobView> listJobs(){
		List<JobView> jobList=new ArrayList<JobView>();
			jobList.addAll(jobs.values());
		//return _jobs;
			return jobList;
	}

	public void clearJobs(){
		//_jobs.clear();
		jobs.clear();
		//timers.clear();
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
