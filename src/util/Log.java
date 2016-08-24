package util;

import util.commands.AddDeleteNodeCommand;
import util.commands.Command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chalmers on 201 6-08-24.
 */
public class Log {

    BufferedWriter writer;

    public Log(){
        writer = null;
        try {

            File file = new File(File.separator + "log");
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            //create a temporary file
            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File logFile = new File(File.separator + "log" + File.separator + timeLog + ".csv");
            //logFile.createNewFile();
            // This will output the full path where the file will be written to...
            System.out.println(logFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(logFile));
            closeLog();
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

    public void log(Command command){
        if(command instanceof AddDeleteNodeCommand){
            logAddDeleteNode((AddDeleteNodeCommand) command);
        }
    }

    private void logAddDeleteNode(AddDeleteNodeCommand command){
        StringBuilder post = new StringBuilder();
        post.append(System.currentTimeMillis() + "\t");
        post.append("ADD\t");
        post.append("CLASS\t");
        post.append(command.getNode().getId() + "\t");
        post.append(command.getNode().getTitle() + "\t");
        post.append("TRGID\t");
        post.append("NOTRG\t");
        post.append("DO\t");
        try {
            writer.write(post.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
