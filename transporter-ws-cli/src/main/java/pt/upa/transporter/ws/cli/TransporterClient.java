package pt.upa.transporter.ws.cli;


import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.JobView;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

public class TransporterClient {

	private TransporterPortType _tc;
	
	public TransporterClient(String uddiURL, String name) throws JAXRException{
		//System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		//System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		//System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		TransporterPortType port = service.getTransporterPort();

		//System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		_tc=port;
	}
	
	public TransporterClient(TransporterPortType tc){
			_tc=tc;
	}
	public String ping(String name){
		return _tc.ping(name);
	}


    public JobView requestJob(String origin,String destination,int price) throws BadLocationFault_Exception, BadPriceFault_Exception{
    	return _tc.requestJob(origin, destination, price);
    }

    public JobView decideJob(String id,boolean accept) throws BadJobFault_Exception{
    	return _tc.decideJob(id, accept);
    }

   	public JobView jobStatus(String id){
   		return _tc.jobStatus(id);
   	}

    public List<JobView> listJobs(){
    	return _tc.listJobs();
    }

    public void clearJobs(){
    	_tc.clearJobs();
    }

}
