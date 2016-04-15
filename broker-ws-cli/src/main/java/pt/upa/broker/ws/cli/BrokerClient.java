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
