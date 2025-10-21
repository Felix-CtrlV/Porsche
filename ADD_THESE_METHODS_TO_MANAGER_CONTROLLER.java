// ========== ADD THESE METHODS BEFORE THE CLOSING BRACE OF managerDashboardController.java ==========
// Copy everything below and paste it before the final closing brace "}" at line 527

    private void showProfilePane() {
        // Settings pane is already hidden from verification step
        
        if (settingPane != null) {
            settingPane.setVisible(false);
        }

        // Refresh profile data from session first
        if (current != null) {
            profileName.setText(current.getUsername() != null ? current.getUsername() : "");
            profileEmail.setText(current.getEmail() != null ? current.getEmail() : "");
            profilePhone.setText(current.getPhone() != null ? current.getPhone() : "");
            profileAddress.setText(current.getAddress() != null ? current.getAddress() : "");
            
            // Format DOB nicely
            if (current.getDob() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                profileDOB.setText(current.getDob().format(formatter));
            } else {
                profileDOB.setText("");
            }
        }
        
        // Ensure fields are not editable and deselect any text
        profileEmail.setEditable(false);
        profilePhone.setEditable(false);
        profileAddress.setEditable(false);
        profileEmail.setFocusTraversable(false);
        profilePhone.setFocusTraversable(false);
        profileAddress.setFocusTraversable(false);
        
        // Slide out verification pane first
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        slideOutVerify.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            
            // Then slide in profile pane
            profilePane.setVisible(true);
            profilePane.setTranslateX(420);
            
            TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
            slideInProfile.setFromX(420);
            slideInProfile.setToX(0);
            slideInProfile.setOnFinished(ev -> {
                // Clear any selection after animation
                profileEmail.deselect();
                profilePhone.deselect();
                profileAddress.deselect();
            });
            slideInProfile.play();
        });
        slideOutVerify.play();
    }
    
    private void hideProfilePane() {
        closeProfilePane(true);
    }

    private void closeProfilePane(boolean reopenSettings) {
        if (!profilePane.isVisible()) {
            return;
        }

        TranslateTransition slideOutProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideOutProfile.setFromX(0);
        slideOutProfile.setToX(420);

        if (reopenSettings) {
            slideOutProfile.setOnFinished(e -> {
                profilePane.setVisible(false);
                profilePane.setTranslateX(0);

                if (settingPane != null) {
                    settingPane.setTranslateX(420);
                    settingPane.setVisible(true);
                    TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
                    slideInSettings.setFromX(420);
                    slideInSettings.setToX(0);
                    slideInSettings.play();
                }
            });
            slideOutProfile.play();
        } else {
            slideOutProfile.setOnFinished(e -> {
                profilePane.setVisible(false);
                profilePane.setTranslateX(0);
                if (settingPane != null) {
                    settingPane.setVisible(false);
                }

                FadeTransition fadeOverlay = new FadeTransition(Duration.millis(200), overlayPane);
                fadeOverlay.setFromValue(overlayPane.getOpacity());
                fadeOverlay.setToValue(0);
                fadeOverlay.setOnFinished(ev -> {
                    overlayPane.setVisible(false);
                    overlayPane.setOpacity(0.5);
                    root.setEffect(null);
                    root.setDisable(false);
                });
                fadeOverlay.play();
            });
            slideOutProfile.play();
        }
    }
    
    private void toggleEditMode() {
        boolean isEditable = profileEmail.isEditable();
        
        if (!isEditable) {
            // Enable editing
            profileEmail.setEditable(true);
            profilePhone.setEditable(true);
            profileAddress.setEditable(true);
            profileEmail.setFocusTraversable(true);
            profilePhone.setFocusTraversable(true);
            profileAddress.setFocusTraversable(true);
            profileEmail.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            profilePhone.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            profileAddress.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            saveProfileBtn.setVisible(true);
            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(true);
            }
            editProfileBtn.setText("✖");
        } else {
            // Disable editing
            profileEmail.setEditable(false);
            profilePhone.setEditable(false);
            profileAddress.setEditable(false);
            profileEmail.setFocusTraversable(false);
            profilePhone.setFocusTraversable(false);
            profileAddress.setFocusTraversable(false);
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            saveProfileBtn.setVisible(false);
            editProfileBtn.setText("✏");
            
            // Deselect any text and revert changes
            profileEmail.deselect();
            profilePhone.deselect();
            profileAddress.deselect();
            profileAddress.setText(current.getAddress());
            profileEmail.setText(current.getEmail());
            profilePhone.setText(current.getPhone());

            selectedProfilePhoto = null;
            applyPhotoToImages(cachedPhotoPath);
        }
    }
    
    @FXML
    private void onEditProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) admin_anc.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        selectedProfilePhoto = selectedFile;
        if (pfImage != null) {
            pfImage.setImage(new Image(selectedFile.toURI().toString()));
        }

        saveProfileBtn.setVisible(true);
    }
    
    @FXML
    private void onSaveProfile() {
        try {
            String photoPath = cachedPhotoPath;
            if (selectedProfilePhoto != null) {
                photoPath = saveProfileImage(selectedProfilePhoto, current.getUsername(), current.getUserid());
            }

            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE user_info SET user_email = ?, user_phone = ?, user_address = ?, user_photo = ? WHERE user_id = ?")) {
                ps.setString(1, profileEmail.getText());
                ps.setString(2, profilePhone.getText());
                ps.setString(3, profileAddress.getText());
                ps.setString(4, photoPath);
                ps.setInt(5, current.getUserid());
                ps.executeUpdate();
            }

            cachedPhotoPath = photoPath;
            selectedProfilePhoto = null;

            // Update session copy
            current.setEmail(profileEmail.getText());
            current.setAddress(profileAddress.getText());
            current.setPhone(profilePhone.getText());
            Session.setInstance(current);

            applyPhotoToImages(cachedPhotoPath);

            // Disable editing
            toggleEditMode();

            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(false);
            }
            saveProfileBtn.setVisible(false);

            // Show success toast
            showToast("Success", "Profile updated successfully!", "success");

        } catch (SQLException ex) {
            // Show error toast
            showToast("Error", "Failed to update profile", "error");
            ex.printStackTrace();
        }
    }

    private void loadCurrentProfilePhoto() {
        cachedPhotoPath = fetchPhotoFromDatabase();
        applyPhotoToImages(cachedPhotoPath);
    }

    private String fetchPhotoFromDatabase() {
        String sql = "SELECT user_photo FROM user_info WHERE user_id = ?";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, current.getUserid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_photo");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private void applyPhotoToImages(String storedPath) {
        Image image = null;

        if (storedPath != null && !storedPath.isBlank()) {
            Optional<String> resolved = UserPhotoResolver.resolve(storedPath);
            if (resolved.isPresent()) {
                try {
                    image = new Image(resolved.get(), true);
                } catch (Exception ignored) {
                    image = null;
                }
            }
        }

        if (image == null) {
            try {
                image = new Image(Objects.requireNonNull(getClass().getResource("/Image/defaultUserProfile.jpg")).toExternalForm(), true);
            } catch (Exception ignored) {
                image = null;
            }
        }

        if (image != null) {
            if (pfImage != null) {
                pfImage.setImage(image);
            }
        }
    }

    private String saveProfileImage(File file, String username, int userId) {
        try {
            String extension = extractExtension(file.getName());
            String sanitizedName = sanitizeForFileName(username);
            String fileName = String.format("user_%d_%s%s", userId, sanitizedName, extension);

            Path imagesDir = Paths.get(System.getProperty("user.dir"), "Images");
            Files.createDirectories(imagesDir);
            Path target = imagesDir.resolve(fileName);

            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            return "Images/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedPhotoPath;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return ".png";
    }

    private String sanitizeForFileName(String value) {
        if (value == null || value.isBlank()) {
            return "user";
        }
        return value.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "_");
    }
    
    // ========== TOAST NOTIFICATION METHODS ==========
    
    public void showToast(String title, String message, String type) {
        toastTitle.setText(title);
        toastMessage.setText(message);

        String normalizedType = (type == null ? "info" : type.toLowerCase());

        // Set icon and color based on type
        StackPane iconContainer = (StackPane) toastIcon.getParent();
        switch (normalizedType) {
            case "success":
                toastIcon.setText("✓");
                iconContainer.setStyle("-fx-background-color: #10b981; -fx-background-radius: 18;");
                break;
            case "error":
                toastIcon.setText("✕");
                iconContainer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 18;");
                break;
            case "warning":
                toastIcon.setText("!");
                iconContainer.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 18;");
                break;
            default:
                toastIcon.setText("ℹ");
                iconContainer.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18;");
                break;
        }
        
        toastNotification.setVisible(true);
        toastNotification.setTranslateY(-100);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastNotification);
        slideIn.setFromY(-100);
        slideIn.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastNotification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showToast = new ParallelTransition(slideIn, fadeIn);
        
        // Auto hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> hideToast());
        
        SequentialTransition sequence = new SequentialTransition(showToast, pause);
        sequence.play();
    }
    
    private void hideToast() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastNotification);
        slideOut.setFromY(0);
        slideOut.setToY(-100);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastNotification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> toastNotification.setVisible(false));
        hide.play();
    }
    
    // ========== CONFIRMATION DIALOG METHODS ==========
    
    public void showConfirmDialog(String title, String message, String icon, Runnable onConfirm) {
        confirmTitle.setText(title);
        confirmMessage.setText(message);
        confirmIcon.setText(icon);
        confirmCallback = onConfirm;
        
        // Clear any custom content
        confirmContent.getChildren().clear();
        confirmContent.getChildren().add(confirmMessage);
        
        // Show overlay
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
        
        // Show dialog
        confirmDialog.setVisible(true);
        confirmDialog.setTranslateY(-600);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), confirmDialog);
        slideIn.setFromY(-600);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), confirmDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();
    }
    
    @FXML
    private void onConfirmOk() {
        hideConfirmDialog();
        if (confirmCallback != null) {
            confirmCallback.run();
            confirmCallback = null;
        }
    }
    
    @FXML
    private void onConfirmCancel() {
        hideConfirmDialog();
        confirmCallback = null;
    }
    
    private void hideConfirmDialog() {
        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();
        
        // Slide out dialog
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), confirmDialog);
        slideOut.setFromY(0);
        slideOut.setToY(-600);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), confirmDialog);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> confirmDialog.setVisible(false));
        hide.play();
    }
    
    // ========== CHANGE PASSWORD DIALOG METHODS ==========
    
    @FXML
    public void showChangePasswordDialog() {
        // Reset to step 1
        passwordVerificationPane.setVisible(true);
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(false);
        
        // Clear fields
        currentPasswordField.clear();
        otpField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        // Clear status labels
        passwordVerifyStatus.setText("");
        otpVerifyStatus.setText("");
        changePasswordStatus.setText("");
        
        // Show overlay
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
        
        // Show dialog
        changePasswordDialog.setVisible(true);
        changePasswordDialog.setTranslateY(-700);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), changePasswordDialog);
        slideIn.setFromY(-700);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), changePasswordDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();
        
        // Set up enter key handlers
        currentPasswordField.setOnAction(e -> onVerifyCurrentPassword());
        otpField.setOnAction(e -> onVerifyOTP());
        confirmPasswordField.setOnAction(e -> onSubmitPasswordChange());
    }
    
    @FXML
    public void closePasswordDialog() {
        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();
        
        // Slide out dialog
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), changePasswordDialog);
        slideOut.setFromY(0);
        slideOut.setToY(-700);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), changePasswordDialog);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> changePasswordDialog.setVisible(false));
        hide.play();
    }
    
    @FXML
    void onVerifyCurrentPassword() {
        String password = currentPasswordField.getText().trim();

        if (password.isEmpty()) {
            showPasswordMessage("Please enter your current password", false);
            return;
        }

        Session session = Session.getInstance();
        if (!password.equals(session.getPassword())) {
            showPasswordMessage("Incorrect password!", false);
            currentPasswordField.clear();
            currentPasswordField.requestFocus();
            return;
        }

        // Password correct, send OTP
        showPasswordLoading(true);
        verifyCurrentPasswordBtn.setDisable(true);
        passwordVerifyStatus.setText("Sending OTP...");

        new Thread(() -> {
            boolean success = otpService.sendOTP(session.getEmail());
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("OTP sent to " + maskEmail(session.getEmail()), true);
                    showOtpPane();
                } else {
                    showPasswordMessage("Failed to send OTP", false);
                    verifyCurrentPasswordBtn.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    void onVerifyOTP() {
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            showPasswordMessage("Please enter the OTP", false);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d+")) {
            showPasswordMessage("OTP must be 6 digits", false);
            return;
        }

        showPasswordLoading(true);
        verifyOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean verified = otpService.verifyOTP(Session.getInstance().getEmail(), otp);
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (verified) {
                    showPasswordMessage("OTP verified successfully!", true);
                    showNewPasswordPane();
                } else {
                    showPasswordMessage("Invalid or expired OTP!", false);
                    verifyOtpBtn.setDisable(false);
                    otpField.clear();
                    otpField.requestFocus();
                }
            });
        }).start();
    }

    @FXML
    void onResendOTP() {
        showPasswordLoading(true);
        resendOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean success = otpService.sendOTP(Session.getInstance().getEmail());
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("OTP resent successfully!", true);
                } else {
                    showPasswordMessage("Failed to resend OTP", false);
                }
                resendOtpBtn.setDisable(false);
            });
        }).start();
    }

    @FXML
    void onSubmitPasswordChange() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordMessage("Please fill in all fields", false);
            return;
        }

        if (newPassword.length() < 6) {
            showPasswordMessage("Password must be at least 6 characters", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordMessage("Passwords do not match!", false);
            return;
        }

        showPasswordLoading(true);
        changePasswordBtn.setDisable(true);

        new Thread(() -> {
            boolean success = updatePasswordInDatabase(Session.getInstance().getUserid(), newPassword);
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("Password changed successfully!", true);
                    Session.getInstance().setPassword(newPassword);
                    
                    // Close dialog after 2 seconds
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(ev -> closePasswordDialog());
                    pause.play();
                } else {
                    showPasswordMessage("Failed to update password", false);
                    changePasswordBtn.setDisable(false);
                }
            });
        }).start();
    }

    private boolean updatePasswordInDatabase(int userId, String newPassword) {
        String sql = "UPDATE user_info SET password = SHA2(?, 256) WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    private void showOtpPane() {
        passwordVerificationPane.setVisible(false);
        otpVerificationPane.setVisible(true);
        newPasswordPane.setVisible(false);
        otpField.requestFocus();
    }

    private void showNewPasswordPane() {
        passwordVerificationPane.setVisible(false);
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(true);
        newPasswordField.requestFocus();
    }

    private void showPasswordLoading(boolean show) {
        passwordLoadingPane.setVisible(show);
    }

    private void showPasswordMessage(String message, boolean isSuccess) {
        if (currentPasswordAnimation != null) {
            currentPasswordAnimation.stop();
        }

        passwordMessagePane.setVisible(false);

        String type = isSuccess ? "success" : "error";
        String title = isSuccess ? "Success" : "Error";

        showToast(title, message, type);
    }
