package model.nodes;

import util.Constants;

/**
 * Created by Marcus on 2016-09-01.
 */
public class SequenceObject extends AbstractNode {

    public static final String TYPE = "LIFELINE";
    public final double LIFELINE_DEFAULT_LENGTH = 500;

    private double lifelineLength = LIFELINE_DEFAULT_LENGTH;

    public SequenceObject(double x, double y, double width, double height)
    {
        super(x, y, width, height );
        //Don't accept nodes with size less than minWidth * minHeight.
        this.width = width < LIFELINE_MIN_WIDTH ? LIFELINE_MIN_WIDTH : width;
        this.height = height < LIFELINE_MIN_HEIGHT ? LIFELINE_MIN_HEIGHT : height;
    }

    @Override
    public AbstractNode copy() {
        return null;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public SequenceObject(){
    }

    @Override
    public void setHeight(double height) {
        this.height = height < LIFELINE_MIN_HEIGHT ? LIFELINE_MIN_HEIGHT : height;
        super.setHeight(height);
    }

    @Override
    public void setWidth(double width) {
        this.width = width < LIFELINE_MIN_WIDTH ? LIFELINE_MIN_WIDTH : width;
        super.setWidth(width);
    }

    @Override
    public void remoteSetHeight(double height) {
        this.height = height < LIFELINE_MIN_HEIGHT ? LIFELINE_MIN_HEIGHT : height;
        super.remoteSetHeight(height);
    }

    @Override
    public void remoteSetWidth(double width) {
        this.width = width < LIFELINE_MIN_WIDTH ? LIFELINE_MIN_WIDTH : width;
        super.remoteSetWidth(width);
    }


    @Override
    public String getType() {
        return TYPE;
    }

    public double getLifelineLength() {
        return lifelineLength;
    }

    public void setLifelineLength(double lifelineLength) {
        this.lifelineLength = lifelineLength;
        changes.firePropertyChange(Constants.changeLifelineLength, null, lifelineLength);
        remoteChanges.firePropertyChange(Constants.changeLifelineLength, null, lifelineLength);
    }

    public void remoteSetLifelineLength(double lifelineLength) {
        this.lifelineLength = lifelineLength;
        changes.firePropertyChange(Constants.changeLifelineLength, null, lifelineLength);
    }
}
