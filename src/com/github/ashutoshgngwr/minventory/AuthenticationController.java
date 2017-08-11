package com.github.ashutoshgngwr.minventory;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.util.AnimationUtils;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class AuthenticationController {

	@FXML
	private GridPane formContainer;
	@FXML
	private Text welcomeText;
	@FXML
	private Text errorText;
	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;

	@FXML
	public void authenticate(ActionEvent event) {
		String errorMsg = null;
		try {
			Class.forName("org.h2.Driver");
			DBUtils.setConnection(DriverManager.getConnection("jdbc:h2:./data/all."
					+ Main.APP_NAME, usernameField.getText().trim(), passwordField.getText().trim()));
			
			AnimationUtils.animateWelcome(welcomeText, formContainer, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					
				}
			});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			errorMsg = "Internal error occured! Please close this window.";
		} catch (SQLException e) {
			e.printStackTrace();
			errorMsg = "Invalid username/password combination!";
		}
		
		errorText.setText(errorMsg);
	}
}
