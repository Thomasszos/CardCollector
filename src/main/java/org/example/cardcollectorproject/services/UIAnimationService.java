package org.example.cardcollectorproject.services;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;


import javafx.scene.image.ImageView;



public class UIAnimationService {
    private static final int ANIMATION_DURATION = 600; // Increased duration

    public void animateTransition(Node node, boolean toLogin) {
        // Create a sequential transition for smoother animation
        SequentialTransition sequentialTransition = new SequentialTransition();

        // First phase - fade and scale out
        FadeTransition fadeOut = createFadeTransition(node, 1.0, 0.3);
        ScaleTransition scaleOut = createScaleTransition(node, 1.0, 0.95);

        // Second phase - fade and scale in
        FadeTransition fadeIn = createFadeTransition(node, 0.3, 1.0);
        ScaleTransition scaleIn = createScaleTransition(node, 0.95, 1.0);

        // Create parallel transitions for each phase
        ParallelTransition fadeOutPhase = new ParallelTransition(fadeOut, scaleOut);
        ParallelTransition fadeInPhase = new ParallelTransition(fadeIn, scaleIn);

        // Add both phases to the sequential transition
        sequentialTransition.getChildren().addAll(fadeOutPhase, fadeInPhase);

        // Add easing for smoother animation
        fadeOut.setInterpolator(Interpolator.EASE_OUT);
        fadeIn.setInterpolator(Interpolator.EASE_IN);
        scaleOut.setInterpolator(Interpolator.EASE_OUT);
        scaleIn.setInterpolator(Interpolator.EASE_IN);

        sequentialTransition.play();
    }

    private FadeTransition createFadeTransition(Node node, double from, double to) {
        FadeTransition transition = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), node);
        transition.setFromValue(from);
        transition.setToValue(to);
        return transition;
    }

    private ScaleTransition createScaleTransition(Node node, double from, double to) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), node);
        transition.setFromX(from);
        transition.setFromY(from);
        transition.setToX(to);
        transition.setToY(to);
        return transition;
    }
    public void startBackgroundAnimation(Node backgroundImage) {
        // Create infinite translation animation with faster speeds
        TranslateTransition moveRight = new TranslateTransition(Duration.seconds(8), backgroundImage);  // reduced from 15 to 8
        moveRight.setFromX(-10);
        moveRight.setToX(10);

        TranslateTransition moveDown = new TranslateTransition(Duration.seconds(5), backgroundImage);   // reduced from 10 to 5
        moveDown.setFromY(-10);
        moveDown.setToY(10);

        // Create parallel transition for combined movement
        ParallelTransition parallelTransition = new ParallelTransition(moveRight, moveDown);

        // Make the animation reverse and repeat indefinitely
        parallelTransition.setAutoReverse(true);
        parallelTransition.setCycleCount(Timeline.INDEFINITE);

        // Use EASE_BOTH for smooth direction changes
        moveRight.setInterpolator(Interpolator.EASE_BOTH);
        moveDown.setInterpolator(Interpolator.EASE_BOTH);

        // Start the animation
        parallelTransition.play();
    }
}

