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

public class SetPasswordController {

	@FXML
	private GridPane formContainer;
	@FXML
	private Text welcomeText;
	@FXML
	private Text errorText;
	@FXML
	private TextField newUsernameField;
	@FXML
	private PasswordField newPasswordField;
	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	public void saveAuthenticationDetails(ActionEvent event) {
		String username = newUsernameField.getText().trim(), password = newPasswordField.getText().trim(),
				confirmPassword = confirmPasswordField.getText().trim(), errorMsg = null;

		if (username.equals("") || username.length() < 4)
			errorMsg = "Username must be atleast 4 characters long!";
		else if (password.equals("") || password.length() < 8)
			errorMsg = "Password must be atleast 8 characters long!";
		else if (!password.equals(confirmPassword))
			errorMsg = "Passwords do not match!";
		
		if(errorMsg == null) {
			try {
				Class.forName("org.h2.Driver");
				DBUtils.setConnection(DriverManager.getConnection("jdbc:h2:./data/all."
						+ Main.APP_NAME, username, password));
				
				AnimationUtils.animateWelcome(welcomeText, formContainer,
						new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						
					}
				});
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				errorMsg = "Internal error occured! Please close this window.";
			} catch (SQLException e) {
				e.printStackTrace();
				errorMsg = "Unable to write data on hard disk.";
			}
		}
		
		errorText.setText(errorMsg);
	}
}
