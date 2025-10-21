package Utils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Example controller showing how to use CSS classes instead of inline styles
 * This demonstrates the proper way to connect controllers to the admin_light_mode.css
 */
public class AdminControllerExample implements Initializable {
    
    @FXML
    private TextField adminTextField;
    
    @FXML
    private Button primaryButton, secondaryButton, successButton, dangerButton, warningButton;
    
    @FXML
    private ChoiceBox<String> adminChoiceBox;
    
    @FXML
    private TableView<?> adminTable;
    
    @FXML
    private Chart adminChart;
    
    @FXML
    private Pane adminCard;
    
    @FXML
    private Text adminLabel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Apply CSS classes instead of using setStyle()
        setupAdminComponents();
    }
    
    /**
     * Setup admin components with proper CSS classes
     * This method shows how to replace inline styles with CSS classes
     */
    private void setupAdminComponents() {
        // Text field styling
        CSSManager.applyAdminTextField(adminTextField);
        
        // Button styling
        CSSManager.applyAdminButtonPrimary(primaryButton);
        CSSManager.applyAdminButtonSecondary(secondaryButton);
        CSSManager.applyAdminActionButtonSuccess(successButton);
        CSSManager.applyAdminActionButtonDanger(dangerButton);
        CSSManager.applyAdminActionButtonWarning(warningButton);
        
        // Choice box styling
        CSSManager.applyAdminChoiceBox(adminChoiceBox);
        
        // Table styling
        CSSManager.applyAdminTable(adminTable);
        
        // Chart styling
        CSSManager.applyAdminChart(adminChart);
        
        // Card styling
        CSSManager.applyAdminCard(adminCard);
        
        // Label styling
        CSSManager.applyAdminLabelValue(adminLabel);
    }
    
    /**
     * Example method showing how to toggle button states
     */
    @FXML
    private void toggleButtonState() {
        if (primaryButton.getStyleClass().contains("active")) {
            CSSManager.removeActiveState(primaryButton);
        } else {
            CSSManager.applyActiveState(primaryButton);
        }
    }
    
    /**
     * Example method showing how to switch between edit and view modes
     */
    @FXML
    private void toggleEditMode() {
        if (adminTextField.isEditable()) {
            // Switch to view mode
            adminTextField.setEditable(false);
            CSSManager.removeAdminTextField(adminTextField);
        } else {
            // Switch to edit mode
            adminTextField.setEditable(true);
            CSSManager.applyAdminTextField(adminTextField);
        }
    }
    
    /**
     * Example method showing how to apply different text styles
     */
    @FXML
    private void applyTextStyles() {
        // Clear existing styles first
        CSSManager.clearStyles(adminLabel);
        
        // Apply new style
        CSSManager.applyAdminLabelTitle(adminLabel);
    }
}
