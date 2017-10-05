package com.github.ashutoshgngwr.minventory;

import java.io.IOException;
import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.util.Animation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class AuthenticationController {

	@FXML
	private GridPane formContainer;
	@FXML
	private PasswordField passwordField;
	@FXML
	private GridPane root;
	@FXML
	private TextField usernameField;
	@FXML
	private Text welcomeText;

	@FXML
	public void authenticate(ActionEvent event) {
		try {
			DatabaseHandler dbHandler = DatabaseHandler.connect(usernameField.getText().trim(),
					passwordField.getText().trim());
			Main.user = dbHandler.getCurrentUser(usernameField.getText().trim());

			if (Main.user.getAccessLevel() > 1)
				dbHandler.cleanTables();

			final Parent parent = FXMLLoader.load(getClass().getResource("/layout/MainLayout.fxml"));

			new Animation().slideOutRight(welcomeText, 600).fadeOut(welcomeText, 650).fadeOut(formContainer, 650)
					.onFinish((ev3nt) -> {
						formContainer.getScene().setRoot(parent);
					}).play();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Infotip.showError(usernameField, "Internal error occured! Please close this window.");
		} catch (SQLException e) {
			e.printStackTrace();
			Infotip.showError(usernameField, "Invalid username/password combination!");
		} catch (IOException e) {
			e.printStackTrace();
			Infotip.showError(usernameField, "Internal error occured! Please close this window.");
		}
	}

	@FXML
	public void initialize() {
		new Animation().fadeIn(root, 150).play();
	}
}
