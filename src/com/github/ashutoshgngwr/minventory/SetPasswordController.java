package com.github.ashutoshgngwr.minventory;

import java.io.File;
import java.io.FileOutputStream;
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

public class SetPasswordController {

	@FXML
	private GridPane formContainer;
	@FXML
	private PasswordField newPasswordField, confirmPasswordField;
	@FXML
	private TextField newUsernameField, businessNameField;
	@FXML
	private Text welcomeText;

	@FXML
	public void saveAuthenticationDetails(ActionEvent event) {
		String businessName = businessNameField.getText().trim(), username = newUsernameField.getText().trim(),
				password = newPasswordField.getText().trim(), confirmPassword = confirmPasswordField.getText().trim(),
				errorMsg = null;

		if (businessName.isEmpty())
			errorMsg = "Business name field can not be left blank.";
		else if (username.length() < 4)
			errorMsg = "Username must be atleast 4 characters long!";
		else if (!username.matches("^[a-zA-Z0-9_]+$"))
			errorMsg = "Username can only contain alphanumeric and underscore characters.";
		else if (password.length() < 8)
			errorMsg = "Password must be atleast 8 characters long!";
		else if (!password.equals(confirmPassword))
			errorMsg = "Passwords do not match!";

		if (errorMsg == null) {
			try {
				DatabaseHandler dbHandler = DatabaseHandler.connect(username, password);
				dbHandler.setAdminUserPrivileges();
				Main.user = dbHandler.getCurrentUser(username);

				File propertiesFile = new File(Main.PROPERTIES_FILE_NAME);
				propertiesFile.createNewFile();

				Main.properties.setProperty(Main.PROPERTY_BUSINESS_NAME, businessName);
				Main.properties.store(new FileOutputStream(propertiesFile),
						"####### AUTO-GENERATED FILE ########\n######## DO NOT MODIFY ########");

				final Parent parent = FXMLLoader.load(getClass().getResource("/layout/MainLayout.fxml"));
				new Animation().slideOutRight(welcomeText, 600).fadeOut(welcomeText, 650).fadeOut(formContainer, 650)
						.onFinish((ev3nt) -> {
							formContainer.getScene().setRoot(parent);
						}).play();
			} catch (ClassNotFoundException e) {
				Infotip.showInternalError(businessNameField);
				return;
			} catch (SQLException | IOException e) {
				errorMsg = "Unable to write data on hard disk.";
			}
		}

		if (errorMsg != null) {
			Infotip.showError(businessNameField, errorMsg);
		}
	}
}
