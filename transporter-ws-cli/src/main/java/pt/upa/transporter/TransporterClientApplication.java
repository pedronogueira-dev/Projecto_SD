package pt.upa.transporter;

import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;


import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(TransporterClientApplication.class.getSimpleName() + " starting...");


// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", TransporterClient.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		TransporterPortType port = service.getTransporterPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

		try {
			TransporterClient client=new TransporterClient(port);
			System.out.println(client.ping("Client1"));
			
			System.out.println("------LISTING-----");
			for(JobView j: client.listJobs()){
				System.out.println("JOB:\nCompany Name: "+j.getCompanyName()+
						"\nTransPortID: "+j.getJobIdentifier()+
						"\nOrigin: "+j.getJobOrigin()+
						"\nDestination: "+j.getJobDestination()+
						"\nPrice: "+j.getJobPrice()+
						"\nStatus: "+j.getJobState());
			}
			System.out.println("---------------------------------");
			//FIRST REQUEST
			JobView trip= client.requestJob("Lisboa", "Santarem", 10);
			System.out.println("JOB REQUEST: LISBOA->SANTAREM, PRICE:10");
			System.out.println("JOB PROPOSAL:\nCompany Name: "+trip.getCompanyName()+
											"\nTransPortID: "+trip.getJobIdentifier()+
											"\nOrigin: "+trip.getJobOrigin()+
											"\nDestination: "+trip.getJobDestination()+
											"\nPrice: "+trip.getJobPrice()+
											"\nStatus: "+trip.getJobState());
			System.out.println("\n\nAFTER APPROVAL");
			trip=client.decideJob(trip.getJobIdentifier(), true);
			System.out.println("APPROVED JOB:\nCompany Name: "+trip.getCompanyName()+
					"\nTransPortID: "+trip.getJobIdentifier()+
					"\nOrigin: "+trip.getJobOrigin()+
					"\nDestination: "+trip.getJobDestination()+
					"\nPrice: "+trip.getJobPrice()+
					"\nStatus: "+trip.getJobState());
			
			System.out.println("------LISTING-----");
			for(JobView j: client.listJobs()){
				System.out.println("JOB:\nCompany Name: "+j.getCompanyName()+
						"\nTransPortID: "+j.getJobIdentifier()+
						"\nOrigin: "+j.getJobOrigin()+
						"\nDestination: "+j.getJobDestination()+
						"\nPrice: "+j.getJobPrice()+
						"\nStatus: "+j.getJobState());
			}
			System.out.println("---------------------------------");
			
			for(int i=0;i<10;i++){
				System.out.println("AFTER CHECKING STATUS.");
				trip=client.jobStatus(trip.getJobIdentifier());
				Thread.sleep(5000);
				if(trip==null){
					break;
				}
				System.out.println(trip.getJobIdentifier()+" : "+trip.getJobState());
			}
			System.out.println("JOB REQUEST: LEIRIA->SANTAREM, PRICE:100");
			trip= client.requestJob("Leiria", "Santarem", 100);
			System.out.println("JOB PROPOSAL:\nCompany Name: "+trip.getCompanyName()+
											"\nTransPortID: "+trip.getJobIdentifier()+
											"\nOrigin: "+trip.getJobOrigin()+
											"\nDestination: "+trip.getJobDestination()+
											"\nPrice: "+trip.getJobPrice()+
											"\nStatus: "+trip.getJobState()+"\n");
			System.out.println("LIST OF JOBS");
			//System.out.println(client.listJobs()+"\n LENGTH:"+client.listJobs().size());
			for(JobView j: client.listJobs()){
				System.out.println("JOB:\nCompany Name: "+j.getCompanyName()+
						"\nTransPortID: "+j.getJobIdentifier()+
						"\nOrigin: "+j.getJobOrigin()+
						"\nDestination: "+j.getJobDestination()+
						"\nPrice: "+j.getJobPrice()+
						"\nStatus: "+j.getJobState());
			}
			System.out.println("---------------------------------");
			System.out.println("\n\nAFTER REFUSAL");
			trip=client.decideJob(trip.getJobIdentifier(), false);/*
			System.out.println("REFUSED JOB:\nCompany Name: "+trip.getCompanyName()+
					"\nTransPortID: "+trip.getJobIdentifier()+
					"\nOrigin: "+trip.getJobOrigin()+
					"\nDestination: "+trip.getJobDestination()+
					"\nPrice: "+trip.getJobPrice()+
					"\nStatus: "+trip.getJobState());
					*/
			System.out.println("LIST OF JOBS");
			//System.out.println(client.listJobs()+"\n LENGTH:"+client.listJobs().size());
			//LISTING AFTER REFUSAL
			for(JobView j: client.listJobs()){
				System.out.println("JOB:\nCompany Name: "+j.getCompanyName()+
						"\nTransPortID: "+j.getJobIdentifier()+
						"\nOrigin: "+j.getJobOrigin()+
						"\nDestination: "+j.getJobDestination()+
						"\nPrice: "+j.getJobPrice()+
						"\nStatus: "+j.getJobState());
			}
			System.out.println("---------------------------------");
			client.clearJobs();
		} catch (Exception pfe) {
			System.out.println("Caught: " + pfe);
		}
	}
}
