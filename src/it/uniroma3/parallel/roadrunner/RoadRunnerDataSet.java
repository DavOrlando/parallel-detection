package it.uniroma3.parallel.roadrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe che rappresenta il dataset di RoadRunner, ovvero il risultato di una
 * sua esecuzione.
 * 
 * @author davideorlando
 *
 */
public class RoadRunnerDataSet {

	private static final String ATTRIBUTE_LABEL = "//attribute/@label";
	private static final Logger logger = Logger.getLogger(RoadRunnerDataSet.class);
	private NodeList labelNodes;
	private XPath xPath;
	private Document xmlDocument;

	/**
	 * Crea l'oggetto RRDataSet che rappresenta informazioni sui dati di output
	 * di Road Runner, come le label, ovvero le porzioni di pagina allineate.
	 * 
	 * @param outputPath
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public RoadRunnerDataSet(String outputPath)
			throws ParserConfigurationException, SAXException, XPathExpressionException {
		// apro file e creo struttura per fare xpath query
		FileInputStream file;
		try {
			file = new FileInputStream(new File(outputPath));
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			this.xmlDocument = builder.parse(file);
			this.xPath = XPathFactory.newInstance().newXPath();
			// query per sapere le label
			this.labelNodes = (NodeList) xPath.compile(ATTRIBUTE_LABEL).evaluate(xmlDocument, XPathConstants.NODESET);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public NodeList getLabelNodes() {
		return labelNodes;
	}

	/**
	 * Ritorna il numero di label del dataset.
	 * 
	 * @return
	 */
	public int getNumberOfLabels() {
		return this.labelNodes.getLength();
	}

	/**
	 * Ritorna la lista di stringhe che corrispondono agli URI in input a Road
	 * Runner.
	 * 
	 * @return
	 * @throws XPathExpressionException
	 */
	public List<String> getURIInputFileForRR() throws XPathExpressionException {
		List<String> filePathString = new ArrayList<>();
		NodeList localFilenameNodes = this.getLocalFilename();
		for (int j = 0; j < localFilenameNodes.getLength(); j++) {
			filePathString.add(localFilenameNodes.item(j).getFirstChild().getNodeValue().toString());
		}
		return filePathString;
	}

	/**
	 * Ritorna i path dei file che ho avuto in input a Road Runner.
	 * 
	 * @return
	 * @throws XPathExpressionException
	 */
	private NodeList getLocalFilename() throws XPathExpressionException {
		String getLocalFilenameXpath = "//instance/@source";
		NodeList localFilenameNodes = (NodeList) xPath.compile(getLocalFilenameXpath).evaluate(xmlDocument,
				XPathConstants.NODESET);
		return localFilenameNodes;
	}

	/**
	 * Ritorna una lista di testi. Ogni testo è la concatenazione delle stringhe
	 * di ogni label per una lingua. Quindi con due lingue avrò due testi, uno
	 * in linguaggio1 (testo delle label in linguaggio1) e un altro in
	 * linguaggio2(testo delle label in linguaggio2).
	 * 
	 * @return
	 */

	public List<String> getTextFromAllLabels() {
		List<String> listaStringoni = new ArrayList<>();
		try {
			NodeList localFilenameNodes = this.getLocalFilename();
			for (int j = 0; j < localFilenameNodes.getLength(); j++) {
				listaStringoni.add(j, "");
			}
			// prendo il testo da tutte le label
			for (String label : getStringOfLabel()) {
				// query per sapere testo delle label
				String getExtractedValuesXpath = "//attribute[@label='" + label + "']//inputsamples";
				NodeList extractedValueNodes = (NodeList) xPath.compile(getExtractedValuesXpath).evaluate(xmlDocument,
						XPathConstants.NODESET);
				// per ogni risultato avuto dalla query appena sopra (dove
				// chiedo testo per quella label)
				// j rappresenta i vari source, quante label ho con stesso nome
				// quindi quanti documenti ho allineato
				for (int j = 0; j < extractedValueNodes.getLength(); j++) {
					// se per doc j-esimo ho un risultato non null allora setta
					// temp con result della query altrimenti setta temp=" "
					String temp = " ";
					if (extractedValueNodes.item(j).getFirstChild().getNodeValue() != null)
						temp = extractedValueNodes.item(j).getFirstChild().getNodeValue();
					// metodo remove rimuove elemento e lo restituisce
					listaStringoni.add(j, listaStringoni.remove(j).concat(temp) + " ");
				}
			}
		} catch (XPathExpressionException e) {
			logger.error(e);
		}
		return listaStringoni;
	}

	/**
	 * Ritorna una lista di stringhe ognuna rappresentante una label. Ovvero
	 * lettere come _A_, _B_. Formato delle label di Road Runner.
	 * 
	 * @return
	 */
	private List<String> getStringOfLabel() {
		List<String> roadRunnerStringLabel = new ArrayList<>();
		// itero sulle label per sapere il nome sottoforma di stringa
		for (int i = 0; i < this.labelNodes.getLength(); i++) {
			String roadRunnerLabel = labelNodes.item(i).getFirstChild().getNodeValue();
			roadRunnerStringLabel.add(roadRunnerLabel);
		}
		return roadRunnerStringLabel;
	}

}
