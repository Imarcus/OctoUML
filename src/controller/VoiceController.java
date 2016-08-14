package controller;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import sun.applet.Main;

/**
 * Created by Marcus on 2016-08-14.
 */
public class VoiceController {

    private MainController mainController;

    public VoiceController(MainController pMainController){
        mainController = pMainController;
    }

    public Thread A;
    public boolean voiceEnabled = false;

    //starts the voice recognition if it is not started when pressing the button
    public void onVoiceButtonClick() {
        if (!voiceEnabled) {
            System.out.println("Starting voice commands");
            voiceEnabled = true;
            A = new Thread(new VoiceTask());
            A.setDaemon(true);
            A.start();
            mainController.voiceBtn.getStyleClass().add("button-in-use");
        }
        else{
            System.out.println("Stopping voice commands");
            voiceEnabled = false;
            A.interrupt();
            mainController.voiceBtn.getStyleClass().remove("button-in-use");
        }

    }

    //get the name the user wants to name the class or package during naming in NodeConroller
    public String titleName = "";
    //used to see if the user is in naming mode or regular mode
    public int testing = 0;


    public int config = 0;
    public int mic = 0;
    Configuration configuration;
    LiveSpeechRecognizer voiceGrammar;
    public void initVoice(){

        if (config == 0){
            configuration = new Configuration();

            configuration
                    .setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration
                    .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration
                    .setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
            //set the path to the file "hello.gram"
            configuration.setGrammarPath("file:C:/Users/chalmers/gitrepos/PenguinUML/src/controller/"); //Laptop
            configuration.setGrammarPath("file:C:/Users/Marcus/Documents/mthubrepo/PenguinUML"); //Home PC
            configuration.setGrammarName("hello");
            configuration.setUseGrammar(true);
            config = 1;
        }
        else {
            System.out.println("Configuration already done");
        }
        if (mic == 0){
            try{
                voiceGrammar = new LiveSpeechRecognizer(configuration);
                mic = 1;
            }
            catch (Exception e){
                System.out.println(e);

            }
        }
        else {
            System.out.println("Microphone already started");
        }
    }


    //return which tools that are recognised or sets a name on a class or package, from the voice-input
    public String voiceCommands() {
        initVoice();

        voiceGrammar.startRecognition(true);

        while(voiceEnabled) {

            String resultText = voiceGrammar.getResult().getHypothesis();

            if (resultText.equals("create class") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if (resultText.equals("create edge") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if (resultText.equals("create package") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if (resultText.equals("choose draw") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if (resultText.equals("choose select") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if (resultText.equals("choose move") && testing == 0) {
                voiceGrammar.stopRecognition();
                return resultText;
            }
            else if(resultText.length() >= 4 && testing == 1) {
                if (resultText.substring(0, 4).matches("name")) {
                    System.out.println("You said: " + resultText + "\n");
                    String endResult = resultText.replace("name ", "");
                    endResult = endResult.toUpperCase();
                    titleName = endResult;
                }
            }
            else {
                System.out.println("Unknown command: " + resultText + "\n");
            }
        }

        return null;
    }


    private void voice(){ //change to the tool that are recognised from the voice-input
        Button previousButton = mainController.buttonInUse;
        MainController.ToolEnum previousTool = mainController.getTool();

        String buttonMode = "";
        while(buttonMode.equals("") || buttonMode == null) {
            buttonMode = voiceCommands();
        }

        if (buttonMode.equals("create class")) {
            mainController.setTool(MainController.ToolEnum.CREATE_CLASS);
            mainController.setButtonClicked(mainController.createBtn);
        }
        else if (buttonMode.equals("create package")) {
            mainController.setTool(MainController.ToolEnum.CREATE_PACKAGE);
            mainController.setButtonClicked(mainController.packageBtn);
        }
        else if (buttonMode.equals("create edge")) {
            mainController.setTool(MainController.ToolEnum.EDGE);
            mainController.setButtonClicked(mainController.edgeBtn);
        }
        else if (buttonMode.equals("choose select")) {
            mainController.setTool(MainController.ToolEnum.SELECT);
            mainController.setButtonClicked(mainController.selectBtn);
        }
        else if (buttonMode.equals("choose draw")) {
            mainController.setTool(MainController.ToolEnum.DRAW);
            mainController.setButtonClicked(mainController.drawBtn);
        }
        else if (buttonMode.equals("choose move")) {
            mainController.setTool(MainController.ToolEnum.MOVE_SCENE);
            mainController.setButtonClicked(mainController.moveBtn);
        }
        else if (buttonMode.equals("undo")) {
            mainController.getUndoManager().undoCommand();
            mainController.setTool(previousTool);
            mainController.setButtonClicked(previousButton);
        }
        else if (buttonMode.equals("redo")) {
            mainController.getUndoManager().redoCommand();
            mainController.setTool(previousTool);
            mainController.setButtonClicked(previousButton);
        }
        // Running this function until the program is closed
        if(voiceEnabled){
            voice();
        }
    }

    private class VoiceTask extends Task<Void> {
        @Override protected Void call() throws Exception {
            int iterations = 0;

            voice();

            return null;
        }

        @Override protected void succeeded() {
            super.succeeded();
            updateMessage("Done!");
        }

        @Override protected void cancelled() {
            super.cancelled();
            updateMessage("Cancelled!");
        }

        @Override protected void failed() {
            super.failed();
            updateMessage("Failed!");
        }
    };
}
