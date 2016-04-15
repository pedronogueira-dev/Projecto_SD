package pt.upa.broker.ws.cli;

import pt.upa.broker.ws.BrokerPortType;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

import pt.upa.broker.ws.TransportView;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.upa.broker.ws.cli.*;
import pt.upa.broker.ws.*;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import java.util.List;
import java.util.Map;

public class BrokerClient {

	private BrokerPortType _bp;

	/*public BrokerClient(BrokerPortType broker){
		_bp=broker;

	}*/
	
	public BrokerClient(String endpointAddress) throws JAXRException{

		System.out.printf("Looking for '%s'%n", endpointAddress);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		//System.out.println("Creating stub ...");
		BrokerService service = new BrokerService();
		BrokerPortType port = service.getBrokerPort();

		//System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		
		_bp = port;
	}

	public String ping(String name){
		return _bp.ping(name);
	}

	public String requestTransport(String origin, String destination, int price) throws UnknownLocationFault_Exception,
	InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception
	{	return _bp.requestTransport(origin, destination, price);
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception
	{
		return _bp.viewTransport(id);
	}

	public List<TransportView> listTransports(){
		return _bp.listTransports();
	}

	public void clearTransports(){
		_bp.clearTransports();
		return;
	}

}
