package pt.upa.broker.ws;

import java.util.List;
import java.util.Arrays;
import javax.jws.WebService;

@WebService(
    endpointInterface="pt.upa.broker.ws.BrokerPortType",
    wsdlLocation="broker.1_0.wsdl",
    name="BrokerWebService",
    portName="BrokerPort",
    targetNamespace="http://ws.broker.upa.pt/",
    serviceName="BrokerService"
)
public class BrokerPort implements BrokerPortType{
	//private List<String> _NorthRegion;
	//private List<String> _CenterRegion;
	//private List<String> _SouthRegion;

	//private List<TransportView> _transportList= new List<TransportView>();
	
	//_NorthRegion=Arrays.asList("Porto", "Braga", "Viana do Castelo", "Vila Real", "Braganca");
	//_CenterRegion=Arrays.asList("Lisboa", "Leiria", "Santarem", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda");
	//_SouthRegion=Arrays.asList("Setúbal","Évora", "Portalegre", "Beja", "Faro");

	public String ping(String name){
		return "Connected: "+ name;
	}

	public String requestTransport(String origin, String destination, int price) throws UnknownLocationFault_Exception,
	InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception
	{	/*boolean isNorthern=_NorthRegion.contains(origin);
		boolean isSouthern=_CenterRegion.contains(origin);
		boolean isCentral=_SouthRegion.contains(origin);

		if(!isNorthern||!isCentral||!isSouthern){
			throw UnknownLocationFault_Exception(origin);
		}

		boolean isNorthern=_NorthRegion.contains(destination);
		boolean isSouthern=_CenterRegion.contains(destination);
		boolean isCentral=_SouthRegion.contains(destination);

		if(!isNorthern||!isCentral||!isSouthern){
			throw UnknownLocationFault_Exception(destination);
		}

		if(price <0){
			throw new InvalidPriceFault_Exception(price);
		}
		*/
		//HOW TO CHECK THE LIST?
		return new String("requestTransport invocation accepted");
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception
	{
		return null;
	}

	public List<TransportView> listTransports(){
		return null;
	}

	public void clearTransports(){
		return;
	}
}
