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

		this.usernameField = new TextField();
		this.usernameField.setPrefWidth(250);
		this.usernameField.setPromptText("Your new username");

		this.newPasswordField = new PasswordField();
		this.newPasswordField.setPromptText("Your new password");

		this.confirmPasswordField = new PasswordField();
		this.confirmPasswordField.setPromptText("Confirm your new password");

		this.accessLevelSelector = new ToggleGroup();
		RadioButton level0 = new RadioButton("View/Add"), level1 = new RadioButton("View/Add/Edit"),
				level2 = new RadioButton("View/Add/Edit/Delete");

		level0.setSelected(true);
		level0.setUserData(0);
		level1.setUserData(1);
		level2.setUserData(2);

		this.accessLevelSelector.getToggles().addAll(level0, level1, level2);

		gridPane.setPadding(new Insets(20, 20, 20, 20));
		gridPane.add(new Label("Username: "), 0, 0);
		gridPane.add(this.usernameField, 1, 0);
		gridPane.add(new Label("New Password: "), 0, 1);
		gridPane.add(this.newPasswordField, 1, 1);
		gridPane.add(new Label("Confirm Password: "), 0, 2);
		gridPane.add(this.confirmPasswordField, 1, 2);
		gridPane.add(new Label("User Privileges"), 0, 3);
		gridPane.add(level0, 1, 3);
		gridPane.add(level1, 1, 4);
		gridPane.add(level2, 1, 5);

		getDialogPane().setContent(gridPane);

		setResultConverter((button) -> {
			return button;
		});
	}

	public int getAccessLevel() {
		return (Integer) this.accessLevelSelector.getSelectedToggle().getUserData();
	}

	public String getPassword() {
		return this.newPasswordField.getText().trim();
	}

	public String getUsername() {
		return this.usernameField.getText().trim();
	}

	public void reset() {
		this.usernameField.setText(null);
		this.accessLevelSelector.selectToggle(this.accessLevelSelector.getToggles().get(0));
		this.resetPasswordFields();
	}

	public void resetPasswordFields() {
		this.newPasswordField.setText(null);
		this.confirmPasswordField.setText(null);
	}

	public String validateInput() {
		String username = this.usernameField.getText(),
				password = this.newPasswordField.getText(),
				confirmPassword = this.confirmPasswordField.getText();

		if (username == null || username.length() < 4)
			return "Username must be atleast 4 characters long!";
		else if (!username.matches("^[a-zA-Z0-9_]+$"))
			return "Username can only contain alphanumeric and underscore characters.";
		else if (password == null || password.length() < 8)
			return "Password must be atleast 8 characters long!";
		else if (confirmPassword == null || !password.equals(confirmPassword))
			return "Passwords do not match!";

		return null;
	}
}
