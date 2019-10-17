package esa.s1pdgs.cpoc.wrapper;

import java.io.File;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;

public class Demo {
	public static void main(String[] args) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = dbf.newDocumentBuilder().parse(new File("/home/florian_sievert/oqc/amalfi-jo.xml"));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile("//File_Name/text()");

        Node b13Node = (Node) expression.evaluate(document, XPathConstants.NODE);
        b13Node.setTextContent("xxx");
//        b13Node.getParentNode().removeChild(b13Node);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.transform(new DOMSource(document), new StreamResult(System.out));
    }
}
