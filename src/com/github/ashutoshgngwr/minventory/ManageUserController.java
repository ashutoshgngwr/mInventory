package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.controls.ChangePasswordDialog;
import com.github.ashutoshgngwr.minventory.controls.CreateUserDialog;
import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.models.User;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

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
	private VBox manageUserTabPage;
	@FXML
	private Button addButton, deleteButton, passwordButton;
	@FXML
	private TableView<User> userTable;
	@FXML
	private TableColumn<User, String> usernameColumn, privilegesColumn;

	private ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
	private CreateUserDialog createUserDialog = new CreateUserDialog();

	@FXML
	public void initialize() {
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
		usernameColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.498));

		privilegesColumn.setCellValueFactory(new PropertyValueFactory<>("privileges"));
		privilegesColumn.prefWidthProperty().bind(userTable.widthProperty().multiply(0.499));

		addButton.setDisable(Main.user.getAccessLevel() < 3);

		userTable.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
			boolean disabled = userTable.getSelectionModel().getSelectedItems().isEmpty();
			deleteButton.setDisable(Main.user.getAccessLevel() != 3 || disabled);
			passwordButton.setDisable(newValue != null && Main.user.getId() != newValue.getId() || disabled);
		});

		loadUsers();
	}

	private void loadUsers() {
		try {
			userTable.getItems().clear();
			userTable.getItems().addAll(DBUtils.listUsers());
		} catch (SQLException e) {
			Infotip.showInternalError(addButton);
		}
	}

	@FXML
	public void addUser() {
		createUserDialog.showAndWait();

		if (createUserDialog.getResult() != ButtonType.FINISH)
			return;

		String errorMsg;
		if ((errorMsg = createUserDialog.validateInput()) != null) {
			Infotip.showError(addButton, errorMsg);
			createUserDialog.resetPasswordFields();
			return;
		}

		try {
			DBUtils.createUser(createUserDialog.getUsername(), createUserDialog.getPassword(),
					createUserDialog.getAccessLevel());
			Infotip.showSuccess(addButton, "User added successfully.");
			loadUsers();
		} catch (SQLException e) {
			e.printStackTrace();
			Infotip.showInternalError(addButton);
		}
	}

	@FXML
	public void changePassword() {
		changePasswordDialog.showAndWait();

		if (changePasswordDialog.getResult() != ButtonType.APPLY)
			return;

		if (!changePasswordDialog.matchPasswords()) {
			Infotip.showError(passwordButton, "Passwords did not match!");
			return;
		}

		try {
			if (changePasswordDialog.getNewPassword().length() < 8)
				Infotip.showError(passwordButton, "Passwords should be atleast 8 characters long.");
			else {
				DBUtils.changePassword(changePasswordDialog.getNewPassword());
				Infotip.showSuccess(passwordButton, "Password was changed successfully!");
			}
		} catch (SQLException e) {
			Infotip.showInternalError(passwordButton);
			e.printStackTrace();
		}

		changePasswordDialog.reset();
	}

	@FXML
	public void deleteSelected() {
		User user = userTable.getSelectionModel().getSelectedItem();
		userTable.getSelectionModel().clearSelection();

		if (user.isAdmin()) {
			Infotip.showError(deleteButton, "Admin user can not be deleted!");
			return;
		}

		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to delete user '" + user.getUsername() + "'?",
				ButtonType.YES, ButtonType.NO);
		alert.showAndWait();

		if (alert.getResult() != ButtonType.YES)
			return;

		try {
			DBUtils.deleteUser(user.getUsername());
			userTable.getItems().remove(user);
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}
}
