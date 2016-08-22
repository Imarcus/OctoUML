package controller;

import controller.dialog.GithubLoginDialogController;
import controller.dialog.GithubRepoDialogController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The class controlling the top menu and the tabs.
 */
public class TabController {

    @FXML
    private CheckMenuItem umlMenuItem, sketchesMenuItem, mouseMenuItem, gridMenuItem, snapToGridMenuItem, snapIndicatorsMenuItem;

    @FXML
    Pane content;

    @FXML
    private TabPane tabPane;

    private Stage stage;

    private Map<Tab, MainController> tabMap = new HashMap<>();


    @FXML
    public void initialize() {
    }

    public TabPane getTabPane(){
        return tabPane;
    }

    public void setStage(Stage pStage){
        stage = pStage;
    }

    public Tab addTab(){
        BorderPane canvasView = null;
        MainController mainController = null;
        FXMLLoader loader;

        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
            canvasView = loader.load();
            mainController = loader.getController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Tab tab = new Tab();

        tab.setContent(canvasView);
        tabMap.put(tab, mainController);
        tab.setText("Diagram " + tabMap.size());

        tabPane.getTabs().add(tab);
        mainController.setStage(stage);
        return tab;
    }

    public void handleMenuActionUML() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionUML();
    }
    public void handleMenuActionSketches() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSketches();
    }
    public void handleMenuActionGrid() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionGrid();
    }
    public void handleMenuActionMouse() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionMouse();
    }
    public void handleMenuActionExit() {
        Platform.exit();
    }
    public void handleMenuActionSave() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSave();
    }
    public void handleMenuActionLoad() {
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        tabMap.get(tab).handleMenuActionLoad();
        tab.setText(tabMap.get(tab).getGraphModel().getName());
    }
    public void handleMenuActionNew() {
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
    }

    public void handleMenuActionServer(){
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionServer();
    }

    public void handleMenuActionClient(){
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        if(!tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionClient()){
            tabPane.getTabs().remove(tab);
        }
    }

    public void handleMenuActionImage(){
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionImage();
    }

    public void handleMenuActionSnapToGrid() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSnapToGrid(snapToGridMenuItem.isSelected());
    }

    public void handleMenuActionSnapIndicators() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSnapIndicators(snapIndicatorsMenuItem.isSelected());
    }

    public void handleMenuActionGit(){
        try {
            File localPath = File.createTempFile("GitRepository", "");
            localPath.delete();

            GithubRepoDialogController gitRepoController = showGithubRepoDialog();

            if(gitRepoController != null && gitRepoController.isOkClicked()){
                Git git = Git.cloneRepository()
                        .setURI(gitRepoController.urlTextField.getText())
                        .setDirectory(localPath)
                        .call();

                MainController mainController = tabMap.get(tabPane.getSelectionModel().getSelectedItem());

                if(gitRepoController.imageCheckBox.isSelected()){
                    WritableImage image = mainController.getSnapShot();
                    String imageFileName = gitRepoController.fileNameTextField.getText() + ".png";
                    File imageFile = new File(localPath + "/" + imageFileName);
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", imageFile);

                    git.add().addFilepattern(imageFileName).call();
                }

                if(gitRepoController.xmiCheckBox.isSelected()) {
                    String xmiFileName = gitRepoController.fileNameTextField.getText() + ".xmi";
                    mainController.createXMI(xmiFileName);
                    git.add().addFilepattern(xmiFileName).call();

                }

                git.commit().setMessage(gitRepoController.commitTextField.getText()).call();
                PushCommand pushCommand = git.push();
                GithubLoginDialogController gitLoginController = showGithubLoginDialog();
                if(gitLoginController != null && gitLoginController.isOkClicked()){
                    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitLoginController.nameTextField.getText(),
                            gitLoginController.passwordTextField.getText()));
                    pushCommand.call();
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void stop(){
        for(MainController mc : tabMap.values()){
            mc.closeServers();
            mc.closeClients();
        }
    }

    public GithubRepoDialogController showGithubRepoDialog(){
        GithubRepoDialogController controller = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("githubRepoDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.stage);
            dialogStage.setScene(new Scene(page));

            controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
        }
        return controller;

    }

    public GithubLoginDialogController showGithubLoginDialog(){
        GithubLoginDialogController controller = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("githubLoginDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.stage);
            dialogStage.setScene(new Scene(page));

            controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

        } catch (IOException e){
            e.printStackTrace();
        }

        return controller;

    }
}
