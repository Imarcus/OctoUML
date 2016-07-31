package util;

import controller.ClientController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by Marcus on 2016-07-30.
 */
public class NetworkUtils {

    public static String[] queryServerPort() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Connect to server");
        dialog.setHeaderText("Enter server IP and port number");

        ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverIP = new TextField();
        serverIP.setText("127.0.0.1");
        TextField port = new TextField();
        port.setText("54555");

        Platform.runLater(() -> serverIP.requestFocus());

        grid.add(new Label("Server IP:"), 0, 0);
        grid.add(serverIP, 1, 0);
        grid.add(new Label("Port number:"), 0, 1);
        grid.add(port, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(serverIP.getText(), port.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        String[] s = new String[2];
        if (result.isPresent() && validateIP(result.get().getKey()) && Integer.parseInt(result.get().getValue()) >= 1024 && Integer.parseInt(result.get().getValue()) <= 65535) {
            s[0] = result.get().getKey();
            s[1] = result.get().getValue();

        } else {
            s = null;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid input");
            alert.setHeaderText("Invalip input. \nClosing diagram.");
            alert.showAndWait();
        }
        return s;
    }

    //Regex for IPv4-pattern, found: http://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Validates whether given string is valid IPv4-address
     * @param ip String to validate
     * @return true if String is valid IPv4-address, false otherwise
     */
    public static boolean validateIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
