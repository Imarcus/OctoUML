package controller;

import edu.tamu.core.sketch.Point;
import edu.tamu.core.sketch.Shape;
import edu.tamu.core.sketch.Stroke;
import edu.tamu.recognition.paleo.PaleoConfig;
import edu.tamu.recognition.paleo.PaleoSketchRecognizer;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 2016-02-23.
 */
public class RecognizeController {
    private Pane aDrawPane;
    private MainController mainController;
    private PaleoSketchRecognizer recognizer;
    private ArrayList<Sketch> sketchesToBeRemoved;


    public RecognizeController(Pane pDrawPane, MainController mController) {
        aDrawPane = pDrawPane;
        this.mainController = mController;

        //TODO Find a nicer solution for this:
        //This is to load the recognizer when starting app, not when starting to draw.
        recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
        Stroke init = new Stroke();
        init.addPoint(new Point(0,1));
        recognizer.setStroke(init);
        recognizer.recognize().getBestShape();
    }

    public synchronized ArrayList<GraphElement> recognize(List<Sketch> sketches) {
        ArrayList<GraphElement> recognizedElements = new ArrayList<>();
        sketchesToBeRemoved = new ArrayList<>();

        //Go through all sketches to find Nodes.
        for (Sketch s : sketches) {
            if (s.getStroke() != null && s.getStroke().getPoints() != null && !s.getStroke().getPoints().isEmpty()) {
                //TODO This sometimes throws IndexOutOfBoundsException...
                recognizer.setStroke(s.getStroke());
                Shape bestMatch = recognizer.recognize().getBestShape();
                String bestMatchString = bestMatch.getInterpretation().label;
                System.out.println(bestMatchString);
                if (bestMatchString.equals("Square") || bestMatchString.equals("Rectangle")) {
                    double x = s.getStroke().getBoundingBox().getX();
                    double y = s.getStroke().getBoundingBox().getY();
                    double width = s.getStroke().getBoundingBox().getWidth();
                    double height = s.getStroke().getBoundingBox().getHeight();
                    s.setRecognizedElement(new ClassNode(x, y, width, height));
                    System.out.println("New ClassNode: " + s.getRecognizedElement().toString());
                    mainController.getGraphModel().addNode((ClassNode)s.getRecognizedElement());
                    recognizedElements.add(s.getRecognizedElement());
                    sketchesToBeRemoved.add(s);
                    aDrawPane.getChildren().remove(s.getPath());
                }

            }
        }

        //Go through all sketches to find edges.
        for (Sketch s : sketches) {
            if (s.getStroke() != null) {
                recognizer.setStroke(s.getStroke());
                Shape bestMatch = recognizer.recognize().getBestShape();
                String bestMatchString = bestMatch.getInterpretation().label;
                System.out.println(bestMatchString);

                if (bestMatchString.equals("Line") || bestMatchString.startsWith("Polyline") ||
                        bestMatchString.equals("Arc") || bestMatchString.equals("Curve")){
                    //TODO Hmm, quite messy way to create Edges...
                    Point2D startPoint = new Point2D(s.getStroke().getFirstPoint().getX(), s.getStroke().getFirstPoint().getY());
                    Point2D endPoint = new Point2D(s.getStroke().getLastPoint().getX(), s.getStroke().getLastPoint().getY());

                    System.out.println("STARTPOINT: " + startPoint.toString() + " ENDPOINT: " + endPoint.toString());

                    Node startNode = mainController.getGraphModel().findNode(startPoint);
                    Node endNode = mainController.getGraphModel().findNode(endPoint);
                    if (startNode != null && endNode != null && !startNode.equals(endNode)) {
                        s.setRecognizedElement(new AssociationEdge(startNode, endNode));
                        System.out.println("Recognized an Edge: " + s.getRecognizedElement().toString());
                        recognizedElements.add(s.getRecognizedElement());
                        sketchesToBeRemoved.add(s);
                        aDrawPane.getChildren().remove(s.getPath());
                    }
                }
            }
        }
        return recognizedElements;
    }

    public ArrayList<Sketch> getSketchesToBeRemoved() {
        return sketchesToBeRemoved;
    }
}
