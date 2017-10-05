package com.github.ashutoshgngwr.minventory;

import java.io.IOException;
import java.sql.SQLException;

import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.util.Animation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainController {

	protected interface OnTabChangeListener {
		void onTabChanged();
	}

	@FXML
	private VBox root;
	@FXML
	private TabPane tabPane;
	@FXML
	private Text usernameText, businessNameText;

	@FXML
	private Tab viewInventoryPage, logPage, addTransactionPage, manageUserPage;

	@FXML
	public void initialize() {
		businessNameText.setText(Main.properties.getProperty(Main.PROPERTY_BUSINESS_NAME));
		usernameText.setText(Main.user.getUsername());

		tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			if (newTab.getContent().getUserData() != null)
				((OnTabChangeListener) newTab.getContent().getUserData()).onTabChanged();
		});

		new Animation().fadeIn(root, 200).play();
	}

	@FXML
	public void logout() throws SQLException {
		DatabaseHandler.getInstance().closeConnection();
		try {
			Parent parent = FXMLLoader.load(getClass().getResource("/layout/AuthenticationLayout.fxml"));

			new Animation().fadeOut(root, 150).onFinish((ev3nt) -> {
				this.usernameText.getScene().setRoot(parent);
			}).play();
		} catch (IOException e) {
			Infotip.showInternalError(usernameText);
			e.printStackTrace();
		}
	}
}
