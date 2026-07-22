package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SessionManager;
//
public class Main extends Application {
//    testing if update works from another computer. -Awit Sandoy

    @Override
    public void start(Stage primaryStage) throws Exception {
/*      Safety net: always start at the login screen with no leftover session file,
        in case the app was previously closed abnormally (e.g. crash) without going
        through the normal logout flow.
*/
        SessionManager.destroySession();

        Parent root = FXMLLoader.load(getClass().getResource("/views/LoginRegister.fxml"));
        primaryStage.setTitle("Smart Parking Management System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
