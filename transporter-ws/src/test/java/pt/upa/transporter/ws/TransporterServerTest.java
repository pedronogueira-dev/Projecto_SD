//package pt.upa.transporter.ws;
// 
//import org.junit.*;
//import static org.junit.Assert.*;
//
//import java.util.List;
//
///**
//*  Unit Test example
//*  
//*  Invoked by Maven in the "test" life-cycle phase
//*  If necessary, should invoke "mock" remote servers 
//*/
//public class TransporterServerTest {
//
//    // static members
//	private TransporterPortType localPort;
//	private TransporterEndpointManager endpoint;
//
//    // one-time initialization and clean-up
//
////    @BeforeClass
////    public static void oneTimeSetUp() {
////
////    }
////
////    @AfterClass
////    public static void oneTimeTearDown() {
////
////    }
//
//    // members
//
//
//   // initialization and clean-up for each test
//
//    @Before
//    public void setUp() {
////    	localPort = new TransporterPort(1);
//    	endpoint= new TransporterEndpointManager("http://localhost:9090","UpaTransporter1","http://localhost:8081/transporter-ws/endpoint");
//		endpoint.setVerbose(true);
//		localPort = endpoint.getPort();
//    }
//
//    @After
//    public void tearDown() {
//    	try {
//			endpoint.stop();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//
//
//    // tests
//
//    @Test
//    public void testPing() {
//    	String pingResponse=localPort.ping("Test");
//   	String expected="Transporter: "+ "Test"+ " Connected to TransporterServer"+ 1;
//         assertEquals(expected, pingResponse);
//        // if the assert fails, the test fails
//    }
//
//    @Test
//    public void testRequestJob() throws BadLocationFault_Exception, BadPriceFault_Exception {
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//   	
//    	JobView toTest=localPort.requestJob(origin, destination, price);
//    	
//         assertEquals(origin, toTest.getJobOrigin());
//         assertEquals(destination,toTest.getJobDestination());
//         assertNotEquals(price, toTest.getJobPrice());
//         assertEquals(JobStateView.PROPOSED,toTest.getJobState());
//        // if the assert fails, the test fails
//    }
//    
//    @Test
//    public void testDecideJobACCEPTED(){
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//   	try{
//    	JobView toTest=localPort.requestJob(origin, destination, price);
//    	toTest=localPort.decideJob(toTest.getJobIdentifier(), true);
//    	assertEquals(JobStateView.ACCEPTED,toTest.getJobState());
//    	}catch(Exception e){}  
//         
//    }
//    
//   @Test
//    public void testDecideJobREJECTED(){
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//    	try{
//   	JobView toTest=localPort.requestJob(origin, destination, price);
//    	toTest=localPort.decideJob(toTest.getJobIdentifier(), false);
//    	assertEquals(JobStateView.REJECTED,toTest.getJobState());
//    	}catch(Exception e){}  
//         
//    }
//    
//    @Test
//    public void testJobStatus(){
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//  	try{
//    	JobView toTest=localPort.requestJob(origin, destination, price);
//    	toTest=localPort.decideJob(toTest.getJobIdentifier(), false);
//    	toTest=localPort.jobStatus(toTest.getJobIdentifier());
//    	assertEquals(JobStateView.REJECTED,toTest.getJobState());
//    	}catch(Exception e){} 
//    }
//   @Test
//    public void testList(){
//    	localPort.clearJobs();
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//    	try{
//        	JobView toTest=localPort.requestJob(origin, destination, price);
//        	List<JobView> list = localPort.listJobs();
//        	
//        	assertFalse(list.isEmpty());
//       	assertTrue(list.size()==1);
//        	assertEquals(list.get(1),toTest);
//        	}catch(Exception e){} 
//    }
//    
//    @Test
//    public void testClearJobs(){
//    	localPort.clearJobs();
//    	List<JobView> list = localPort.listJobs();
//    	String origin="Lisboa";
//    	String destination="Santarem";
//    	int price = 10;
//    	
//    	assertTrue(list.isEmpty());
//    	try{
//        	JobView toTest=localPort.requestJob(origin, destination, price);
//        	list = localPort.listJobs();
//        	
//        	assertFalse(list.isEmpty());
//        	assertTrue(list.size()==1);
//        	assertEquals(list.get(1),toTest);
//        	localPort.clearJobs();
//        	list=localPort.listJobs();
//        	assertTrue(list.isEmpty());
//        	}catch(Exception e){} 
//    }
//}