package pt.upa.transporter.ws;

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
	
	
	public TransporterPort(int number){
		_transporterNumber=number;
	}

	private int priceCalculator(int price){
		int proposal;
		int maximum =10;
		int minimum =1;

		Random rn = new Random();
		int range;
		int randomNum;

		if(price > 100){
			return 0;
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
			int topPercentage = 100;
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
		return null;
	}

	public JobView decideJob(String id,boolean accept)throws BadJobFault_Exception{
		return null;
	}

	public JobView jobStatus(String id){
		return null;
	}

	public List<JobView> listJobs(){
		return null;
	}

	public void clearJobs(){
	}
}
