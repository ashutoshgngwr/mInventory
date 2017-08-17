package com.github.ashutoshgngwr.minventory.controls;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class CreateUserDialog extends Dialog<ButtonType> {

	private TextField usernameField;
	private PasswordField newPasswordField, confirmPasswordField;
	private ToggleGroup accessLevelSelector;

	public CreateUserDialog() {
		super();
		getDialogPane().getButtonTypes().addAll(ButtonType.FINISH, ButtonType.CANCEL);
		getDialogPane().getStylesheets().add(getClass().getResource("/style/common.css").toExternalForm());
		setTitle("Create user");

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("root");
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		usernameField = new TextField();
		usernameField.setPrefWidth(250);
		usernameField.setPromptText("Your new username");

		newPasswordField = new PasswordField();
		newPasswordField.setPromptText("Your new password");

		confirmPasswordField = new PasswordField();
		confirmPasswordField.setPromptText("Confirm your new password");

		accessLevelSelector = new ToggleGroup();
		RadioButton level0 = new RadioButton("View/Add"), level1 = new RadioButton("View/Add/Edit"),
				level2 = new RadioButton("View/Add/Edit/Delete");

		level0.setSelected(true);
		level0.setUserData(0);
		level1.setUserData(1);
		level2.setUserData(2);

		accessLevelSelector.getToggles().addAll(level0, level1, level2);

		gridPane.setPadding(new Insets(20, 20, 20, 20));
		gridPane.add(new Label("Username: "), 0, 0);
		gridPane.add(usernameField, 1, 0);
		gridPane.add(new Label("New Password: "), 0, 1);
		gridPane.add(newPasswordField, 1, 1);
		gridPane.add(new Label("Confirm Password: "), 0, 2);
		gridPane.add(confirmPasswordField, 1, 2);
		gridPane.add(new Label("User Privileges"), 0, 3);
		gridPane.add(level0, 1, 3);
		gridPane.add(level1, 1, 4);
		gridPane.add(level2, 1, 5);

		getDialogPane().setContent(gridPane);

		setResultConverter((button) -> {
			return button;
		});
	}

	public String validateInput() {
		String username = usernameField.getText().trim(), password = newPasswordField.getText().trim(),
				confirmPassword = confirmPasswordField.getText().trim();

		if (username.length() < 4)
			return "Username must be atleast 4 characters long!";
		else if (!username.matches("^[a-zA-Z0-9_]+$"))
			return "Username can only contain alphanumeric and underscore characters.";
		else if (password.length() < 8)
			return "Password must be atleast 8 characters long!";
		else if (!password.equals(confirmPassword))
			return "Passwords do not match!";

		return null;
	}

	public String getUsername() {
		return usernameField.getText().trim();
	}

	public String getPassword() {
		return newPasswordField.getText().trim();
	}

	public int getAccessLevel() {
		return (Integer) accessLevelSelector.getSelectedToggle().getUserData();
	}

	public void reset() {

	}

	public void resetPasswordFields() {

	}
}
