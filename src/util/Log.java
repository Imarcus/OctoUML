package util;

import util.commands.AddDeleteNodeCommand;
import util.commands.Command;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chalmers on 201 6-08-24.
 */
public class Log {

    public enum Dot {DO, UNDO, REDO}

    BufferedWriter writer;

    public Log(){
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
        if(command instanceof AddDeleteNodeCommand){
            logAddDeleteNode((AddDeleteNodeCommand) command, dot);
        }
    }

    private void logAddDeleteNode(AddDeleteNodeCommand command, Dot dot){
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

    private void logAddDelete
}
