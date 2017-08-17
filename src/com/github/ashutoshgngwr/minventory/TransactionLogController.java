package com.github.ashutoshgngwr.minventory;

import java.sql.SQLException;
import java.util.List;

import com.github.ashutoshgngwr.minventory.controls.Infotip;
import com.github.ashutoshgngwr.minventory.controls.OnTabChangeListener;
import com.github.ashutoshgngwr.minventory.models.LogItem;
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

public class TransactionLogController {

	@FXML
	private VBox logTabPage;
	@FXML
	private TableView<LogItem> logTable;
	@FXML
	private TableColumn<LogItem, String> toFromColumn, nameColumn, timeColumn, soldBoughtColumn;
	@FXML
	private TableColumn<LogItem, Integer> quantityColumn;
	@FXML
	private Button deleteButton;

	private int offset = 0, limit = 20;

	@FXML
	public void initialize() {
		if (Main.user.getAccessLevel() > 0) {
			logTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			logTable.setEditable(true);

			if (Main.user.getAccessLevel() > 1) {
				logTable.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
					deleteButton.setDisable(logTable.getSelectionModel().getSelectedItems().isEmpty());
				});
			}
		}
		nameColumn.setCellValueFactory(new PropertyValueFactory<LogItem, String>("name"));
		nameColumn.prefWidthProperty().bind(logTable.widthProperty().multiply(0.3));

		toFromColumn.setCellValueFactory(new PropertyValueFactory<LogItem, String>("toFrom"));
		toFromColumn.prefWidthProperty().bind(logTable.widthProperty().multiply(0.3));
		toFromColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		toFromColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LogItem, String>>() {

			@Override
			public void handle(CellEditEvent<LogItem, String> event) {
				try {
					LogItem item = event.getRowValue();
					item.setToFrom(event.getNewValue());
					DBUtils.updateLogTable(item.getDBTimestamp(), "to_from", event.getNewValue());
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		timeColumn.setCellValueFactory(new PropertyValueFactory<LogItem, String>("time"));
		timeColumn.prefWidthProperty().bind(logTable.widthProperty().multiply(0.2));

		soldBoughtColumn.setCellValueFactory(new PropertyValueFactory<LogItem, String>("soldBought"));
		soldBoughtColumn.prefWidthProperty().bind(logTable.widthProperty().multiply(0.097));

		quantityColumn.setCellValueFactory(new PropertyValueFactory<LogItem, Integer>("quantity"));
		quantityColumn.prefWidthProperty().bind(logTable.widthProperty().multiply(0.1));
		quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		quantityColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<LogItem, Integer>>() {

			@Override
			public void handle(CellEditEvent<LogItem, Integer> event) {
				if (event.getNewValue() == null) {
					event.getRowValue().setQuantity(0);
					logTable.refresh();
					return;
				}
				try {
					LogItem item = event.getRowValue();
					item.setQuantity(event.getNewValue());
					DBUtils.updateLogTable(item.getDBTimestamp(), "quantity", event.getNewValue().toString());
				} catch (SQLException e) {
					Infotip.showInternalError(deleteButton);
					e.printStackTrace();
				}
			}
		});

		addData();
		ScrollBar sBar = (ScrollBar) logTable.lookup(".scroll-bar:vertical");
		if (sBar != null)
			sBar.valueProperty().addListener((obs, oldValue, newValue) -> {
				if (newValue.doubleValue() >= sBar.getMax()) {
					addData();
				}
			});

		logTabPage.setUserData(new OnTabChangeListener() {
			@Override
			public void onTabChanged() {
				offset = 0;
				logTable.getItems().clear();
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
			logTable.getSelectionModel().clearSelection();
			return;
		}
		try {
			for (LogItem item : logTable.getSelectionModel().getSelectedItems()) {
				DBUtils.deleteRow("log", "time", item.getDBTimestamp());
				logTable.getItems().remove(item);
			}

			logTable.getSelectionModel().clearSelection();
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}

	private void addData() {

		if (offset == -1)
			return;

		try {
			List<LogItem> items = DBUtils.listLog(offset, limit);
			logTable.getItems().addAll(items);
			offset = items.size() < limit ? -1 : offset + limit;
		} catch (SQLException e) {
			Infotip.showInternalError(deleteButton);
			e.printStackTrace();
		}
	}
}
