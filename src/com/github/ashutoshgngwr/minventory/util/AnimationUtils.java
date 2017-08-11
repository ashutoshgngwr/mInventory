package com.github.ashutoshgngwr.minventory.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

	public static void animateWelcome(Node welcomeText, Node formContainer, EventHandler<ActionEvent> onFinishedHandler) {
		FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), formContainer);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		TranslateTransition translateTransition = new TranslateTransition(Duration.millis(800), welcomeText);
		translateTransition.setToX(welcomeText.getScene().getWindow().getWidth());
		FadeTransition welcomeTextFadeTransition = new FadeTransition(Duration.millis(1000), welcomeText);
		welcomeTextFadeTransition.setFromValue(1.0);
		welcomeTextFadeTransition.setToValue(0.0);
		ParallelTransition parallelTransition = 
				new ParallelTransition(fadeTransition, translateTransition, welcomeTextFadeTransition);
		parallelTransition.setOnFinished(onFinishedHandler);
		parallelTransition.play();
	}
}
