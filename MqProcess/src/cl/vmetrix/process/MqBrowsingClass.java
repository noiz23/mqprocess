package cl.vmetrix.process;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import cl.vmetrix.process.createxml.object.ObjectFromXMLstg;
import cl.vmetrix.xml.model.FileNameXml;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
/**
 * This class is used to read messages from Queue using the conecction method Browsing (Read without erase message from queue)
 * @author REcheverri
 *
 */
public class MqBrowsingClass {


	final static Logger logger = Logger.getLogger(MqProcessClass.class);
	private MQQueueManager _queueManager = null;
	public int port = 1416;
	public static String hostname = "";//"VmetrixRubenD";//"vmserver01";
	public String channel = "WAS.CLIENTS";//"SYSTEM.DEF.SVRCONN";
	public String qManager = "IntegracionFndCmx_QM";
	public static String inputQName = "";//"FND.TO.CMX.QUEUE";
	private static ArrayList<FileNameXml> objects = new ArrayList<FileNameXml>();
//	public String outputQName = "LQ2.IN";

	public MqBrowsingClass() {
		super();
	}

	private void init() throws IllegalArgumentException {
		// Set up MQ environment
		MQEnvironment.hostname = hostname;
		MQEnvironment.channel = channel;
		MQEnvironment.port = port;

	}

//	public static void main(String[] args) {
	public ArrayList<FileNameXml> browseQueue(String queue){
		inputQName = queue;
		
		MqBrowsingClass readQ = new MqBrowsingClass();
		

		try {
			hostname = InetAddress.getLocalHost().getHostName();
			logger.debug("Conneting to  Queue in hostname iquals to : "+hostname+" ....");
			readQ.init();
			readQ.selectQMgr();
			logger.debug("Reading Queue....");
			readQ.read();
//			readQ.write();
		} catch (IllegalArgumentException e) {
			logger.error("Usage: java MQRead <-h host> <-p port> <-c channel> <-m QueueManagerName> <-q QueueName>");
			System.exit(1);
		} catch (MQException e) {
			logger.error(e);
			System.exit(1);
		} catch (UnknownHostException e) {
			logger.error("The name of the server is undefined.");
			logger.error(e);
			System.exit(1);
		}
		return objects;
	}

	
	private void read() throws MQException {
		int openOptions = CMQC.MQOO_BROWSE + CMQC.MQOO_FAIL_IF_QUIESCING;//	+ CMQC.MQOO_INPUT_SHARED;

		MQQueue queue = _queueManager.accessQueue(inputQName, openOptions,
				qManager, // default q manager
				null, // no dynamic q name
				null); // no alternate user id

		logger.debug("MqBrowsingClass Read v1.0 connected...\n");

		/*int depth = queue.getCurrentDepth();
		logger.debug("---> Current Queue depth: " + depth + "\n");
		if (depth == 0) {
			return;
		}*/

		MQGetMessageOptions getOptions = new MQGetMessageOptions();
		getOptions.options = CMQC.MQGMO_NO_WAIT  | CMQC.MQGMO_BROWSE_FIRST;
		getOptions.matchOptions=CMQC.MQMO_NONE;
//		getOptions.waitInterval=5000;
		
		ArrayList<String> listString = new ArrayList<String>();
		while (true) {
			MQMessage message = new MQMessage();
			try {
				queue.get(message, getOptions);
				byte[] b = new byte[message.getMessageLength()];
				message.readFully(b);
				listString.add(new String(b));
//				logger.debug(new String(b));
				message.clearMessage();
				
				getOptions.options = CMQC.MQGMO_WAIT | CMQC.MQGMO_BROWSE_NEXT;
				
			} catch (IOException e) {
				logger.error("IOException during GET: " + e.getMessage());
				break;
			} catch (MQException e) {
				
				if (e.completionCode == 2 && e.getReason()==2033){//if(e.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
		            System.out.println("no more message available or retrived");
		        }
				break;
			}
		}
		
		
		
		
		queue.close();
		_queueManager.disconnect();
		
		try {
			objects = stringToDom(listString);
			logger.debug("Number of message in Queue = " + objects.size());
		} catch (Exception e) {
			logger.error("IOException in stringToDom : " ,e);
		}
	}

/*	public void stringToDom(ArrayList<String> xmlSource)  throws IOException {
		Date date = new Date();
		date.getTime();
		int count =0;
		if(xmlSource.size()>0){
			for(String stFile: xmlSource){
				java.io.FileWriter fw = new java.io.FileWriter("C:\\MqProcess\\" + ++count+"_xmlFile_"+date.getTime()+"_.xml");
			    fw.write(stFile);
			    fw.close();
			}
		
	    
		}
	}*/
	
	public ArrayList<FileNameXml> stringToDom(ArrayList<String> xmlSource)  throws Exception {
		ArrayList<FileNameXml> listObjects = new ArrayList<FileNameXml>();
		
		if(xmlSource.size()>0){
			ObjectFromXMLstg ofxml = new ObjectFromXMLstg();
			listObjects=ofxml.operation(xmlSource);
		}
		
		return listObjects;
	}
	
	private void selectQMgr() throws MQException {
		_queueManager = new MQQueueManager(qManager);
	}

	/*private void write() throws MQException {
		int lineNum = 0;
		int openOptions = MQC.MQOO_OUTPUT + MQC.MQOO_FAIL_IF_QUIESCING;
		try {
			MQQueue queue = _queueManager.accessQueue(outputQName, openOptions,
					null, // default q manager
					null, // no dynamic q name
					null); // no alternate user id

			DataInputStream input = new DataInputStream(System.in);

			System.out.println("MQWrite v1.0 connected");
			System.out.println("and ready for input, terminate with ^Z\n\n");

			// Define a simple MQ message, and write some text in UTF format..
			MQMessage sendmsg = new MQMessage();
			sendmsg.format = MQC.MQFMT_STRING;
			sendmsg.feedback = MQC.MQFB_NONE;
			sendmsg.messageType = MQC.MQMT_DATAGRAM;
			sendmsg.replyToQueueName = "ROGER.QUEUE";
			sendmsg.replyToQueueManagerName = qManager;

			MQPutMessageOptions pmo = new MQPutMessageOptions(); // accept the
																	// defaults,
																	// same
			// as MQPMO_DEFAULT constant

			String line = "test message";
			sendmsg.clearMessage();
			sendmsg.messageId = MQC.MQMI_NONE;
			sendmsg.correlationId = MQC.MQCI_NONE;
			sendmsg.writeString(line);

			// put the message on the queue

			queue.put(sendmsg, pmo);
			System.out.println(++lineNum + ": " + line);

			queue.close();
			_queueManager.disconnect();

		} catch (com.ibm.mq.MQException mqex) {
			System.out.println(mqex);
		} catch (java.io.IOException ioex) {
			System.out.println("An MQ IO error occurred : " + ioex);
		}

	}*/

}
