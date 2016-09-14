package model;

import javafx.geometry.Point2D;
import static org.junit.Assert.assertEquals;

import model.nodes.ClassNode;
import model.nodes.PackageNode;
import org.junit.Test;


/**
 * Created by chris on 2016-02-29.
 */
public class PackageTest {

    @Test
    public void testModelPackage() {
        PackageNode pNode = new PackageNode(0, 0, 100, 100);
        ClassNode c1 = new ClassNode(0, 0, 25, 25);
        ClassNode c2 = new ClassNode(50, 50, 25, 25);
        pNode.addChild(c1);
        pNode.addChild(c2);

        Point2D p1 = new Point2D(12, 12);
        Point2D p2 = new Point2D(70, 70);
        assertEquals(pNode.findNode(p1), c1);
        assertEquals(pNode.findNode(p2), c2);
    }

}
