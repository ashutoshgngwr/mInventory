package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;
import java.util.List;

import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.controls.OnTabChangeListener;
import com.github.ashutoshgngwr.minventory.models.Item;
import com.github.ashutoshgngwr.minventory.util.DBUtils;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

public class ViewInventoryController {

	@FXML
	private VBox viewInventoryTabPage;
	@FXML
	private TableView<Item> inventoryTable;
	@FXML
	private TableColumn<Item, Integer> snColumn, quantityColumn;
	@FXML
	private TableColumn<Item, String> nameColumn;
	@FXML
	private Button deleteButton;

	private int offset = 0, limit = 20;

	@FXML
	public void initialize() {
		if (Main.user.getAccessLevel() > 0) {
			inventoryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			inventoryTable.setEditable(true);

			if (Main.user.getAccessLevel() > 1) {
				inventoryTable.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
					deleteButton.setDisable(inventoryTable.getSelectionModel().getSelectedItems().isEmpty());
				});
			}
		}

		snColumn.setCellValueFactory(new PropertyValueFactory<Item, Integer>("id"));
		snColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.197));

		nameColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
		nameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.4));

		nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Item, String>>() {
			@Override
			public void handle(CellEditEvent<Item, String> event) {
				if (event.getNewValue().isEmpty()) {
					inventoryTable.refresh();
					return;
				}
				try {
					Item item = event.getRowValue();
					item.setName(event.getNewValue());
					DBUtils.updateItemTable(item.getId(), "name", event.getNewValue());
				} catch (SQLException e) {
					Infotip.showError(deleteButton,
							"Another product with name '" + event.getNewValue() + "' already exists in inventory.");
					e.printStackTrace();
				}
			}
		});

		quantityColumn.setCellValueFactory(new PropertyValueFactory<Item, Integer>("quantity"));
		quantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.4));
		quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		quantityColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Item, Integer>>() {

			@Override
			public void handle(CellEditEvent<Item, Integer> event) {
				if (event.getNewValue() == null) {
					event.getRowValue().setQuantity(0);
					inventoryTable.refresh();
					return;
				}
				try {
					Item item = event.getRowValue();
					item.setQuantity(event.getNewValue());
					DBUtils.updateItemTable(item.getId(), "quantity", event.getNewValue().toString());
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		addData();
		ScrollBar sBar = (ScrollBar) inventoryTable.lookup(".scroll-bar:vertical");
		if (sBar != null)
			sBar.valueProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue.doubleValue() >= sBar.getMax()) {
					addData();
				}
			});

		viewInventoryTabPage.setUserData(new OnTabChangeListener() {
			@Override
			public void onTabChanged() {
				offset = 0;
				inventoryTable.getItems().clear();
				addData();
			}
		});
	}

	@FXML
	public void deleteSelected() {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to delete selected rows?", ButtonType.YES,
				ButtonType.NO);
		alert.showAndWait();

		if (alert.getResult() != ButtonType.YES) {
			inventoryTable.getSelectionModel().clearSelection();
			return;
		}
		try {
			for (Item item : inventoryTable.getSelectionModel().getSelectedItems()) {
				DBUtils.deleteRow("item", "id", String.valueOf(item.getId()));
				inventoryTable.getItems().remove(item);
			}

			inventoryTable.getSelectionModel().clearSelection();
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}

	private void addData() {
		if (offset == -1)
			return;

		try {
			List<Item> items = DBUtils.listItems(offset, limit);
			inventoryTable.getItems().addAll(items);
			offset = items.size() < limit ? -1 : offset + limit;
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}
}