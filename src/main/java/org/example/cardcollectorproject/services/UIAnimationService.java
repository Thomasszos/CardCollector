package org.example.cardcollectorproject.services;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.animation.Interpolator;

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

    /**
     * Animates the home screen elements with staggered entrance animations
     * @param mainContent The main content container in the home screen
     */
    public void animateHomeScreenEntrance(HBox mainContent) {
        // Clear any existing inline styling
        mainContent.setOpacity(0);
        mainContent.setTranslateY(20);

        // Create the fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), mainContent);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        // Create the slide-up animation
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1), mainContent);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        // Play both animations together
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideUp);
        // Add a slight delay to let the main container animate first
        parallelTransition.setDelay(Duration.seconds(0.3));
        parallelTransition.play();

        // Animate feature cards if they exist
        animateFeatureCards(mainContent);
    }

    /**
     * Animate feature cards with a staggered entrance
     */
    private void animateFeatureCards(Node root) {
        // Look for feature cards in the scene graph
        HBox featureCardsContainer = (HBox) root.lookup(".feature-cards-container");
        if (featureCardsContainer == null) return;

        // Get all feature cards
        featureCardsContainer.getChildren().forEach(card -> {
            // Initial state
            card.setOpacity(0);
            card.setTranslateY(30);

            // Delay each card by a bit more than the previous one
            int index = featureCardsContainer.getChildren().indexOf(card);

            // Create animations with longer durations and delays
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), card); // Increased from 0.8 to 1.2
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.seconds(1.0 + (index * 0.4))); // Increased from 0.8 to 1.0 and from 0.2 to 0.4

            TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1.2), card); // Increased from 0.8 to 1.2
            slideUp.setFromY(30);
            slideUp.setToY(0);
            slideUp.setDelay(Duration.seconds(1.0 + (index * 0.4))); // Increased from 0.8 to 1.0 and from 0.2 to 0.4

            // Add smoother interpolation
            slideUp.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);

            // Play both animations
            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideUp);
            parallelTransition.play();
        });
    }

    /**
     * Creates a subtle continuous hover animation for UI elements
     * @param node The node to animate
     */
    public void applyHoverAnimation(Node node) {
        // Create a subtle continuous floating animation
        TranslateTransition floatUp = new TranslateTransition(Duration.seconds(0.95), node);
        floatUp.setFromY(0);
        floatUp.setToY(-3);
        floatUp.setCycleCount(Animation.INDEFINITE);
        floatUp.setAutoReverse(true);
        floatUp.setInterpolator(Interpolator.EASE_BOTH);
        floatUp.play();
    }

    /**
     * Fades in a node with scaling effect
     * @param node The node to animate
     * @param duration Animation duration in seconds
     */
    public void fadeInWithScale(Node node, double duration) {
        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(duration), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        // Create scale animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(duration), node);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        // Run animations in parallel
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleUp);
        parallelTransition.play();
    }
}