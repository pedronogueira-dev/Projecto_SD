package pt.upa.transporter;



import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.Endpoint;

import pt.upa.transporter.ws.TransporterEndpointManager;
import pt.upa.transporter.ws.TransporterPort;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
public class TransporterApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(TransporterApplication.class.getSimpleName() + " starting...");

		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n",	TransporterApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];

		TransporterEndpointManager endpoint =null;
		//System.out.println("UDDI URL: "+uddiURL +"\nWS NAME: "+name+"\nWS URL: "+url);
		endpoint= new TransporterEndpointManager(uddiURL,name,url);
		endpoint.setVerbose(true);
		//Endpoint endpoint = null;

			try{
				endpoint.start();
				endpoint.awaitConnections();
			}finally{
				endpoint.stop();
				}
	}

}
