package org.jboss.sapconnector;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class SapServiceProcessor {

    public static final String CUST_ENTRY_URL_CALL ="http://localhost:8080/customer/department";
    
    public static final String CUST_MOVE_URL_CALL ="http://localhost:8080/customer/movement";

    public static final String CUST_CLASS_URL_CALL ="http://localhost:8080/customer/classification";

	public static void main(String[] args) throws IOException,
			TransformerException {
		 
		//Document doc = constructCustomerMove();
		//processXml(doc,"http://localhost:8080/customer/movement");
		//  Document doc = constructCustomerClass();
		 // processXml(doc,"http://localhost:8080/customer/classification");

	}

	public static Document constructCustomerEntry(String custId, Integer depId, long timestamp) {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		Document doc = null;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			doc = icBuilder.newDocument();
			Element mainRootElement = doc
					.createElement("CustomerDepartmentEvent");
			Node cid = null;
			Node deptId = null;
			Node ts = null;
			cid = doc.createElement("CustomerID");
			deptId = doc.createElement("DepartmentID");
			ts = doc.createElement("TimeStamp");
			cid.setTextContent(custId);
			deptId.setTextContent(depId.toString());
			ts.setTextContent(String.valueOf(timestamp));
			// mainRootElement.appendChild(cid).setNodeValue("8be0cca0-3841-4183-9cbf-b8507294a06f");
			mainRootElement.appendChild(cid);
			// mainRootElement.appendChild(deptId).setNodeValue("0");
			mainRootElement.appendChild(deptId);
			// mainRootElement.appendChild(ts).setNodeValue("1444150090");
			mainRootElement.appendChild(ts);

			doc.appendChild(mainRootElement);
			 

			// output DOM XML to console

		/*	Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult console = new StreamResult(System.out);
			transformer.transform(source, console);

			System.out.println("\nXML DOM Created Successfully..");*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

	public static Document constructCustomerMove(String custId, Point pt, long timestamp) {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		Document doc = null;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			doc = icBuilder.newDocument();
			Element mainRootElement = doc
					.createElement("CustomerMovementEvent");
			Node cid = null;
			Node location = null;
			Node ts = null;
			Node xLoc = null;
			Node yLoc = null;
			cid = doc.createElement("CustomerID");
			location = doc.createElement("Location");
			xLoc = doc.createElement("X");
			xLoc.setTextContent(String.valueOf(pt.x));
			yLoc = doc.createElement("Y");
			yLoc.setTextContent(String.valueOf(pt.y));
			location.appendChild(xLoc);
			location.appendChild(yLoc);
			ts = doc.createElement("TimeStamp");
			cid.setTextContent(custId);
			 
			ts.setTextContent(String.valueOf(timestamp));
			// mainRootElement.appendChild(cid).setNodeValue("8be0cca0-3841-4183-9cbf-b8507294a06f");
			mainRootElement.appendChild(cid);
			// mainRootElement.appendChild(deptId).setNodeValue("0");
			mainRootElement.appendChild(location);
			// mainRootElement.appendChild(ts).setNodeValue("1444150090");
			mainRootElement.appendChild(ts);

			doc.appendChild(mainRootElement);
			 

		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public static Document constructCustomerClass(String custId, Integer custClass, long timestamp) {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		Document doc = null;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			doc = icBuilder.newDocument();
			Element mainRootElement = doc
					.createElement("CustomerClassificationEvent");
			Node cid = null;
			Node ctypeId = null;
			Node ts = null;
			 
			cid = doc.createElement("CustomerID");
			ctypeId = doc.createElement("CustomerTypeID");
			 
			
			ts = doc.createElement("TimeStamp");
			cid.setTextContent(custId);
			ctypeId.setTextContent(custClass.toString()); 
			ts.setTextContent(String.valueOf(timestamp));
			// mainRootElement.appendChild(cid).setNodeValue("8be0cca0-3841-4183-9cbf-b8507294a06f");
			mainRootElement.appendChild(cid);
			// mainRootElement.appendChild(deptId).setNodeValue("0");
			mainRootElement.appendChild(ctypeId);
			// mainRootElement.appendChild(ts).setNodeValue("1444150090");
			mainRootElement.appendChild(ts);

			doc.appendChild(mainRootElement);
			 

		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public static void processXml(Document doc, String url) throws ClientProtocolException, IOException, TransformerException {
		 
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		String xmlString = writer.toString();
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost postRequest = new HttpPost(url);
		StringEntity input = new StringEntity(xmlString);
		input.setContentType("text/xml");
		postRequest.setEntity(input);
		HttpResponse response = client.execute(postRequest);
		
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer resultOutut = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			resultOutut.append(line);
		}

		System.out.println(resultOutut.toString());
	}

}
