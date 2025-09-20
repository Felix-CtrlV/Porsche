package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NotificationCardController {

    @FXML
    private HBox root;

    @FXML
    private Label usernameLabel, messageLabel, timeLabel;

    public void setData(String username, String message, String time, boolean isLogin) {
        usernameLabel.setText(username);
        messageLabel.setText(message);
        timeLabel.setText(time);

        if (isLogin) {
            root.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 8;");
        } else {
            root.setStyle("-fx-background-color: #F44336; -fx-background-radius: 8;");
        }
    }
}
