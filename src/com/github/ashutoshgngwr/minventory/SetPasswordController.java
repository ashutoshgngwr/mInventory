package com.github.ashutoshgngwr.minventory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.util.AnimationUtils;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
	private Text welcomeText;
	@FXML
	private TextField newUsernameField, businessNameField;
	@FXML
	private PasswordField newPasswordField, confirmPasswordField;

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
				Class.forName("org.h2.Driver");
				DBUtils.setConnection(
						DriverManager.getConnection("jdbc:h2:./data/all." + Main.APP_NAME, username, password));

				DBUtils.createTables();
				DBUtils.setAdminUserPrivileges();
				Main.user = DBUtils.getCurrentUser(username);

				File propertiesFile = new File(Main.PROPERTIES_FILE_NAME);
				propertiesFile.createNewFile();

				Main.properties.setProperty(Main.PROPERTY_BUSINESS_NAME, businessName);
				Main.properties.store(new FileOutputStream(propertiesFile),
						"####### AUTO-GENERATED FILE ########\n######## DO NOT MODIFY ########");

				AnimationUtils.animateWelcome(welcomeText, formContainer, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							Parent parent = FXMLLoader.load(getClass().getResource("/layout/MainLayout.fxml"));
							formContainer.getScene().setRoot(parent);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				errorMsg = "Internal error occured! Please close this window.";
			} catch (SQLException | IOException e) {
				e.printStackTrace();
				errorMsg = "Unable to write data on hard disk.";
			}
		}

		if (errorMsg != null) {
			Infotip.showError(businessNameField, errorMsg);
		}
	}
}
