package com.github.ashutoshgngwr.minventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.database.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String APP_NAME = "mInventory";
	public static final String PROPERTIES_FILE_NAME = "./data/" + APP_NAME + ".properties";
	public static final String PROPERTY_BUSINESS_NAME = "b_name";

	protected static Properties properties = new Properties();
	protected static User user;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root;

		try {
			properties.load(new FileInputStream(new File(PROPERTIES_FILE_NAME)));
			root = FXMLLoader.load(getClass().getResource("/layout/AuthenticationLayout.fxml"));
		} catch (FileNotFoundException e) {
			root = FXMLLoader.load(getClass().getResource("/layout/SetPasswordLayout.fxml"));
		}

		Scene scene = new Scene(root, 720, 640);
		primaryStage.setTitle(APP_NAME);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
		primaryStage.setMaximized(true);
		primaryStage.setMinWidth(scene.getWidth());
		primaryStage.setMinHeight(scene.getHeight());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void stop() throws SQLException {
		try {
			DatabaseHandler.getInstance().closeConnection();
		} catch (IllegalStateException ignore) {}
	}
}
