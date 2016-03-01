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

    @Override
    public ClassNode copy(){
        ClassNode newCopy = new ClassNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        newCopy.setTranslateX(this.getTranslateX());
        newCopy.setTranslateY(this.getTranslateY());
        newCopy.setScaleX(this.getScaleX());
        newCopy.setScaleY(this.getScaleY());

        if(this.getTitle() != null){
            newCopy.setTitle(this.getTitle());

        }
        if(this.getAttributes() != null){
            newCopy.setAttributes(this.getAttributes());
        }
        if(this.getOperations() != null){
            newCopy.setOperations(getOperations());
        }
        newCopy.setTranslateX(this.getTranslateX());
        newCopy.setTranslateY(this.getTranslateY());
        return newCopy;
    }
}
