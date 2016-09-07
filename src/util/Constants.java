package util;

import javafx.scene.paint.Color;

/**
 * Constants used in the project.
 */
public class Constants {
    public static Color not_selected_color = Color.web("#7EC6FF");
    public static Color selected_color = Color.web("#FF6D70");
    public static Color selected_sketch_color = Color.web("#884FA8");

    public static int GRID_DISTANCE = 20;


    //--------------------- Network Constants -----------------------------------
    public static String requestGraph = "RequestGraph";
    public static String NodeAdd = "NodeAdd";
    public static String NodeRemove = "NodeRemove";
    public static String EdgeAdd = "EdgeAdd";
    public static String EdgeRemove = "EdgeRemove";
    public static String sketchAdd = "SketchAdd";
    public static String sketchRemove = "SketchRemove";

    public static String changeNodeX = "NodeX";
    public static String changeNodeY = "NodeY";
    public static String changeNodeWidth = "NodeWidth";
    public static String changeNodeHeight = "NodeHeight";
    public static String changeNodeTranslateX = "NodeTranslateX";
    public static String changeNodeTranslateY = "NodeTranslateY";
    public static String changeNodeScaleX = "NodeScaleX";
    public static String changeNodeScaleY = "NodeScaleY";
    public static String changeNodeTitle = "NodeTitle";
    public static String changeClassNodeAttributes = "ClassNodeAttributes";
    public static String changeClassNodeOperations = "ClassNodeOperations";
    public static String changeNodeIsChild = "ClassNodeIsChild";
    public static String changeLifelineLength = "LifelineLength";

    public static String changeEdgeDirection = "EdgeDirection";
    public static String changeEdgeStartNode = "EdgeStartNode";
    public static String changeEdgeEndNode = "EdgeEndNode";
    public static String changeEdgeZoom = "EdgeZoom";
    public static String changeEdgeStartMultiplicity = "EdgeStartMultiplicity";
    public static String changeEdgeEndMultiplicity = "EdgeEndMultiplicity";
    public static String changeMessageStartX = "MessageStartX";
    public static String changeMessageStartY = "MessageStartY";
    public static String changeMessageTitle = "MessageTitle";
    public static String changeMessageType = "MessageType";

    public static String changeSketchStart = "SketchStart";
    public static String changeSketchPoint = "SketchPoint";
    public static String changeSketchStroke = "SketchStroke";
    public static String changeSketchTranslateX = "SketchTranslateX";
    public static String changeSketchTranslateY = "SketchTranslateY";

}
