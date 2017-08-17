package com.github.ashutoshgngwr.minventory.controls;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.github.ashutoshgngwr.minventory.models.Item;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AutoCompleteTextField extends TextField {

	private ContextMenu suggestionsPopup;
	private List<Item> suggestions;
	private SearchThread searchThread;
	private Item selectedItem = null;
	private boolean ignoreTextChange = false;

	public AutoCompleteTextField() {
		super();
		suggestionsPopup = new ContextMenu();
		suggestionsPopup.setPrefWidth(getWidth());

		textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (ignoreTextChange)
					return;

				if (searchThread != null)
					searchThread.shouldStop = true;

				if (newValue.isEmpty())
					return;

				searchThread = new SearchThread(newValue.trim());
				searchThread.start();
			}
		});
	}

	public void populateSuggestions() {
		suggestionsPopup.getItems().clear();
		suggestionsPopup.hide();

		selectedItem = null;

		if (suggestions == null)
			return;

		List<CustomMenuItem> menuItems = new LinkedList<>();

		for (Item item : suggestions) {
			Label suggestionLabel = new Label(item.getName());
			suggestionLabel.setStyle("-fx-text-fill: #000000");
			suggestionLabel.setPrefWidth(getWidth());
			CustomMenuItem menuItem = new CustomMenuItem(suggestionLabel, true);
			menuItem.setOnAction(event -> {
				selectedItem = item;
				ignoreTextChange = true;
				setText(item.getName());
				ignoreTextChange = false;
			});

			menuItems.add(menuItem);
		}

		suggestionsPopup.getItems().addAll(menuItems);
		suggestionsPopup.show(this, Side.BOTTOM, 0, 0);
	}

	public Item getSelectedItem() {
		return selectedItem;
	}

	private class SearchThread extends Thread {

		private String searchString;
		private volatile boolean shouldStop = false;

		private SearchThread(String searchString) {
			this.searchString = searchString;
		}

		@Override
		public void run() {
			try {
				suggestions = DBUtils.searchProduct(searchString);

				if (shouldStop)
					return;

				Platform.runLater(() -> {
					populateSuggestions();
				});
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}