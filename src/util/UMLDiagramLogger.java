package util;

import model.Node;
import util.commands.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chalmers on 201 6-08-24.
 */
public class UMLDiagramLogger {

    public enum Dot {DO, UNDO, REDO}

    BufferedWriter writer;

    public UMLDiagramLogger(){
        writer = null;
        try {

            File file = new File(System.getProperty("user.dir") + File.separator  + "log");
            if (!file.exists()) {
                file.mkdir();
            }

            String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(System.getProperty("user.dir") + File.separator + "log" + File.separator + "OctoUMLLog"+ time + ".csv"), "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeLog(){
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Command command, Dot dot){
        System.out.println(command.toString());
        if(command instanceof AddDeleteNodeCommand){
            if(((AddDeleteNodeCommand) command).isAdding()){
                logAddNode((AddDeleteNodeCommand) command, dot);
            } else {
                logDeleteNode((AddDeleteNodeCommand) command, dot);
            }
        } else if (command instanceof AddDeleteEdgeCommand) {
            if(((AddDeleteEdgeCommand) command).isAdding()){
                logAddEdge((AddDeleteEdgeCommand)command, dot);
            } else {
                logDeleteEdge((AddDeleteEdgeCommand)command, dot);
            }
        } else if (command instanceof MoveGraphElementCommand){
            if(((MoveGraphElementCommand) command).getGraphElement() instanceof Node){
                logMoveNode((MoveGraphElementCommand)command, dot);
            }
        } else if (command instanceof AddDeleteSketchCommand){
            if(((AddDeleteSketchCommand)command).isAdding()){
                logAddSketch((AddDeleteSketchCommand)command, dot);
            } else {
                logDeleteSketch((AddDeleteSketchCommand)command, dot);
            }
        } else if (command instanceof CompoundCommand){
            for(Command subCommand : ((CompoundCommand)command).getCommands()){
                log(subCommand, dot);
            }
        }
    }

    private void logAddSketch(AddDeleteSketchCommand command, Dot dot){
        //<DT>  ADD <OBT> <OBID> <OBN> <TRGID> <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("ADD\t"); //ADD
        post.append("SKETCH\t"); //OBT
        post.append(command.getSketch().getId() + "\t"); //OBID
        post.append("null\t"); //OBN
        post.append("null\t"); //TRGID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logDeleteSketch(AddDeleteSketchCommand command, Dot dot){
        //<DT> DELETE <OBT> [<OBID>]* <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("DELETE\t"); //ADD
        post.append("SKETCH\t"); //OBT
        post.append(command.getSketch().getId() + "\t"); //OBID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logAddNode(AddDeleteNodeCommand command, Dot dot){
        //<DT>  ADD <OBT> <OBID> <OBN> <TRGID> <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("ADD\t"); //ADD
        post.append("CLASS\t"); //OBT
        post.append(command.getNode().getId() + "\t"); //OBID
        post.append(command.getNode().getTitle() + "\t"); //OBN
        post.append("null\t"); //TRGID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logDeleteNode(AddDeleteNodeCommand command, Dot dot){
        //<DT> DELETE <OBT> [<OBID>]* <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("DELETE\t"); //DELETE
        post.append("CLASS\t"); //OBT
        post.append(command.getNode().getId() + "\t"); //OBID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logAddEdge(AddDeleteEdgeCommand command, Dot dot){
        //<DT>  ADD <OBT> <OBID> <OBN> <TRGID> <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("ADD\t"); //ADD
        post.append(command.getEdge().getType().toUpperCase() + "\t"); //OBT
        post.append(command.getEdge().getId() + "\t"); //OBID
        post.append("null\t"); //OBN
        post.append("null\t"); //TRGID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logLink(command, dot);
    }

    private void logDeleteEdge(AddDeleteEdgeCommand command, Dot dot){
        //<DT> DELETE <OBT> [<OBID>]* <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("DELETE\t"); //DELETE
        post.append("CLASS\t"); //OBT
        post.append(command.getEdge().getId() + "\t"); //OBID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logLink(AddDeleteEdgeCommand command, Dot dot){
        //<DT> LINK <OBT> <OBID> <OBN> <SRCID> <TRGID> <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("LINK\t"); //LINK
        post.append(command.getEdge().getType().toUpperCase() + "\t"); //OBT
        post.append(command.getEdge().getId() + "\t"); //OBID
        post.append("null\t"); //OBN
        post.append(command.getEdge().getStartNode().getId() + "\t"); //SRCID
        post.append(command.getEdge().getEndNode().getId() + "\t"); //TRGID
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logMoveNode(MoveGraphElementCommand command, Dot dot){
        //<DT> MOVE <OBT> [<OBID> <OBN>]* <COOR1> <COOR2> <DOT>
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t"); //DT
        post.append("MOVE\t"); //MOVE
        post.append("CLASS\t"); //OBT
        post.append(command.getGraphElement().getId() + "\t"); //OBID
        post.append(((Node)command.getGraphElement()).getTitle() + "\t"); //OBN
        post.append(command.getStartX() + "," + command.getStartY() + "\t"); //COOR1
        post.append(command.getEndX() + "," + command.getEndY() + "\t"); //COOR2
        post.append(dot + "\t"); //DOT
        post.append("\n");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
