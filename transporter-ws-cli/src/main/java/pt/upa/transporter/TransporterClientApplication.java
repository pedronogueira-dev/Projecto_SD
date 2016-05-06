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

		if(args.length == 0){
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java "+TransporterClientApplication.class.getName() + "[[uddiURL] wsURL]");
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		
		if(args.length == 1){
			wsURL = args[0];
		}else if(args.length >= 2){
			uddiURL = args[0];
			wsName = args[1];
		}
		
		//Transporter creation
		TransporterClient client = null;
		
		if(wsURL != null){
			System.out.printf("Creating client for server at %s%n", wsURL);
			client = new TransporterClient(wsURL);
		}else if(uddiURL != null){
			System.out.printf("Creating client using UDDI at %s for server with name %s%n", uddiURL, wsName);
			client = new TransporterClient(uddiURL, wsName);
		}
		System.out.println("PING TO SERVER::");
		System.out.println(client.ping("[CLIENT ONE]"));
//		
//		 //Attempting to generate a new job:
//		client.clearJobs();
//		
//		System.out.println("------LISTING:");
//		for(JobView j: client.listJobs()){
//			System.out.println("\tJOB:\nCompany Name: "+j.getCompanyName()+
//					"\n\tTransPortID: "+j.getJobIdentifier()+
//					"\n\tOrigin: "+j.getJobOrigin()+
//					"\n\tDestination: "+j.getJobDestination()+
//					"\n\tPrice: "+j.getJobPrice()+
//					"\n\tStatus: "+j.getJobState());
//		}
//		System.out.println("--------------");
//		//First Request
//		JobView trip= client.requestJob("Faro", "Santarem", 10);
//		//System.out.println(trip);
//		//List after creating a jobView
//		System.out.println("AFTER REQUESTING A JOB FROM PORTO TO SANTAREM");
//		for(JobView j: client.listJobs()){
//			System.out.println("\tJOB:\nCompany Name: "+j.getCompanyName()+
//					"\n\tTransPortID: "+j.getJobIdentifier()+
//					"\n\tOrigin: "+j.getJobOrigin()+
//					"\n\tDestination: "+j.getJobDestination()+
//					"\n\tPrice: "+j.getJobPrice()+
//					"\n\tStatus: "+j.getJobState());
//		}
//		System.out.println("--------------");
//		trip=client.decideJob(trip.getJobIdentifier(), true);
//		System.out.println("JOB: "+trip.getJobIdentifier() + "NEW STATUS: "+trip.getJobState());
//		for(int i=0;i<7;i++){
//			System.out.println("AFTER CHECKING STATUS.");
//			trip=client.jobStatus(trip.getJobIdentifier());
//			System.out.println(trip.getJobState());
//			Thread.sleep(2000);
//		}
	}
}
