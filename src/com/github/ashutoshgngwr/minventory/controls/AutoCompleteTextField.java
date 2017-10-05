package com.github.ashutoshgngwr.minventory.controls;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.database.Product;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AutoCompleteTextField extends TextField {

	private class SearchThread extends Thread {

		private String searchString;
		private volatile boolean shouldStop = false;

		private SearchThread(String searchString) {
			this.searchString = searchString;
		}

		@Override
		public void run() {
			try {
				suggestions = DatabaseHandler.getInstance().searchProduct(searchString);

				if (shouldStop)
					return;

				Platform.runLater(() -> {
					populateSuggestions();
				});
			} catch (SQLException ignore) {
				ignore.printStackTrace();
			}
		}
	}
	private ContextMenu suggestionsPopup;
	private List<Product> suggestions;
	private SearchThread searchThread;
	private Product selectedProduct = null;

	private boolean ignoreTextChange = false;

	public AutoCompleteTextField() {
		super();
		this.suggestionsPopup = new ContextMenu();
		this.suggestionsPopup.setPrefWidth(getWidth());

		textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (ignoreTextChange)
					return;

				if (searchThread != null)
					searchThread.shouldStop = true;

				if (newValue == null || newValue.isEmpty())
					return;

				searchThread = new SearchThread(newValue.trim());
				searchThread.start();
			}
		});
	}

	public Product getSelectedProduct() {
		return this.selectedProduct;
	}

	public void populateSuggestions() {
		this.suggestionsPopup.getItems().clear();
		this.suggestionsPopup.hide();

		this.selectedProduct = null;

		if (this.suggestions == null)
			return;

		List<CustomMenuItem> menuItems = new ArrayList<>(3);

		for (Product product : this.suggestions) {
			Label suggestionLabel = new Label(product.getName());
			suggestionLabel.setStyle("-fx-text-fill: #000000");
			suggestionLabel.setPrefWidth(getWidth());
			CustomMenuItem menuItem = new CustomMenuItem(suggestionLabel, true);
			menuItem.setOnAction(event -> {
				this.selectedProduct = product;
				this.ignoreTextChange = true;
				setText(product.getName());
				this.ignoreTextChange = false;
			});

			menuItems.add(menuItem);
		}

		this.suggestionsPopup.getItems().addAll(menuItems);
		this.suggestionsPopup.show(this, Side.BOTTOM, 0, 0);
	}
}