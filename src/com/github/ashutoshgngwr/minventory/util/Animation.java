package com.github.ashutoshgngwr.minventory.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Animation {
	
	ParallelTransition parallelTransition = new ParallelTransition();
	
	public Animation fadeIn(Node node, int duration) {
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		parallelTransition.getChildren().add(fadeTransition);
		return this;
	}

	public Animation fadeOut(Node node, long duration) {
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		parallelTransition.getChildren().add(fadeTransition);
		return this;
	}
	
	public Animation slideInRight(Node node, long duration) {
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
		translateTransition.setFromX(node.getScene().getWidth());
		translateTransition.setToX(0);
		parallelTransition.getChildren().add(translateTransition);
		return this;
	}
	
	public Animation slideInLeft(Node node, long duration) {
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
		translateTransition.setFromX(-node.getLayoutBounds().getWidth());
		translateTransition.setToX(0);
		parallelTransition.getChildren().add(translateTransition);
		return this;
	}
	
	public Animation slideOutRight(Node node, long duration) {
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
		translateTransition.setToX(node.getScene().getWindow().getWidth());
		parallelTransition.getChildren().add(translateTransition);
		return this;
	}
	
	public Animation slideOutLeft(Node node, long duration) {
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
		translateTransition.setToX(-node.getLayoutBounds().getWidth());
		parallelTransition.getChildren().add(translateTransition);
		return this;
	}
	
	public Animation onFinish(EventHandler<ActionEvent> eventHandler) {
		parallelTransition.setOnFinished(eventHandler);
		return this;
	}
	
	public void play() {
		parallelTransition.play();
	}
}
