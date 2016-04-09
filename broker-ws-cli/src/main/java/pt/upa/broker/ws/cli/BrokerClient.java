package pt.upa.broker.ws.cli;

import pt.upa.broker.ws.BrokerPortType;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

import pt.upa.broker.ws.TransportView;

import java.util.List;

public class BrokerClient {

	private BrokerPortType _bp;

	public BrokerClient(BrokerPortType broker){
		_bp=broker;
	}

	public String ping(String name){
		return _bp.ping(name);
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
