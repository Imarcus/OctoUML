package sikuli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import controller.Launcher;
import javafx.stage.Stage;
import org.sikuli.script.*;
import org.sikuli.basics.Debug;
import org.junit.*;


public class GUITest {

    Screen screen;

    public GUITest(){
        screen = new Screen();
        Launcher.launch(Launcher.class);
    }

    @Test
    public void testGUI() throws FindFailed {
        screen.click("/tests/sikuli/images/startClassDiagram.PNG");

        //Draw first rectangle
        screen.click("/tests/sikuli/images/drawTool.PNG");
        Mouse.move(0, 100);
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 150);
        Mouse.move(150, 0);
        Mouse.move(0, -150);
        Mouse.move(-150, 0);
        Mouse.up();

        assertNotEquals(screen.exists("tests/sikuli/images/rectangleDrawing.PNG"), null);

        //Select and transform rectangle
        screen.click("tests/sikuli/images/selectTool.PNG");
        screen.mouseMove("tests/sikuli/images/rectangleDrawing.PNG");
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 150);
        Mouse.up();
        screen.click("tests/sikuli/images/recognizeButton.PNG");

        assertNotEquals(screen.exists("tests/sikuli/images/recognizedClass.PNG"), null);


        //Draw second rectangle
        screen.click("/tests/sikuli/images/drawTool.PNG");
        screen.mouseMove("tests/sikuli/images/recognizedClass.PNG");
        Mouse.move(0, 100);
        Mouse.down(Mouse.LEFT);
        Mouse.move(0, 200);
        Mouse.move(200, 0);
        Mouse.move(0, -200);
        Mouse.move(-200, 0);
        Mouse.up();

        assertNotEquals(screen.exists("tests/sikuli/images/rectangleDrawing2.PNG"), null);

        //Draw first line between second and first rectangle
        screen.mouseMove("tests/sikuli/images/rectangleDrawing2.PNG");
        Mouse.down(Mouse.LEFT);
        Mouse.move(-100, -150);
        Mouse.up();

        //Select and transform first line and second rectangle
        screen.click("tests/sikuli/images/selectTool.PNG");
        screen.mouseMove("tests/sikuli/images/rectangleDrawing2.PNG");
        Mouse.move(5,5);
        Mouse.down(Mouse.LEFT);
        Mouse.move(-100, -150);
        Mouse.up();
        screen.click("tests/sikuli/images/recognizeButton.PNG");

        assertNotEquals(screen.exists("tests/sikuli/images/recognizedDiagram.PNG"), null);

        screen.click("tests/sikuli/images/undoButton.PNG"); //Undo second transform
        screen.click("tests/sikuli/images/undoButton.PNG"); //Undo draw first line
        screen.click("tests/sikuli/images/undoButton.PNG"); //Undo draw second rectangle
        screen.click("tests/sikuli/images/undoButton.PNG"); //Undo first transform
        screen.click("tests/sikuli/images/undoButton.PNG"); //Undo draw first rectangle

        assertEquals(screen.exists("tests/sikuli/images/recognizedDiagram.PNG"), null);

        screen.click("tests/sikuli/images/redoButton.PNG"); //redo draw first rectangle
        screen.click("tests/sikuli/images/redoButton.PNG"); //redo first transform
        screen.click("tests/sikuli/images/redoButton.PNG"); //redo draw second rectangle
        screen.click("tests/sikuli/images/redoButton.PNG"); //redo draw first line
        screen.click("tests/sikuli/images/redoButton.PNG"); //redo second transform

        assertNotEquals(screen.exists("tests/sikuli/images/recognizedDiagram.PNG"), null);

    }
}
