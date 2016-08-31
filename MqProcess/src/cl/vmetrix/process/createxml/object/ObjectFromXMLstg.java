/**
 * 
 */
package cl.vmetrix.process.createxml.object;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import cl.vmetrix.xml.model.FileNameXml;
import cl.vmetrix.xml.model.OperFx;

/**
 * This class is used to create objects from Strings (XML).
 * Receives an arrayList of XMLs in String format and returns an arrayList of
 * objects type FileNameXml
 * @author REcheverri
 *
 */
public class ObjectFromXMLstg {
	final static Logger logger = Logger.getLogger(ObjectFromXMLstg.class);
	private final static String REPLACE_HEADER = "<x:root xmlns:x=\"urn:RegistroFindur\">";//"x:root"; 
	private final static String REPLACE_STG = "</x:root>";
	
	private final static String REPLACE_HEADER_END = "<OperFx xmlns:ns2=\"OperFx\">";//"x:root"; 
	private final static String REPLACE_STG_END = "</OperFx>";

	public ArrayList<FileNameXml> operation(ArrayList<String> data){
		logger.debug("Begins process of create objects from XML string....");
		ArrayList<FileNameXml> rpta = new ArrayList<FileNameXml>();
		for(String g : data){
			rpta.add(operation(g));
		}
		logger.debug("End process of Creating objects from XML string.");
		return rpta;
			
	}
	
	public FileNameXml operation(String g){
		FileNameXml fn = new FileNameXml();
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(OperFx.class);
		
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		g=g.replaceAll(REPLACE_HEADER, "<OperFx xmlns:ns2=\"OperFx\">").replaceAll(REPLACE_STG, "</OperFx>");
		StreamSource streamSource = new StreamSource(new StringReader(g));
		JAXBElement<OperFx> je = unmarshaller.unmarshal(streamSource,OperFx.class);
		OperFx operacion = (OperFx) je.getValue();
		fn.setName(operacion.getCampos().getOpfNumeroOpFindur());
		fn.setContent(g.replaceAll(REPLACE_HEADER_END, REPLACE_HEADER).replaceAll(REPLACE_STG_END, REPLACE_STG));
			
		} catch (JAXBException e) {
			logger.error("JAXBException in ObjectFromXMLstg.operation", e);
			logger.error("Excepton unmarshaller the xml: "+ g);
			fn.setName(1010101010);
			fn.setContent(g.replaceAll(REPLACE_HEADER_END, REPLACE_HEADER).replaceAll(REPLACE_STG_END, REPLACE_STG));
		}
		
		return fn;
		
	}
	
	public OperFx operationObj(String g) throws JAXBException{
		JAXBContext jc;
		OperFx operacion = null;
		try {
			jc = JAXBContext.newInstance(OperFx.class);
		
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		g=g.replaceAll(REPLACE_HEADER, "<OperFx xmlns:ns2=\"OperFx\">").replaceAll(REPLACE_STG, "</OperFx>");
		StreamSource streamSource = new StreamSource(new StringReader(g));
		JAXBElement<OperFx> je = unmarshaller.unmarshal(streamSource,OperFx.class);
		operacion = (OperFx) je.getValue();
		
		} catch (JAXBException e) {
			logger.error("JAXBException in ObjectFromXMLstg.operationObj", e);
			logger.error("Excepton unmarshaller the xml: "+ g);
			throw new JAXBException(e);
		}
		
		return operacion;
		
	}
	
	
	
	
	
}
