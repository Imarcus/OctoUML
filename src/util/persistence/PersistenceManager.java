package util.persistence;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import model.*;
import model.edges.*;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.IdentifiedTextField;
import model.nodes.PackageNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.awt.TextField;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class with static methods for importing and exporting xmi models.
 */
public class PersistenceManager {

    public static void exportXMI(Graph pGraph, String path){
        try{
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(createXmi(pGraph));

            StreamResult result = new StreamResult(new File(path));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();	
        }

    }

    public static Document createXmi(Graph pGraph){
        DocumentBuilder docBuilder = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

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

        Element umlModel = doc.createElement("UML:Model");
        xmiContent.appendChild(umlModel);
        umlModel.setAttribute("isAbstract", "false");
        umlModel.setAttribute("isLeaf", "false");
        umlModel.setAttribute("isRoot", "false");
        umlModel.setAttribute("namespace", "UMLModel.2");
        umlModel.setAttribute("isSpecification", "false");
        umlModel.setAttribute("visibility", "public");
        umlModel.setAttribute("name", pGraph.getName());
        umlModel.setAttribute("xmi.id", pGraph.getId());
        Element umlNamespace = doc.createElement("UML:Namespace.ownedElement");
        umlModel.appendChild(umlNamespace);

        Element umlDiagram = doc.createElement("UML:Diagram");
        xmiContent.appendChild(umlDiagram);
        umlDiagram.setAttribute("name", "UMLExport");
        umlDiagram.setAttribute("xmi.id", "UMLDIAGRAMID");
        umlDiagram.setAttribute("owner", pGraph.getId()); //xmi.id in umlModel
        umlDiagram.setAttribute("toolName", "PenguinUML");
        umlDiagram.setAttribute("diagramType", "ClassDiagram");
        Element umlDiagramElement = doc.createElement("UML:Diagram.element");
        umlDiagram.appendChild(umlDiagramElement);


        for(AbstractNode node : pGraph.getAllNodes()){
            if(node instanceof ClassNode && !node.isChild()){
                addClassNode(doc, (ClassNode)node, umlNamespace, pGraph, false);
            } else if (node instanceof PackageNode){
                Element umlPackage = doc.createElement("UML:Package");
                umlPackage.setAttribute("isAbstract", "false");
                umlPackage.setAttribute("isLeaf", "false");
                umlPackage.setAttribute("isRoot", "false");
                umlPackage.setAttribute("name", node.getTitle());
                umlPackage.setAttribute("xmi.id", node.getId());
                Element packageOwnedElement = doc.createElement("UML:Namespace.ownedElement");
                umlPackage.appendChild(packageOwnedElement);
                for(AbstractNode childNode : ((PackageNode)node).getChildNodes()){
                    addClassNode(doc, (ClassNode)childNode, packageOwnedElement, pGraph, true); //TODO Package nodes in package nodes
                }
                umlNamespace.appendChild(umlPackage);
            }

            Element umlElement = doc.createElement("UML:DiagramElement");
            umlElement.setAttribute("xmi.id", "NODEVIEWID");
            umlElement.setAttribute("subject", node.getId());
            umlElement.setAttribute("geometry", node.getTranslateX() + "," + node.getTranslateY() + "," +
                    (node.getTranslateX()+node.getWidth()) + "," + (node.getTranslateY()+node.getHeight()));
            umlElement.setAttribute("style", "LineColor.Red=128,LineColor.Green=0,LineColor.Blue=0,FillColor.Red=255,FillColor.Green=255,FillColor.Blue=185,Font.Red=0,Font.Green=0,Font.Blue=0,Font.FaceName=Tahoma,Font.Size=8,Font.Bold=0,Font.Italic=0,Font.Underline=0,Font.Strikethrough=0,AutomaticResize=0,ShowAllAttributes=1,SuppressAttributes=0,ShowAllOperations=1,SuppressOperations=0,ShowOperationSignature=1,");
            umlDiagramElement.appendChild(umlElement);
        }



        for(Edge edge : pGraph.getAllEdges()){
            Element umlAssociation = doc.createElement("UML:Association");
            umlAssociation.setAttribute("namespace", pGraph.getId());
            umlAssociation.setAttribute("name", "");  //TODO label for edges
            umlAssociation.setAttribute("xmi.id", edge.getId());
            umlAssociation.setAttribute("relation", edge.getType());
            umlAssociation.setAttribute("direction", ((AbstractEdge) edge).getDirection().toString());


            Element associationConnection = doc.createElement("UML:Association.connection");
            umlAssociation.appendChild(associationConnection);
            
            AbstractEdge abstractEdge = (AbstractEdge) edge;
            addAssociatonEnd(edge.getStartNode().getId(), abstractEdge.getStartMultiplicity(), associationConnection, doc, "true");
            addAssociatonEnd(edge.getEndNode().getId(), abstractEdge.getEndMultiplicity(), associationConnection, doc, "false");

            umlNamespace.appendChild(umlAssociation);


            Element umlElementAssociation = doc.createElement("UML:DiagramElement");
            umlDiagramElement.appendChild(umlElementAssociation);
            umlElementAssociation.setAttribute("xmi.id", "ID");
            umlElementAssociation.setAttribute("subject", edge.getId());
            umlElementAssociation.setAttribute("style", "Association:LineColor.Red=128,LineColor.Green=0,LineColor.Blue=0,Font.Red=0,Font.Green=0,Font.Blue=0,Font.FaceName=Tahoma,Font.Size=8,Font.Bold=0,Font.Italic=0,Font.Underline=0,Font.Strikethrough=0,");
        }

        for(Sketch sketch : pGraph.getAllSketches()){
            Element sketchElement = doc.createElement("Sketch");
            Path sketchPath = sketch.getPath();
            sketchElement.setAttribute("translateX", Double.toString(sketchPath.getTranslateX()));
            sketchElement.setAttribute("translateY", Double.toString(sketchPath.getTranslateY()));
            sketchElement.setAttribute("scaleX", Double.toString(sketchPath.getScaleX()));
            sketchElement.setAttribute("scaleY", Double.toString(sketchPath.getScaleY()));

            Element pathElement = doc.createElement("Path");
            for(PathElement el : sketchPath.getElements()){
                if(el instanceof MoveTo){
                    Element moveTo = doc.createElement("MoveTo");
                    moveTo.setAttribute("xPoint", Double.toString(((MoveTo)el).getX()));
                    moveTo.setAttribute("yPoint", Double.toString(((MoveTo)el).getY()));
                    pathElement.appendChild(moveTo);
                } else if(el instanceof LineTo){
                    Element lineTo = doc.createElement("LineTo");
                    lineTo.setAttribute("xPoint", Double.toString(((LineTo)el).getX()));
                    lineTo.setAttribute("yPoint", Double.toString(((LineTo)el).getY()));
                    pathElement.appendChild(lineTo);
                }

            }
            sketchElement.appendChild(pathElement);
            rootElement.appendChild(sketchElement);
        }

        return doc;
    }

