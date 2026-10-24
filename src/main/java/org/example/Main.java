import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
public class Main {
    private static final String XML_FILE = "cinema.xml";
    private static final String XSD_FILE = "cinema.xsd";
    public static void main(String[] args) throws Exception {
        System.out.println("===== 1. VALIDATION XML / XSD =====");
        boolean valide = validerXML(XML_FILE, XSD_FILE);
        if (!valide) {
            System.out.println("Document invalide : interrogation XPath annulee.");
            return;
        }
        System.out.println("\n===== 2. INTERROGATION XPATH =====");
        Document doc = chargerDocument(XML_FILE);
        XPath xpath = XPathFactory.newInstance().newXPath();

        afficher(xpath, doc, "Titres de tous les films", "/cinema/films/film/titre");
        afficher(xpath, doc, "Films actuellement en salle", "/cinema/films/film[@enSalle='true']/titre");
        afficher(xpath, doc, "Films de science-fiction", "//film[@genre='science-fiction']/titre");
        afficher(xpath, doc, "Films avec une note superieure a 8.5", "//film[note > 8.5]/titre");
        afficher(xpath, doc, "Noms des utilisateurs actifs", "//utilisateur[@actif='true']/nom");

        evaluerNombre(xpath, doc, "Nombre total de films", "count(//film)");
        evaluerNombre(xpath, doc, "Total des places reservees", "sum(//reservation/nbPlaces)");
        evaluerNombre(xpath, doc, "Chiffre d'affaires total", "sum(//reservation/prix)");

        afficher(xpath, doc, "Films reserves par l'utilisateur U1",
                "//film[@id = //reservation[@utilisateurRef='U1']/@filmRef]/titre");
    }
    public static boolean validerXML(String xmlResource, String xsdResource) {
        try (InputStream xml = ressource(xmlResource);
             InputStream xsd = ressource(xsdResource)) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
             Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            System.out.println("Le document '" + xmlResource + "' est VALIDE par rapport a '" + xsdResource + "'.");
            return true;
        } catch (SAXException e) {
            System.out.println("Document INVALIDE : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            return false;
        }
    }
    private static Document chargerDocument(String xmlResource) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        try (InputStream in = ressource(xmlResource)) {
            return builder.parse(in);
        }
    }

    private static void afficher(XPath xpath, Document doc, String libelle, String requete) throws Exception {
        XPathExpression expr = xpath.compile(requete);
        NodeList noeuds = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        System.out.println("\n" + libelle + "  [" + requete + "]");
        if (noeuds.getLength() == 0) {
            System.out.println("  (aucun resultat)");
        }
        for (int i = 0; i < noeuds.getLength(); i++) {
            Node n = noeuds.item(i);
            System.out.println("  - " + n.getTextContent().trim());
        }
    }

    private static void evaluerNombre(XPath xpath, Document doc, String libelle, String requete) throws Exception {
        XPathExpression expr = xpath.compile(requete);
        Double valeur = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
        System.out.println("\n" + libelle + "  [" + requete + "]\n  = " + valeur);
    }

    private static InputStream ressource(String nom) {
        InputStream in = Main.class.getClassLoader().getResourceAsStream(nom);
        if (in == null) throw new IllegalArgumentException("Ressource introuvable : " + nom);
        return in;
    }
}