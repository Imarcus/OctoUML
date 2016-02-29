package model;

import javafx.geometry.Point2D;
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

        graph.addNode(packageNode);
        graph.addNode(c1);
        graph.addNode(c2);


    }
}