    private static void addClassNode(Document doc, ClassNode node, Element parent, Graph pGraph, boolean isChild){
    	IdentifiedTextField textField;
        Element umlClass = doc.createElement("UML:Class");
        if(isChild){
            umlClass.setAttribute("namespace", ((Element)parent.getParentNode()).getAttribute("xmi.id"));
        } else {
            umlClass.setAttribute("namespace", pGraph.getId());
        }
        umlClass.setAttribute("name", node.getTitle());
        umlClass.setAttribute("xmi.id", node.getId());
        Element classifierFeature = doc.createElement("UML:Classifier.feature");
        umlClass.appendChild(classifierFeature);

        if(node.getAttributes() != null){
            String attributes[] = node.getAttributes().split("\\r?\\n");
            for(String att : attributes){
            	textField = new IdentifiedTextField(att);
                Element attribute = doc.createElement("UML:Attribute");
                attribute.setAttribute("name", textField.getText());
                attribute.setAttribute("xmi.id", "att" + UUID.randomUUID().toString() + "_" + node.getId());
                classifierFeature.appendChild(attribute);
            }
        }
        if(node.getOperations() != null){
            String operations[] = node.getOperations().split("\\r?\\n");
            for(String op : operations) {
            	textField = new IdentifiedTextField(op);
                Element operation = doc.createElement("UML:Operation");
                operation.setAttribute("name", textField.getText());
                operation.setAttribute("xmi.id", "oper" + UUID.randomUUID().toString() + "_" + node.getId());
                classifierFeature.appendChild(operation);
            }
        }
        parent.appendChild(umlClass);
    }
    
