package com.github.ashutoshgngwr.minventory;

import java.io.IOException;

import com.github.ashutoshgngwr.minventory.controls.OnTabChangeListener;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

public class MainController {

	@FXML
	private Text usernameText, businessNameText;
	@FXML
	private TabPane tabPane;
	@FXML
	private Tab viewInventoryTabPage, logTabPage, addTransactionTabPage, manageUserTabPage;

	@FXML
	public void initialize() {
		businessNameText.setText(Main.properties.getProperty(Main.PROPERTY_BUSINESS_NAME));
		usernameText.setText(Main.user.getUsername());

		tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			if (newTab.getContent().getUserData() != null)
				((OnTabChangeListener) newTab.getContent().getUserData()).onTabChanged();
		});
	}

	@FXML
	public void logout() {
		DBUtils.close();
		try {
			usernameText.getScene()
					.setRoot(FXMLLoader.load(getClass().getResource("/layout/AuthenticationLayout.fxml")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
