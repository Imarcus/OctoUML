package model;

import javafx.geometry.Point2D;
import static org.junit.Assert.assertEquals;

import model.nodes.ClassNode;
import model.nodes.PackageNode;
import org.junit.Test;

/**
 * Created by chris on 2016-02-29.
 */
public class GraphTest {

    @Test
    public void testGraph() {
        Graph graph = new Graph();
        PackageNode packageNode = new PackageNode(0, 0 ,100, 100);
        ClassNode c1 = new ClassNode(0, 0, 25, 25);
        ClassNode c2 = new ClassNode(50, 50, 25, 25);

        Point2D p1 = new Point2D(12, 12);
        Point2D p2 = new Point2D(70, 70);
        Point2D p3 = new Point2D(90, 90);
        graph.addNode(packageNode, false);
        packageNode.addChild(c1);
        packageNode.addChild(c2);
        graph.addNode(c1, false);
        graph.addNode(c2, false);
        assertEquals(graph.findNode(p1), c1);
        assertEquals(graph.findNode(p2), c2);
        assertEquals(graph.findNode(p3), packageNode);
    }
}
