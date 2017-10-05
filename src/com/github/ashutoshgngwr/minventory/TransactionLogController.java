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

public class TransactionLogController {

	@FXML
	private Button deleteButton;
	@FXML
	private VBox logPage;
	@FXML
	private TableView<Transaction> logTable;
	private int offset = 0, limit = 50;
	@FXML
	private TableColumn<Transaction, Long> quantityColumn;
	@FXML
	private TableColumn<Transaction, String> traderColumn, nameColumn, timeColumn, tradeTypeColumn, userColumn;

	private DatabaseHandler dbHandler = DatabaseHandler.getInstance();

	@FXML
	public void deleteSelected() {
		Alert alert = new Alert(AlertType.CONFIRMATION, "Do you want to delete selected rows?", ButtonType.YES,
				ButtonType.NO);
		alert.showAndWait();

		if (alert.getResult() != ButtonType.YES) {
			this.logTable.getSelectionModel().clearSelection();
			return;
		}
		try {
			for (Transaction transaction : this.logTable.getSelectionModel().getSelectedItems()) {
				Product affectedProduct = this.dbHandler.getProduct(transaction.getProductId());
				affectedProduct.setQuantity(affectedProduct.getQuantity() - transaction.getQuantity());
				this.dbHandler.update(affectedProduct);
				this.dbHandler.delete(transaction);
				this.logTable.getItems().remove(transaction);
			}

			this.logTable.getSelectionModel().clearSelection();
		} catch (SQLException e) {
			Infotip.showInternalError(this.deleteButton);
			e.printStackTrace();
		}
	}

	@FXML
	public void initialize() {
		if (Main.user.getAccessLevel() > 0) {
			this.logTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			this.logTable.setEditable(true);

			if (Main.user.getAccessLevel() > 1) {
				this.logTable.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
					this.deleteButton.setDisable(this.logTable.getSelectionModel().getSelectedItems().isEmpty());
				});
			}
		}
		this.nameColumn.setCellValueFactory(new PropertyValueFactory<Transaction, String>("productName"));
		this.nameColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.23));

		this.traderColumn.setCellValueFactory(new PropertyValueFactory<Transaction, String>("trader"));
		this.traderColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.23));
		this.traderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		this.traderColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, String>>() {

			@Override
			public void handle(CellEditEvent<Transaction, String> event) {
				try {
					Transaction transaction = event.getRowValue();
					transaction.setTrader(event.getNewValue());
					dbHandler.update(transaction);
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		this.timeColumn.setCellValueFactory(new PropertyValueFactory<Transaction, String>("time"));
		this.timeColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.23));

		this.tradeTypeColumn.setCellValueFactory(new PropertyValueFactory<Transaction, String>("tradeType"));
		this.tradeTypeColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.1));

		this.userColumn.setCellValueFactory(new PropertyValueFactory<Transaction, String>("user"));
		this.tradeTypeColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.1));

		this.quantityColumn.setCellValueFactory(new PropertyValueFactory<Transaction, Long>("quantity"));
		this.quantityColumn.prefWidthProperty().bind(this.logTable.widthProperty().multiply(0.1));
		this.quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
		this.quantityColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Transaction, Long>>() {

			@Override
			public void handle(CellEditEvent<Transaction, Long> event) {
				try {
					Transaction transaction = event.getRowValue();
					Product affectedProduct = dbHandler.getProduct(transaction.getProductId());
					if (event.getNewValue() == null) {
						transaction.setQuantity(event.getOldValue());
						return;
					}
					affectedProduct.setQuantity(affectedProduct.getQuantity() + event.getNewValue() - event.getOldValue());
					transaction.setQuantity(event.getNewValue());
					dbHandler.update(affectedProduct);
					dbHandler.update(transaction);
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		this.logPage.setUserData(new OnTabChangeListener() {
			@Override
			public void onTabChanged() {
				offset = 0;
				logTable.getItems().clear();
				addData();
			}
		});

		this.addData();
		this.logTable.getItems().addListener(new ListChangeListener<Transaction>() {
			@Override
			public void onChanged(Change<? extends Transaction> c) {
				ScrollBar sBar = (ScrollBar) logTable.lookup(".scroll-bar:vertical");

				if (sBar == null)
					return;

				sBar.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue.doubleValue() == sBar.getMax())
						addData();
				});
				logTable.getItems().removeListener(this);
			}
		});
	}

	private void addData() {
		if (this.offset == -1)
			return;

		try {
			List<Transaction> transactions = this.dbHandler.listTransactions(this.offset, this.limit);
			this.logTable.getItems().addAll(transactions);
			this.offset = transactions.size() < this.limit ? -1 : this.offset + this.limit;
		} catch (SQLException e) {
			Infotip.showInternalError(this.deleteButton);
			e.printStackTrace();
		}
	}
}
