package sikuli;

import org.sikuli.script.*;
import org.sikuli.basics.Debug;
class Test {
    public static void main(String[] args) throws FindFailed {
        Debug.setDebugLevel(3);
        Screen s = new Screen();
        s.click("/tests/sikuli/images/startClassDiagram.PNG");

        //Draw first rectangle
        s.click("/tests/sikuli/images/drawTool.PNG");
        Mouse.move(0, 100);
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 150);
        Mouse.move(150, 0);
        Mouse.move(0, -150);
        Mouse.move(-150, 0);
        Mouse.up();

        //Select and transform rectangle
        s.click("tests/sikuli/images/selectTool.PNG");
        s.mouseMove("tests/sikuli/images/rectangleDrawing.PNG");
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 150);
        Mouse.up();
        s.click("tests/sikuli/images/recognizeButton.PNG");

        //Draw second rectangle
        s.click("/tests/sikuli/images/drawTool.PNG");
        s.mouseMove("tests/sikuli/images/recognizedClass.PNG");
        Mouse.move(0, 100);
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 200);
        Mouse.move(200, 0);
        Mouse.move(0, -200);
        Mouse.move(-200, 0);
        Mouse.up();

        //Draw first line between second and first rectangle
        s.mouseMove("tests/sikuli/images/rectangleDrawing2.PNG");
        Mouse.down(Mouse.LEFT);
        Mouse.move(-100, -150);
        Mouse.up();

        //Select and transform first line and second rectangle
        s.click("tests/sikuli/images/selectTool.PNG");
        s.mouseMove("tests/sikuli/images/rectangleDrawing2.PNG");
        Mouse.move(5,5);
        Mouse.down(Mouse.LEFT);
        Mouse.move(-100, -150);
        Mouse.up();
        s.click("tests/sikuli/images/recognizeButton.PNG");

        s.click("tests/sikuli/images/undoButton.PNG"); //Undo second transform
        s.click("tests/sikuli/images/undoButton.PNG"); //Undo draw first line
        s.click("tests/sikuli/images/undoButton.PNG"); //Undo draw second rectangle
        s.click("tests/sikuli/images/undoButton.PNG"); //Undo first transform
        s.click("tests/sikuli/images/undoButton.PNG"); //Undo draw first rectangle

        s.click("tests/sikuli/images/redoButton.PNG"); //redo draw first rectangle
        s.click("tests/sikuli/images/redoButton.PNG"); //redo first transform
        s.click("tests/sikuli/images/redoButton.PNG"); //redo draw second rectangle
        s.click("tests/sikuli/images/redoButton.PNG"); //redo draw first line
        s.click("tests/sikuli/images/redoButton.PNG"); //redo second transform
    }
}
