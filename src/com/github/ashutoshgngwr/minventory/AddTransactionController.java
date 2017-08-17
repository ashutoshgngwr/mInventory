package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;
import java.util.List;

import com.github.ashutoshgngwr.minventory.controls.AutoCompleteTextField;
import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.models.Item;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class AddTransactionController {

	@FXML
	private GridPane addTransactionTabPage;
	@FXML
	private TextField toFromField, quantityField;
	@FXML
	private AutoCompleteTextField productNameField;
	@FXML
	private Label toFromLabel;
	@FXML
	private ToggleGroup tradeTypeToggle;

	int tradeType = 0;

	@FXML
	public void initialize() {
		tradeTypeToggle.getToggles().get(0).setSelected(true);
		tradeTypeToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (tradeType == 0) {
					tradeType = 1;
					toFromLabel.setText("Bought From:");
					toFromField.setPromptText("Who sold this product?");
					quantityField.setPromptText("How much items did you buy?");
				} else {
					tradeType = 0;
					toFromLabel.setText("Sold To:");
					toFromField.setPromptText("Who bought this product?");
					quantityField.setPromptText("How much items did they buy?");
				}
			}
		});
	}

	@FXML
	public void saveTransaction() throws SQLException {
		String productName = productNameField.getText().trim(), toFrom = toFromField.getText(), errorMsg = null;
		Item selectedItem = productNameField.getSelectedItem();
		int quantity = 0;

		try {
			quantity = (tradeType == 0 ? -1 : 1) * Integer.valueOf(quantityField.getText());
		} catch (NumberFormatException e) {
			errorMsg = "Invalid quantity field value! Only numeric values are allowed.";
			e.printStackTrace();
		}

		if (selectedItem == null) {
			List<Item> items = DBUtils.searchProduct(productName);

			if (items.size() == 1 && items.get(0).getName().equalsIgnoreCase(productName))
				selectedItem = items.get(0);
		}

		if (errorMsg == null && tradeType == 0) {
			if (selectedItem == null)
				errorMsg = (productName.isEmpty() ? "Item" : productName) + " does not exists in inventory!";
			else if (selectedItem.getQuantity() == 0)
				errorMsg = productName + " is out of stock!";
			else if (selectedItem.getQuantity() < quantity)
				errorMsg = "Only " + selectedItem.getQuantity() + " items of this type are available in inventory.";
		}

		if (errorMsg == null) {
			try {
				if (selectedItem == null)
					selectedItem = DBUtils.addItem(productName);

				DBUtils.addLog(selectedItem.getId(), toFrom, quantity);
			} catch (SQLException e) {
				e.printStackTrace();
				errorMsg = "An internal error occurred! Please restart " + Main.APP_NAME + ".";
			}
		}

		if (errorMsg != null) {
			Infotip.showError(productNameField, errorMsg);
		} else {
			Infotip.showSuccess(productNameField, "Transaction record added successfully.");
			resetForm();
		}
	}

	private void resetForm() {
		productNameField.setText("");
		tradeTypeToggle.getToggles().get(0).setSelected(true);
		toFromField.setText("");
		quantityField.setText("");
	}
}
