package org.example.cardcollectorproject.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.cardcollectorproject.exceptions.AuthenticationException;
import org.example.cardcollectorproject.exceptions.ValidationException;
import org.example.cardcollectorproject.services.AuthenticationService;
import org.example.cardcollectorproject.services.UIAnimationService;
import org.example.cardcollectorproject.services.ValidationService;

import javafx.scene.image.ImageView;

import java.io.IOException;


public class LogInController {
    @FXML
    private HBox buttonGroup;
    @FXML
    private GridPane formGrid;
    @FXML
    private Button loginButton;
    @FXML
    private Button signUpButton;
    @FXML
    private Button submitButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField emailField;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private ImageView backgroundImage;


    private final AuthenticationService authService;
    private final UIAnimationService animationService;
    private final ValidationService validationService;
    private boolean isLoginMode = true;

    public LogInController() {
        this.authService = new AuthenticationService();
        this.animationService = new UIAnimationService();
        this.validationService = new ValidationService();
    }

    @FXML
    public void initialize() {
        setupInitialState();
        setupButtonStyles();
        animationService.startBackgroundAnimation(backgroundImage);

    }

    private void setupInitialState() {
        updateFormVisibility();
        clearError();

        // Set initial button states
        loginButton.setDisable(false);
        signUpButton.setDisable(false);

        // Clear any existing active classes first
        loginButton.getStyleClass().remove("active");
        signUpButton.getStyleClass().remove("active");

        // Set active class only on the initial active button (login)
        if (isLoginMode) {
            loginButton.getStyleClass().add("active");
        } else {
            signUpButton.getStyleClass().add("active");
        }

        // Set fixed positions for the common fields
        GridPane.setRowIndex(usernameField, 0);
        GridPane.setRowIndex(passwordField, 1);

        // Set fixed positions for the additional fields
        GridPane.setRowIndex(confirmPasswordField, 2);
        GridPane.setRowIndex(confirmPasswordLabel, 2);
        GridPane.setRowIndex(emailField, 3);
        GridPane.setRowIndex(emailLabel, 3);

        submitButton.setText(isLoginMode ? "Login" : "Sign Up");
    }



    private void setupButtonStyles() {

        loginButton.getStyleClass().add("auth-button");
        signUpButton.getStyleClass().add("auth-button");
        submitButton.getStyleClass().add("submit-button");
    }
    @FXML
    public void handleSubmit() {
        if (isLoginMode) {
            handleLogin();
        } else {
            handleSignUp();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            authService.login(username, password);
            clearError();
            navigateToMainView(); // Navigate to the next view
        } catch (AuthenticationException e) {
            showError(e.getMessage()); // Display error message on the UI
        }
    }

    private void handleSignUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();

        try {
            validationService.validateSignUpData(username, password, confirmPassword, email);
            authService.signUp(username, password, email);
            clearError();
            showSuccess("Sign-Up Successful!"); // Display success message
            switchToLogin(); // Go back to login mode
        } catch (ValidationException | AuthenticationException e) {
            showError(e.getMessage()); // Display validation/authentication error
        }
    }

    @FXML
    public void switchToLogin() {
        if (!isLoginMode) {
            isLoginMode = true;
            loginButton.getStyleClass().add("active");
            signUpButton.getStyleClass().remove("active");
            submitButton.setText("Login");
            animationService.animateTransition(formGrid, true);
            updateFormVisibility();
        }
    }

    @FXML
    public void switchToSignUp() {
        if (isLoginMode) {
            isLoginMode = false;
            signUpButton.getStyleClass().add("active");
            loginButton.getStyleClass().remove("active");
            submitButton.setText("Sign Up");
            animationService.animateTransition(formGrid, false);
            updateFormVisibility();
        }
    }

    private void updateFormVisibility() {
        Node[] signUpNodes = {
                confirmPasswordField, confirmPasswordLabel,
                emailField, emailLabel
        };

        for (Node node : signUpNodes) {
            if (!isLoginMode) {
                node.setVisible(true);
                node.setManaged(true);
                node.setOpacity(0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(800), node);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setDelay(Duration.millis(300)); // Add delay for smooth sequence
                fadeIn.setInterpolator(Interpolator.EASE_IN);
                fadeIn.play();
            } else {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(600), node);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setInterpolator(Interpolator.EASE_OUT);
                fadeOut.setOnFinished(e -> {
                    node.setVisible(false);
                    node.setManaged(false);
                });
                fadeOut.play();
            }
        }
    }
    @FXML
    private void navigateToMainView() {
        try {
            // Get the current stage
            Stage currentStage = (Stage) loginButton.getScene().getWindow();

            // Load main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/main-view.fxml"));
            Scene mainScene = new Scene(loader.load(), 1150, 680);

            // Set the scene
            currentStage.setScene(mainScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.getStyleClass().remove("success-message");
        errorLabel.getStyleClass().add("error-message");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.getStyleClass().remove("error-message");
        errorLabel.getStyleClass().add("success-message");
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.getStyleClass().removeAll("error-message", "success-message");
    }
    // for future implementations
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailField.clear();
    }
}