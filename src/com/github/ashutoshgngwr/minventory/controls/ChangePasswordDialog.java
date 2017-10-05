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

		this.newPasswordField = new PasswordField();
		this.newPasswordField.setPromptText("Your new password");

		this.confirmPasswordField = new PasswordField();
		this.confirmPasswordField.setPromptText("Confirm your new password");

		gridPane.add(new Label("New Password: "), 0, 0);
		gridPane.add(this.newPasswordField, 1, 0);
		gridPane.add(new Label("Confirm Password: "), 0, 1);
		gridPane.add(this.confirmPasswordField, 1, 1);

		getDialogPane().setContent(gridPane);

		setResultConverter((button) -> {
			return button;
		});
	}

	public String getNewPassword() {
		return this.newPasswordField.getText().trim();
	}

	public boolean matchPasswords() {
		return this.newPasswordField.getText().trim().equals(confirmPasswordField.getText().trim());
	}

	public void reset() {
		this.newPasswordField.setText(null);
		this.confirmPasswordField.setText(null);
	}
}
