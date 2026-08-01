package com.campuslink.app;

import com.campuslink.utils.AppDataSource;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/Login.fxml")
        );
        Scene scene = new Scene(loader.load(), 480, 600);
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );

        primaryStage.setTitle("CampusLink Career");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        try {
            primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")))
            );
        } catch (Exception ignored) {
            // Icon is optional
        }
    }

    @Override
    public void stop() {
        try {
            AppDataSource.getInstance().shutdown();
        } catch (Exception ignored) {
            // ignore shutdown failures during exit
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