    private static void addAssociatonEnd(String nodeId, String multiplicityRange, Element association, Document doc, String isStart){
        Element associationEnd = doc.createElement("UML:AssociationEnd");
        associationEnd.setAttribute("xmi.id", "end0");
        associationEnd.setAttribute("type", nodeId);
        associationEnd.setAttribute("association", "ass0");
        associationEnd.setAttribute("isStart", isStart);
        Element associationEnd1Mulitplicity = doc.createElement("UML:AssociationEnd.multiplicity");
        associationEnd.appendChild(associationEnd1Mulitplicity);
        Element multiplicity1 = doc.createElement("UML:multiplicity");
        associationEnd1Mulitplicity.appendChild(multiplicity1);
        Element multiplicityRange1 = doc.createElement("UML:Multiplicity.range");
        multiplicity1.appendChild(multiplicityRange1);
        Element multiplicityRange11 = doc.createElement("UML:MultiplicityRange");
        // Split multiplicityRange string into upper and lower attributes and add to element.
        if (multiplicityRange != null) {
            if (multiplicityRange.contains(".")) {
            	multiplicityRange11.setAttribute("lower", multiplicityRange.substring(0, multiplicityRange.indexOf(".")));
            	multiplicityRange11.setAttribute("upper", multiplicityRange.substring(multiplicityRange.lastIndexOf(".")+1));
        	} else {
        		multiplicityRange11.setAttribute("lower", multiplicityRange); // TODO: Is correct set to lower? 
            	multiplicityRange11.setAttribute("upper", "");
        	}
        } else {
    		multiplicityRange11.setAttribute("lower", ""); 
        	multiplicityRange11.setAttribute("upper", "");
        }
        multiplicityRange1.appendChild(multiplicityRange11);
        association.appendChild(associationEnd);
    }


    //------------------------------------------ IMPORT -----------------------------------------------
    public static Graph importXMIFromPath(String path){
        File xmiFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmiFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        // Remove indentation to avoid errors.
        XPathFactory xfact = XPathFactory.newInstance();
        XPath xpath = xfact.newXPath();
        try {
			NodeList empty =
					(NodeList)xpath.evaluate("//text()[normalize-space(.) = '']",
							doc, XPathConstants.NODESET);
			for (int i = 0; i < empty.getLength(); i++) {
			    Node node = empty.item(i);
			    node.getParentNode().removeChild(node);
			}
        } catch (XPathExpressionException e) {
			e.printStackTrace();
		}        
        return importXMI(doc);
    }
    public static Graph importXMI(Document doc){

        Graph graph = new Graph();
        //IDs are generated when AbstractNodes are created, we need to keep track of the nodes IDs in the xmi were when referenced elsewhere.
        Map<String, AbstractNode> idMap = new HashMap<>();

        NodeList nList = doc.getElementsByTagName("UML:Model");
        Element umlModel = ((Element)nList.item(0));
        graph.setName(umlModel.getAttribute("name"));
        String modelNamespace = umlModel.getAttribute("xmi.id");

        //Import packages
        nList = doc.getElementsByTagName("UML:Package");
        for(int i = 0; i < nList.getLength(); i++){
            Element modelElement = ((Element)nList.item(i));
            NodeList viewList = doc.getElementsByTagName("UML:DiagramElement");
            for(int j = 0; j < viewList.getLength(); j++){ //Find its corresponding view
                Element viewElement = ((Element)viewList.item(j));
                if(viewElement.getAttribute("subject").equals(modelElement.getAttribute("xmi.id"))){
                    Boolean isChild = !modelElement.getAttribute("namespace").equals(modelNamespace);
                    AbstractNode node = createAbstractNode(viewElement, modelElement, isChild, true);
                    idMap.put(modelElement.getAttribute("xmi.id"), node);
                    graph.addNode(node, false);
                }
            }
        }

        //Import classes
        nList = doc.getElementsByTagName("UML:Class");
        for(int i = 0; i < nList.getLength(); i++){
            Element modelElement = ((Element)nList.item(i));
            NodeList viewList = doc.getElementsByTagName("UML:DiagramElement");
            for(int j = 0; j < viewList.getLength(); j++){ //Find its corresponding view
                Element viewElement = ((Element)viewList.item(j));
                if(viewElement.getAttribute("subject").equals(modelElement.getAttribute("xmi.id"))){
                    Boolean isChild = !modelElement.getAttribute("namespace").equals(modelNamespace);
                    AbstractNode node = createAbstractNode(viewElement, modelElement, isChild, false);
                    idMap.put(modelElement.getAttribute("xmi.id"), node);
                    graph.addNode(node, false);
                }
            }
        }

        //Import associations
        nList = doc.getElementsByTagName("UML:Association");
        for(int i = 0; i < nList.getLength(); i++){
            Element associationElement = (Element) nList.item(i);
            String startNodeId = ((Element)associationElement.getChildNodes().item(0).getChildNodes().item(0)).getAttribute("type");
            String endNodeId = ((Element)associationElement.getChildNodes().item(0).getChildNodes().item(1)).getAttribute("type");

            AbstractEdge edge;
            String relation = associationElement.getAttribute("relation");
            String direction = associationElement.getAttribute("direction");
            if (relation.equals("Association")){
                edge = new AssociationEdge(idMap.get(startNodeId), idMap.get(endNodeId));
                edge.setDirection(AbstractEdge.Direction.valueOf(direction));
            } else if (relation.equals("Inheritance")){
                edge = new InheritanceEdge(idMap.get(startNodeId), idMap.get(endNodeId));
                edge.setDirection(AbstractEdge.Direction.valueOf(direction));
            } else if (relation.equals("Aggregation")){
                edge = new AggregationEdge(idMap.get(startNodeId), idMap.get(endNodeId));
                edge.setDirection(AbstractEdge.Direction.valueOf(direction));
            } else if (relation.equals("Composition")){
                edge = new CompositionEdge(idMap.get(startNodeId), idMap.get(endNodeId));
                edge.setDirection(AbstractEdge.Direction.valueOf(direction));
            } else { //Standard is Assocation
                edge = new AssociationEdge(idMap.get(startNodeId), idMap.get(endNodeId));
            }
            // Recovering lower and upper multiplicity
            NodeList nList2 = associationElement.getElementsByTagName("UML:AssociationEnd");
            for(int i2 = 0; i2 < nList2.getLength(); i2++) {
                Element associationEndElement = (Element) nList2.item(i2);
                NodeList nList3 = associationEndElement.getElementsByTagName("UML:MultiplicityRange");
                Element multiplicityRangeElement = (Element) nList3.item(0);
            	String multiplicityRange = multiplicityRangeElement.getAttribute("lower");
            	if (!multiplicityRangeElement.getAttribute("upper").isEmpty()) {
            		multiplicityRange = multiplicityRange + ".." + multiplicityRangeElement.getAttribute("upper");
            	}
                String isStart = associationEndElement.getAttribute("isStart");
                if (isStart.equals("true")){
                	edge.setStartMultiplicity(multiplicityRange);
                } else {
                	edge.setEndMultiplicity(multiplicityRange);
                }
            }            
            graph.addEdge(edge, false);
        }




        //Import sketches
        nList = doc.getElementsByTagName("Sketch");
        for(int i = 0; i < nList.getLength(); i++) {

            Sketch sketch = new Sketch();

            Element sketchElement = (Element) nList.item(i);
            Element pathElement = (Element) sketchElement.getChildNodes().item(0);
            NodeList pathList = pathElement.getChildNodes();
            for (int j = 0; j < pathList.getLength(); j++) {
                Element point = (Element) pathList.item(j);
                if (point.getTagName().equals("MoveTo")) {
                    sketch.setStart(Double.parseDouble(point.getAttribute("xPoint")),
                            Double.parseDouble(point.getAttribute("yPoint")));
                } else if (point.getTagName().equals("LineTo")) {
                    sketch.addPoint(Double.parseDouble(point.getAttribute("xPoint")),
                            Double.parseDouble(point.getAttribute("yPoint")));
                }
            }
            graph.addSketch(sketch, false);
        }
        return graph;
    }

