package com.github.ashutoshgngwr.minventory.controls;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

public class ChangePasswordDialog extends Dialog<ButtonType> {

	private PasswordField newPasswordField, confirmPasswordField;

	public ChangePasswordDialog() {
		super();
		getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
		getDialogPane().getStylesheets().add(getClass().getResource("/style/common.css").toExternalForm());
		setTitle("Change password");

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("root");
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		newPasswordField = new PasswordField();
		newPasswordField.setPromptText("Your new password");

		confirmPasswordField = new PasswordField();
		confirmPasswordField.setPromptText("Confirm your new password");

		gridPane.add(new Label("New Password: "), 0, 0);
		gridPane.add(newPasswordField, 1, 0);
		gridPane.add(new Label("Confirm Password: "), 0, 1);
		gridPane.add(confirmPasswordField, 1, 1);

		getDialogPane().setContent(gridPane);

		setResultConverter((button) -> {
			return button;
		});
	}

	public boolean matchPasswords() {
		return newPasswordField.getText().trim().equals(confirmPasswordField.getText().trim());
	}

	public String getNewPassword() {
		return newPasswordField.getText().trim();
	}

	public void reset() {
		newPasswordField.setText(null);
		confirmPasswordField.setText(null);
	}
}
