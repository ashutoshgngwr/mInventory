package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;
import java.util.List;

import com.github.ashutoshgngwr.minventory.MainController.OnTabChangeListener;
import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.database.DatabaseHandler;
import com.github.ashutoshgngwr.minventory.database.Product;
import com.github.ashutoshgngwr.minventory.database.Transaction;

import javafx.collections.ListChangeListener;
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
import javafx.util.converter.LongStringConverter;

public class InventoryController {

	@FXML
	private Button deleteButton;
	@FXML
	private VBox inventoryPage;
	@FXML
	private TableView<Product> inventoryTable;
	@FXML
	private TableColumn<Product, String> nameColumn;
	@FXML
	private TableColumn<Product, Long> snColumn, quantityColumn;

	private int offset = 0, limit = 50;
	private DatabaseHandler dbHandler = DatabaseHandler.getInstance();

	@FXML
	public void deleteSelected() {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to delete selected rows?", ButtonType.YES,
				ButtonType.NO);
		alert.showAndWait();

		if (alert.getResult() != ButtonType.YES) {
			inventoryTable.getSelectionModel().clearSelection();
			return;
		}

		DatabaseHandler dbHandler = DatabaseHandler.getInstance();
		try {
			for (Product product : inventoryTable.getSelectionModel().getSelectedItems()) {
				dbHandler.delete(product);
				dbHandler.deleteAllTransactionsFor(product.getId());
			}

			inventoryTable.getItems().removeAll(inventoryTable.getSelectionModel().getSelectedItems());
			inventoryTable.getSelectionModel().clearSelection();
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}

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

		snColumn.setCellValueFactory(new PropertyValueFactory<Product, Long>("id"));
		snColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.17));

		nameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
		nameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.4));

		nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Product, String>>() {
			@Override
			public void handle(CellEditEvent<Product, String> event) {
				if (event.getNewValue().isEmpty()) {
					inventoryTable.refresh();
					return;
				}
				try {
					Product product = event.getRowValue();
					product.setName(event.getNewValue());
					dbHandler.update(product);
				} catch (SQLException e) {
					Infotip.showError(deleteButton,
							"Another product with name '" + event.getNewValue() + "' already exists in inventory.");
					e.printStackTrace();
				}
			}
		});

		quantityColumn.setCellValueFactory(new PropertyValueFactory<Product, Long>("quantity"));
		quantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.4));
		quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
		quantityColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Product, Long>>() {

			@Override
			public void handle(CellEditEvent<Product, Long> event) {
				try {
					Product product = event.getRowValue();
					if (product.getQuantity() != event.getNewValue())
						dbHandler.create(new Transaction(product.getId(), event.getNewValue() - event.getOldValue(),
								"--Edited directly from Inventory--", Main.user.getUsername()));
					
					product.setQuantity(event.getNewValue() == null ? 0 : event.getNewValue().longValue());
					dbHandler.update(product);
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		addData();
		inventoryPage.setUserData(new OnTabChangeListener() {
			@Override
			public void onTabChanged() {
				offset = 0;
				inventoryTable.getItems().clear();
				addData();
			}
		});

		inventoryTable.getItems().addListener(new ListChangeListener<Product>() {
			@Override
			public void onChanged(Change<? extends Product> c) {
				ScrollBar sBar = (ScrollBar) inventoryTable.lookup(".scroll-bar:vertical");
				if (sBar == null)
					return;

				sBar.valueProperty().addListener((obs, oldValue, newValue) -> {
					if (newValue.doubleValue() >= sBar.getMax()) {
						addData();
					}
				});

				inventoryTable.getItems().removeListener(this);
			}
		});
	}

	private void addData() {
		if (offset == -1)
			return;

		try {
			List<Product> products = DatabaseHandler.getInstance().listProducts(offset, limit);
			inventoryTable.getItems().addAll(products);
			offset = products.size() < limit ? -1 : offset + limit;
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}
}