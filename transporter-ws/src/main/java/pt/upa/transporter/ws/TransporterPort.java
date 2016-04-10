package pt.upa.transporter.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
	
	private int _transporterNumber=0;
	private List<JobView> _jobs=new ArrayList<JobView>();
	private int _jobId=0;
	
	public TransporterPort(int number){
		_transporterNumber=number;
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
			}while(proposal>0&&proposal<=price);
			return proposal;
		}		
			//int topPercentage = 100;
			Double result;
			do{
			result=rn.nextDouble() * price;
			}while(result>0&&result<100);
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

	public String ping(String name){
		return "Transporter: "+ name+ " Connected to TransporterServer";
	}

	public JobView requestJob(String origin,String destination,int price)throws BadLocationFault_Exception, BadPriceFault_Exception{
		
		//CHECKING IF ORIGIN AND DESTINATION ARE KNOWN TO THE SYSTEM
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
			if(!((_NorthRegion.contains(origin)||_CenterRegion.contains(origin))
						&&(_NorthRegion.contains(destination)||_CenterRegion.contains(destination))
						)){
					return null;
			}
		}
		
		int priceProposal=priceCalculator(price);
		
		JobView job =new JobView();
		job.setCompanyName(""+_transporterNumber);
		job.setJobOrigin(origin);
		job.setJobDestination(destination);
		job.setJobPrice(priceProposal);
		job.setJobIdentifier("Transporter"+_transporterNumber+": Transport number: "+_jobId);
		
		job.setJobState(JobStateView.PROPOSED);
		return job;

	}

	public JobView decideJob(String id,boolean accept)throws BadJobFault_Exception{
		for(JobView job : _jobs){
			if(job.getJobIdentifier().equals(id)){
				if(accept){
					job.setJobState(JobStateView.ACCEPTED);
					return job;
				}else{
					job.setJobState(JobStateView.REJECTED);
					}
			}
		}
		return null;
	}

	public JobView jobStatus(String id){
		JobView chosenJob=null;
		for(JobView j : _jobs){
			if(j.getJobIdentifier().equals(id)){
				
			}
		}
		return null;
	}

	public List<JobView> listJobs(){
		return _jobs;
	}

	public void clearJobs(){
		_jobs.clear();
	}
}
