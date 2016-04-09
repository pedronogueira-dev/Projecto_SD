package pt.upa.transporter.ws;

import java.util.List;
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
