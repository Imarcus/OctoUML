package view;

import model.nodes.ClassNode;
import org.junit.Test;
import view.nodes.ClassNodeView;

import static org.junit.Assert.assertEquals;

public class ClassNodeViewTest {

    @Test
    public void testNodes() {
        double delta = 0.001d;
        ClassNode mNode = new ClassNode(0, 0, 0, 0);
        ClassNodeView vNode = new ClassNodeView(mNode);
        assert vNode.getX() == mNode.getX();
        mNode.setX(5);
        mNode.setY(10.555);
        mNode.setWidth(66);
        mNode.setHeight(87);
        assertEquals(vNode.getX(), 5, delta);
        assertEquals(vNode.getY(), 10.555d, delta);
        assertEquals(vNode.getWidth(), 66, delta);
        assertEquals(vNode.getHeight(), 87, delta);
        assertEquals(vNode.getX(), mNode.getX(), delta);
    }
}