package cl.vmetrix.process;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

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
 * This class is used to read message from queue, deleting the message from the queue and using the 
 * connection method MQOO_INPUT_EXCLUSIVE  
 * @author REcheverri
 *
 */
public class MqProcessClass {

	final static Logger logger = Logger.getLogger(MqProcessClass.class);
	private MQQueueManager _queueManager = null;
	public int port = 1416;
	public static String hostname = "";//"vmserver01";//"VmetrixRubenD"
	public String channel = "WAS.CLIENTS";//"SYSTEM.DEF.SVRCONN";
	public String qManager = "IntegracionFndCmx_QM";
	public String inputQName = "FND.TO.CMX.QUEUE";//"FndToCmxQueue";
//	public String outputQName = "LQ2.IN";

	public MqProcessClass() {
		super();
	}

	private void init(String[] args) throws IllegalArgumentException {
		// Set up MQ environment
		MQEnvironment.hostname = hostname;
		MQEnvironment.channel = channel;
		MQEnvironment.port = port;

	}

	public static void main(String[] args) {

		MqProcessClass readQ = new MqProcessClass();
		if(args.length>0)
			logger.debug("Param="+ args[0]);

		try {
			hostname=InetAddress.getLocalHost().getHostName();
			logger.debug("Conneting to  Queue....");
			readQ.init(args);
			readQ.selectQMgr();
			logger.debug("Reading Queue....");
			readQ.read();
			logger.debug("--------------------- Process Terminated. -------------------");
//			readQ.write();
		} catch (IllegalArgumentException e) {
			logger.error("Usage: java MQRead <-h host> <-p port> <-c channel> <-m QueueManagerName> <-q QueueName>",e);
			System.exit(1);
		} catch (MQException e) {
			if(e.completionCode==2 && e.getReason()==2042){
				logger.error("The queue is in use (reading the queue). The aplication will be completed. ");
				logger.debug("--------------------- Process Terminated. -------------------");
			}else
				logger.error("MQException: ",e);
			System.exit(1);
		}catch (Exception e){
			logger.error("Exception ", e);
		}
	}

	

	private void read() throws MQException {
		int openOptions = CMQC.MQOO_INPUT_EXCLUSIVE + CMQC.MQOO_INQUIRE ;//+ CMQC.MQOO_FAIL_IF_QUIESCING	+ CMQC.MQOO_INPUT_EXCLUSIVE;//CMQC.MQOO_INPUT_SHARED;

		MQQueue queue = _queueManager.accessQueue(inputQName, openOptions,
				qManager, // default q manager
				null, // no dynamic q name
				null); // no alternate user id

		logger.debug("MqProcessClass Read v1.0 connected...\n");

		int depth = queue.getCurrentDepth();
		logger.debug("---> Current Queue depth: " + depth + "\n");
		if (depth == 0) {
			return;
		}

		MQGetMessageOptions getOptions = new MQGetMessageOptions();
		getOptions.options = CMQC.MQGMO_NO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_CONVERT;
		
//		ArrayList<String> listString = new ArrayList<String>();
		boolean reading = true;
		while (reading) {
			MQMessage message = new MQMessage();
			try {
				queue.get(message, getOptions);
				byte[] b = new byte[message.getMessageLength()];
				message.readFully(b);
				stringToDom(new String(b,"ISO_8859_1"));
				message.clearMessage();
			} catch (IOException e) {
				logger.error("IOException during GET: " + e.getMessage());
				reading=false;
			} catch (MQException e) {
				if (e.completionCode == 2 && e.getReason()==2033){//e.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
					if (depth > 0) {
						logger.debug("All messages read.");
						reading=false;
					}
				} else {
					logger.error("GET Exception: ", e);
					reading=false;
				}
				
			}
		}
		queue.close();
		_queueManager.disconnect();
		
	/*	try {
			stringToDom(listString);
		} catch (IOException e) {
			logger.error("IOException in stringToDom : " ,e);
		} catch (InterruptedException e) {
			logger.error("InterruptedException in stringToDom : " ,e);
		}*/
	}

	
	public void stringToDom(String xmlSource)  throws IOException {
			ObjectFromXMLstg ofxml = new ObjectFromXMLstg();
			FileNameXml lxmlObject = new FileNameXml();
			lxmlObject=ofxml.operation(xmlSource);
			logger.debug("Starting to creat xml files..");		
			logger.debug("Creating xml file for fx number "+ lxmlObject.getName());
			java.io.FileWriter fw = new java.io.FileWriter("C:\\MqProcess\\xmlOut\\" + lxmlObject.getName()+"_"+new Date().getTime()+"_.xml");
			fw.write(lxmlObject.getContent());
			fw.close();
	}
	public void stringToDom(ArrayList<String> xmlSource)  throws IOException, InterruptedException {
		
		
		if(xmlSource.size()>0){
			ObjectFromXMLstg ofxml = new ObjectFromXMLstg();
			ArrayList<FileNameXml> listObjects = new ArrayList<FileNameXml>();
			listObjects=ofxml.operation(xmlSource);
			logger.debug("Starting to creat xml files..");		
			for(FileNameXml fn: listObjects){
				Thread.sleep(20);
				logger.debug("Creating xml file for fx number "+ fn.getName());
				java.io.FileWriter fw = new java.io.FileWriter("C:\\MqProcess\\xmlOut\\" + fn.getName()+"_"+new Date().getTime()+"_.xml");
			    fw.write(fn.getContent());
			    fw.close();
			}

		}
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