    private static AbstractNode createAbstractNode(Element view, Element model, boolean isChild, boolean isPackage){
        String[] geometry = view.getAttribute("geometry").split(",");
        double x = Double.parseDouble(geometry[0]);
        double y = Double.parseDouble(geometry[1]);
        double width = Double.parseDouble(geometry[2]) - x;
        double height = Double.parseDouble(geometry[3]) - y;
        int attributesCont, operationsCont;

        AbstractNode abstractNode;
        if(!isPackage){
            abstractNode = new ClassNode(x, y, width, height);
            NodeList attsOps = model.getChildNodes().item(0).getChildNodes();
            String attributes = "";
            String operations = "";
            attributesCont = 0;
            operationsCont = 0;
            for(int i = 0; i < attsOps.getLength(); i++){
                Element item = ((Element)attsOps.item(i));
                String name = item.getAttribute("name");
                String xmiId = item.getAttribute("xmi.id");
                if(item.getNodeName().equals("UML:Attribute")){
                    attributes = attributes + attributesCont + ";" + xmiId + IdentifiedTextField.SEPARATOR + name + System.getProperty("line.separator");
                    attributesCont++;
                } else if(item.getNodeName().equals("UML:Operation")){
                    String op = item.getAttribute("name");
                    operations = operations + operationsCont + ";" + xmiId + IdentifiedTextField.SEPARATOR + name + System.getProperty("line.separator");
                    operationsCont++;
                }
            }
            ((ClassNode)abstractNode).setAttributes(attributes);
            ((ClassNode)abstractNode).setOperations(operations);
        } else {
            abstractNode = new PackageNode(x, y, width, height);
        }
        abstractNode.setTitle(model.getAttribute("name"));
        abstractNode.setIsChild(isChild);

        return abstractNode;
    }
}
