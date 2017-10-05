package com.github.ashutoshgngwr.minventory.controls;

import com.github.ashutoshgngwr.minventory.Main;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Infotip extends Tooltip {

	public static final int TYPE_ERROR = 213;
	public static final int TYPE_SUCCESS = 923;

	public static void showError(Node node, String message) {
		new Infotip(node, TYPE_ERROR, message);
	}

	public static void showInternalError(Node node) {
		new Infotip(node, TYPE_ERROR, "Internal error occurred! Please restart " + Main.APP_NAME + ".");
	}

	public static void showSuccess(Node node, String message) {
		new Infotip(node, TYPE_SUCCESS, message);
	}

	private Infotip(Node node, int type, String message) {
		super();

		this.setAutoHide(true);
		this.setGraphic(type);
		this.setText(message);
		this.show(node.getScene().getWindow());
	}

	private void setGraphic(int type) {
		String iconPath = "/img/error-icon.png";
		switch (type) {
		case TYPE_ERROR:
			iconPath = "/img/error-icon.png";
			break;
		case TYPE_SUCCESS:
			iconPath = "/img/success-icon.png";
			break;
		}

		this.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(iconPath))));
	}
}
