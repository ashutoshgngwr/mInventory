package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;
import java.util.List;

import com.github.ashutoshgngwr.minventory.controls.AutoCompleteTextField;
import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.database.Product;
import com.github.ashutoshgngwr.minventory.database.Transaction;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class AddTransactionController {

	int tradeType = 0;
	@FXML
	private GridPane addTransactionPage;
	@FXML
	private AutoCompleteTextField productNameField;
	@FXML
	private TextField traderField, quantityField;
	@FXML
	private Label traderLabel;

	@FXML
	private ToggleGroup tradeTypeToggle;
	
	private DatabaseHandler dbHandler = DatabaseHandler.getInstance();

	@FXML
	public void initialize() {
		tradeTypeToggle.getToggles().get(0).setSelected(true);
		tradeTypeToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (tradeType == 0) {
					tradeType = 1;
					traderLabel.setText("Bought From:");
					traderField.setPromptText("Who sold this product?");
					quantityField.setPromptText("How much items did you buy?");
				} else {
					tradeType = 0;
					traderLabel.setText("Sold To:");
					traderField.setPromptText("Who bought this product?");
					quantityField.setPromptText("How much items did they buy?");
				}
			}
		});
	}

	@FXML
	public void saveTransaction() throws SQLException {
		String productName = this.productNameField.getText(), trader = this.traderField.getText();
		Product selectedProduct = this.productNameField.getSelectedProduct();
		long quantity = 0;

		if (productName == null || productName.isEmpty()) {
			Infotip.showError(productNameField, "Product Name can not be left blank!");
			return;
		}
		
		productName = productName.trim();
		try {
			quantity = Integer.valueOf(quantityField.getText());
			if(quantity < 0)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			Infotip.showError(productNameField, "Invalid quantity field value! Only positive numeric values are allowed.");
			return;
		}
		
		if (this.tradeType == 0) {
			if (selectedProduct == null) {
				List<Product> products = dbHandler.searchProduct(productName);

				if (products.size() != 1 || products.get(0).getName().equalsIgnoreCase(productName)) {
					Infotip.showError(productNameField, productName + " does not exist in inventory!");
					return;
				}
				
				selectedProduct = products.get(0);
			}
			
			if (selectedProduct.getQuantity() == 0 || selectedProduct.getQuantity() < quantity) {
				Infotip.showError(productNameField, productName + ": Too few items left in stock!");
				return;
			}
			
			quantity = -quantity;
		}

		try {
			if (selectedProduct == null) {
				selectedProduct = new Product(productName, quantity);
				this.dbHandler.create(selectedProduct);
			} else {
				selectedProduct.setQuantity(selectedProduct.getQuantity() + quantity);
				this.dbHandler.update(selectedProduct);
			}

			this.dbHandler.create(new Transaction(selectedProduct.getId(), quantity,
					trader == null ? "" : trader, Main.user.getUsername()));
		} catch (SQLException e) {
			e.printStackTrace();
			Infotip.showInternalError(productNameField);
			return;
		}

		Infotip.showSuccess(productNameField, "Transaction record added successfully.");
		resetForm();
	}

	private void resetForm() {
		productNameField.setText(null);
		traderField.setText(null);
		quantityField.setText(null);
	}
}
