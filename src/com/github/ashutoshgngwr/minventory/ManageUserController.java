package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.controls.ChangePasswordDialog;
import com.github.ashutoshgngwr.minventory.controls.CreateUserDialog;
import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.database.User;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class ManageUserController {

	@FXML
	private Button addUserButton, deleteUserButton, changePasswordButton;
	private ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
	private CreateUserDialog createUserDialog = new CreateUserDialog();
	@FXML
	private VBox manageUserPage;

	@FXML
	private TableColumn<User, String> usernameColumn, privilegesColumn;
	@FXML
	private TableView<User> userTable;
	
	private DatabaseHandler dbHandler = DatabaseHandler.getInstance();

	@FXML
	public void addUser() {
		createUserDialog.showAndWait();

		if (createUserDialog.getResult() != ButtonType.FINISH)
			return;

		String errorMsg;
		if ((errorMsg = createUserDialog.validateInput()) != null) {
			Infotip.showError(addUserButton, errorMsg);
			createUserDialog.resetPasswordFields();
			return;
		}

		try {
			dbHandler.create(new User(createUserDialog.getUsername(), createUserDialog.getPassword(),
					createUserDialog.getAccessLevel()));
			Infotip.showSuccess(addUserButton, "User added successfully.");
			loadUsers();
			createUserDialog.reset();
		} catch (SQLException e) {
			Infotip.showInternalError(addUserButton);
		}
	}

	@FXML
	public void changePassword() {
		changePasswordDialog.showAndWait();

		if (changePasswordDialog.getResult() != ButtonType.APPLY)
			return;

		if (!changePasswordDialog.matchPasswords()) {
			Infotip.showError(changePasswordButton, "Passwords did not match!");
			return;
		}

		try {
			if (changePasswordDialog.getNewPassword().length() < 8)
				Infotip.showError(changePasswordButton, "Passwords should be atleast 8 characters long.");
			else {
				Main.user.setPassword(changePasswordDialog.getNewPassword());
				dbHandler.update(Main.user);
				Infotip.showSuccess(changePasswordButton, "Password was changed successfully!");
			}
		} catch (SQLException e) {
			Infotip.showInternalError(changePasswordButton);
		}

		changePasswordDialog.reset();
	}

	@FXML
	public void deleteSelected() {
		User user = userTable.getSelectionModel().getSelectedItem();
		userTable.getSelectionModel().clearSelection();

		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to delete user '" + user.getUsername() + "'?",
				ButtonType.YES, ButtonType.NO);
		alert.showAndWait();

		if (alert.getResult() != ButtonType.YES)
			return;

		try {
			this.dbHandler.delete(user);
			userTable.getItems().remove(user);
		} catch (SQLException e) {
			Infotip.showInternalError(deleteUserButton);
		}
	}

	@FXML
	public void initialize() {
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
		usernameColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.498));

		privilegesColumn.setCellValueFactory(new PropertyValueFactory<>("privileges"));
		privilegesColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.498));

		addUserButton.setDisable(!Main.user.isAdmin());

		userTable.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
			boolean disabled = newValue == null;
			deleteUserButton.setDisable(disabled || newValue.isAdmin());
			changePasswordButton.setDisable(disabled || Main.user.getId() != newValue.getId());
		});

		this.loadUsers();
	}

	private void loadUsers() {
		try {
			userTable.getItems().clear();
			userTable.getItems().addAll(this.dbHandler.listAllUsers());
			userTable.refresh();
		} catch (SQLException e) {
			Infotip.showInternalError(addUserButton);
		}
	}
}
