package Utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * CSS Manager utility class for applying admin light mode styles
 * This class provides methods to apply CSS classes from admin_light_mode.css
 * instead of using inline styles in controllers.
 */
public class CSSManager {
    
    /**
     * Apply admin light mode CSS to a scene
     * @param scene The scene to apply CSS to
     */
    public static void applyAdminLightMode(Scene scene) {
        scene.getStylesheets().add(CSSManager.class.getResource("/CSS/admin_light_mode.css").toExternalForm());
    }
    
    /**
     * Apply admin text field styling
     * @param textField The text field to style
     */
    public static void applyAdminTextField(TextField textField) {
        textField.getStyleClass().add("admin-text-field");
    }
    
    /**
     * Remove admin text field styling
     * @param textField The text field to remove styling from
     */
    public static void removeAdminTextField(TextField textField) {
        textField.getStyleClass().remove("admin-text-field");
    }
    
    /**
     * Apply admin button primary styling
     * @param button The button to style
     */
    public static void applyAdminButtonPrimary(Button button) {
        button.getStyleClass().add("admin-button-primary");
    }
    
    /**
     * Apply admin button secondary styling
     * @param button The button to style
     */
    public static void applyAdminButtonSecondary(Button button) {
        button.getStyleClass().add("admin-button-secondary");
    }
    
    /**
     * Apply admin action button success styling
     * @param button The button to style
     */
    public static void applyAdminActionButtonSuccess(Button button) {
        button.getStyleClass().add("admin-action-button-success");
    }
    
    /**
     * Apply admin action button danger styling
     * @param button The button to style
     */
    public static void applyAdminActionButtonDanger(Button button) {
        button.getStyleClass().add("admin-action-button-danger");
    }
    
    /**
     * Apply admin action button warning styling
     * @param button The button to style
     */
    public static void applyAdminActionButtonWarning(Button button) {
        button.getStyleClass().add("admin-action-button-warning");
    }
    
    /**
     * Apply admin choice box styling
     * @param choiceBox The choice box to style
     */
    public static void applyAdminChoiceBox(ChoiceBox<?> choiceBox) {
        choiceBox.getStyleClass().add("admin-choice-box");
    }
    
    /**
     * Apply admin table styling
     * @param tableView The table view to style
     */
    public static void applyAdminTable(TableView<?> tableView) {
        tableView.getStyleClass().add("admin-table");
    }
    
    /**
     * Apply admin chart styling
     * @param chart The chart to style
     */
    public static void applyAdminChart(Chart chart) {
        chart.getStyleClass().add("admin-chart");
    }
    
    /**
     * Apply admin overview chart styling
     * @param chart The chart to style
     */
    public static void applyAdminOverviewChart(Chart chart) {
        chart.getStyleClass().add("admin-overview-chart");
    }
    
    /**
     * Apply admin card styling
     * @param pane The pane to style as a card
     */
    public static void applyAdminCard(Pane pane) {
        pane.getStyleClass().add("admin-card");
    }
    
    /**
     * Apply admin label title styling
     * @param text The text node to style
     */
    public static void applyAdminLabelTitle(Text text) {
        text.getStyleClass().add("admin-label-title");
    }
    
    /**
     * Apply admin label value styling
     * @param text The text node to style
     */
    public static void applyAdminLabelValue(Text text) {
        text.getStyleClass().add("admin-label-value");
    }
    
    /**
     * Apply percentage positive styling
     * @param text The text node to style
     */
    public static void applyPercentagePositive(Text text) {
        text.getStyleClass().add("percentage-positive");
    }
    
    /**
     * Apply percentage negative styling
     * @param text The text node to style
     */
    public static void applyPercentageNegative(Text text) {
        text.getStyleClass().add("percentage-negative");
    }
    
    /**
     * Apply chart bar styling
     * @param node The node to style as a chart bar
     */
    public static void applyChartBar(Node node) {
        node.getStyleClass().add("chart-bar");
    }
    
    /**
     * Apply chart series area line styling
     * @param node The node to style
     */
    public static void applyChartSeriesAreaLine(Node node) {
        node.getStyleClass().add("chart-series-area-line");
    }
    
    /**
     * Apply chart series area fill styling
     * @param node The node to style
     */
    public static void applyChartSeriesAreaFill(Node node) {
        node.getStyleClass().add("chart-series-area-fill");
    }
    
    /**
     * Apply chart line symbol styling
     * @param node The node to style
     */
    public static void applyChartLineSymbol(Node node) {
        node.getStyleClass().add("chart-line-symbol");
    }
    
    /**
     * Apply bar chart styling
     * @param chart The chart to style
     */
    public static void applyBarChart(Chart chart) {
        chart.getStyleClass().add("bar-chart");
    }
    
    /**
     * Apply active state to a button
     * @param button The button to make active
     */
    public static void applyActiveState(Button button) {
        button.getStyleClass().add("active");
    }
    
    /**
     * Remove active state from a button
     * @param button The button to remove active state from
     */
    public static void removeActiveState(Button button) {
        button.getStyleClass().remove("active");
    }
    
    /**
     * Clear all style classes from a node
     * @param node The node to clear styles from
     */
    public static void clearStyles(Node node) {
        node.getStyleClass().clear();
    }
}
