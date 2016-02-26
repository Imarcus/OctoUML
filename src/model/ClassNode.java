package model;

import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class ClassNode extends AbstractNode
{
    private ArrayList<String> aAttributes = new ArrayList<>();
    private ArrayList<String> aOperations = new ArrayList<>();


    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    }

    public ArrayList<String> getAttributes() {
        return aAttributes;
    }

    public void setAttributes(ArrayList<String> aAttributes) {
        this.aAttributes = aAttributes;
    }

    public ArrayList<String> getOperations() {
        return aOperations;
    }

    public void setOperations(ArrayList<String> aOperations) {
        this.aOperations = aOperations;
    }

}
