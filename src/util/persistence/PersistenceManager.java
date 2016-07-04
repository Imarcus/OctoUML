package util.persistence;

import jdk.nashorn.internal.ir.ObjectNode;
import model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcus on 2016-03-03.
 */
public class PersistenceManager {

    public static void saveFile(Graph pGraph, String path)
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        XMLEncoder encoder = new XMLEncoder(bos);
        encoder.writeObject(pGraph);
        /*for(AbstractNode node : pGraph.getAllNodes()){
            encoder.writeObject(node);
        }
        for(Edge edge : pGraph.getAllEdges()){
            encoder.writeObject(edge);
        }*/

        encoder.close();
    }

    public static void exportXMI(Graph pGraph, String path){
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("XMI");
            rootElement.setAttribute("xmi.version", "1.1");
            rootElement.setAttribute("xmlns:UML", "org.omg/UML/1.3");
            doc.appendChild(rootElement);

            Element xmiHeader = doc.createElement("XMI.header");
             rootElement.appendChild(xmiHeader);
            Element xmiDocumentation = doc.createElement("XMI.documenation");
            xmiHeader.appendChild(xmiDocumentation);

            xmiDocumentation.appendChild(doc.createElement("XMI.owner"));
            xmiDocumentation.appendChild(doc.createElement("XMI.contact"));
            Element xmiExporter = doc.createElement("XMI.exporter");
            xmiExporter.appendChild(doc.createTextNode("PenguinUML"));
            xmiDocumentation.appendChild(xmiExporter);
            Element xmiExporterVersion = doc.createElement("XMI.exporterVersion");
            xmiExporterVersion.appendChild(doc.createTextNode("1.0"));
            xmiDocumentation.appendChild(xmiExporterVersion);
            xmiDocumentation.appendChild(doc.createElement("XMI.notice"));

            Element xmiMetaModel = doc.createElement("XMI.metamodel");
            xmiMetaModel.setAttribute("xmi.version", "1.3");
            xmiMetaModel.setAttribute("xmi.name", "UML");
            xmiHeader.appendChild(xmiMetaModel);

            Element xmiContent = doc.createElement("XMI.content");
            rootElement.appendChild(xmiContent);

            Element umlModel = doc.createElement("UML:model");
            xmiContent.appendChild(umlModel);
            umlModel.setAttribute("isAbstract", "false");
            umlModel.setAttribute("isLeaf", "false");
            umlModel.setAttribute("isRoot", "false");
            umlModel.setAttribute("namespace", "UMLModel.2");
            umlModel.setAttribute("isSpecification", "false");
            umlModel.setAttribute("visibility", "public");
            umlModel.setAttribute("name", "Design Model");
            umlModel.setAttribute("xmi.id", "UMLModel.3");
            Element umlNamespace = doc.createElement("UML:Namespace.ownedElement");
            umlModel.appendChild(umlNamespace);

            Element umlDiagram = doc.createElement("UML:Diagram");
            xmiContent.appendChild(umlDiagram);
            umlDiagram.setAttribute("name", "UMLExport");
            umlDiagram.setAttribute("xmi.id", "UMLDIAGRAMID");
            umlDiagram.setAttribute("owner", "UMLModel.3"); //xmi.id in umlModel
            umlDiagram.setAttribute("toolName", "PenguinUML");
            umlDiagram.setAttribute("diagramType", "ClassDiagram");
            Element umlDiagramElement = doc.createElement("UML:Diagram.element");
            umlDiagram.appendChild(umlDiagramElement);


            Element umlClass;
            Element umlElement;
            for(AbstractNode node : pGraph.getAllNodes()){
                if(node instanceof ClassNode){
                    umlClass = doc.createElement("UML:Class");
                    umlClass.setAttribute("namespace", pGraph.getId());
                    umlClass.setAttribute("name", node.getTitle());
                    umlClass.setAttribute("xmi.id", node.getId());
                    Element classifierFeature = doc.createElement("UML:Classifier.feature");
                    umlClass.appendChild(classifierFeature);

                    int attIdCount = 0;
                    int opIdCount = 0;
                    if(((ClassNode) node).getAttributes() != null){
                        String atttributes[] = ((ClassNode) node).getAttributes().split("\\r?\\n");
                        for(String att : atttributes){
                            Element attribute = doc.createElement("UML:Attribute");
                            attribute.setAttribute("name", att);
                            attribute.setAttribute("xmi.id", "att" + ++attIdCount + "_" + node.getId());
                            classifierFeature.appendChild(attribute);
                        }
                    }
                    if(((ClassNode) node).getOperations() != null){
                        String operations[] = ((ClassNode) node).getOperations().split("\\r?\\n");
                        for(String op : operations) {
                            Element operation = doc.createElement("UML:Operation");
                            operation.setAttribute("name", op);
                            operation.setAttribute("xmi.id", "oper" + ++opIdCount + "_" + node.getId());
                            classifierFeature.appendChild(operation);
                        }
                    }
                    umlNamespace.appendChild(umlClass);
                } else if (node instanceof PackageNode){
                    Element umlPackage = doc.createElement("UML:Package");
                    umlPackage.setAttribute("isAbstract", "false");
                    umlPackage.setAttribute("isLeaf", "false");
                    umlPackage.setAttribute("isRoot", "false");
                    umlPackage.setAttribute("name", node.getTitle());
                    umlPackage.setAttribute("xmi.id", node.getId());

                }

                umlElement = doc.createElement("UML:DiagramElement");
                umlElement.setAttribute("xmi.id", "CLASSVIEWID");
                umlElement.setAttribute("subject", node.getId());
                umlElement.setAttribute("geometry", node.getTranslateX() + "," + node.getTranslateY() + "," +
                        node.getTranslateX()+node.getWidth() + "," + node.getTranslateY()+node.getHeight());
                umlElement.setAttribute("style", "LineColor.Red=128,LineColor.Green=0,LineColor.Blue=0,FillColor.Red=255,FillColor.Green=255,FillColor.Blue=185,Font.Red=0,Font.Green=0,Font.Blue=0,Font.FaceName=Tahoma,Font.Size=8,Font.Bold=0,Font.Italic=0,Font.Underline=0,Font.Strikethrough=0,AutomaticResize=0,ShowAllAttributes=1,SuppressAttributes=0,ShowAllOperations=1,SuppressOperations=0,ShowOperationSignature=1,");
                umlDiagramElement.appendChild(umlElement);
            }



            for(Edge edge : pGraph.getAllEdges()){
                Element umlAssociation = doc.createElement("UML:Association");
                umlAssociation.setAttribute("namespace", pGraph.getId());
                umlAssociation.setAttribute("name", "");  //TODO label for edges
                umlAssociation.setAttribute("xmi.id", ((AbstractEdge)edge).getId());
                Element associationConnection = doc.createElement("UML:Association.connection");
                umlAssociation.appendChild(associationConnection);

                //Start
                Element associationEnd1 = doc.createElement("UML:AssociationEnd");
                associationEnd1.setAttribute("xmi.id", "end0");
                associationEnd1.setAttribute("type", ((AbstractNode)edge.getStartNode()).getId());
                associationEnd1.setAttribute("association", "ass0");
                Element associationEnd1Mulitplicity = doc.createElement("UML:AssociationEnd.multiplicity");
                associationEnd1.appendChild(associationEnd1Mulitplicity);
                Element multiplicity1 = doc.createElement("UML:multiplicity");
                associationEnd1Mulitplicity.appendChild(multiplicity1);
                Element multiplicityRange1 = doc.createElement("UML:Multiplicity.range");
                multiplicity1.appendChild(multiplicityRange1);
                Element multiplicityRange11 = doc.createElement("UML:MultiplicityRange");
                multiplicityRange11.setAttribute("upper", ""); //TODO
                multiplicityRange11.setAttribute("lower", "");
                multiplicityRange1.appendChild(multiplicityRange11);

                //End
                Element associationEnd2 = doc.createElement("UML:AssociationEnd");
                associationEnd2.setAttribute("xmi.id", "end0");
                associationEnd2.setAttribute("type", ((AbstractNode)edge.getEndNode()).getId());
                associationEnd2.setAttribute("association", "ass0");
                Element associationEnd2Mulitplicity = doc.createElement("UML:AssociationEnd.multiplicity");
                associationEnd2.appendChild(associationEnd2Mulitplicity);
                Element multiplicity2 = doc.createElement("UML:multiplicity");
                associationEnd2Mulitplicity.appendChild(multiplicity2);
                Element multiplicityRange2 = doc.createElement("UML:Multiplicity.range");
                multiplicity2.appendChild(multiplicityRange2);
                Element multiplicityRange22 = doc.createElement("UML:MultiplicityRange");
                multiplicityRange22.setAttribute("upper", ""); //TODO
                multiplicityRange22.setAttribute("lower", "");
                multiplicityRange2.appendChild(multiplicityRange22);

                associationConnection.appendChild(associationEnd1);
                associationConnection.appendChild(associationEnd2);

                umlNamespace.appendChild(umlAssociation);


                Element umlElementAssociation = doc.createElement("UML:DiagramElement");
                umlDiagramElement.appendChild(umlElementAssociation);
                umlElementAssociation.setAttribute("xmi.id", "ID");
                umlElementAssociation.setAttribute("subject", ((AbstractNode)edge.getEndNode()).getId());
                umlElementAssociation.setAttribute("style", "Association:LineColor.Red=128,LineColor.Green=0,LineColor.Blue=0,Font.Red=0,Font.Green=0,Font.Blue=0,Font.FaceName=Tahoma,Font.Size=8,Font.Bold=0,Font.Italic=0,Font.Underline=0,Font.Strikethrough=0,");
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            transformer.transform(source, result);

            System.out.println("File saved!");


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }

    public static Graph loadFile(String path){

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedInputStream bis = new BufferedInputStream(fis);
        XMLDecoder decoder = new XMLDecoder(bis);
        Graph graph = (Graph) decoder.readObject();
        return graph;
    }
}
