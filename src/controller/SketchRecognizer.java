package controller;

import edu.tamu.recognition.paleo.PaleoConfig;
import edu.tamu.recognition.paleo.PaleoSketchRecognizer;

/**
 * Singleton Wrapper for the PaleoRegognizer.
 */
public class SketchRecognizer {
    private static PaleoSketchRecognizer recognizer;
    private static SketchRecognizer instance;

    private SketchRecognizer(){
        //TODO Change this depending on which shapes to recognize.
        //TODO Other settings on the PaleoSketchRecognizer could be made here.
        recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
    }

    public SketchRecognizer getInstance(){
        if (instance == null){
            instance = new SketchRecognizer();
        }
        return instance;
    }

    public PaleoSketchRecognizer getRecognizer() {
        return recognizer;
    }
}
