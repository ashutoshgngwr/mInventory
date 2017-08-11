package com.github.ashutoshgngwr.minventory;

import java.io.File;
import java.io.IOException;

import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String APP_NAME = "mInventory";

	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root;
		File database = new File("./data/all." + APP_NAME + ".mv.db");
		
		if(database.exists())
			root = FXMLLoader.load(getClass().getResource("/layout/AuthenticationLayout.fxml"));
		else
			root = FXMLLoader.load(getClass().getResource("/layout/SetPasswordLayout.fxml"));
		
		Scene scene = new Scene(root, 720, 640);
		primaryStage.setTitle(APP_NAME);
		primaryStage.setMinWidth(scene.getWidth());
		primaryStage.setMinHeight(scene.getHeight());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	@Override
	public void stop() {
		DBUtils.close();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
