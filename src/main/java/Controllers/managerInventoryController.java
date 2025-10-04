package Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.w3c.dom.Text;

public class managerInventoryController {

    @FXML
    private TableColumn<?, ?> PriceCol;

    @FXML
    private TableColumn<?, ?> actionCol;

    @FXML
    private VBox addCar;

    @FXML
    private Button addCarbtn;

    @FXML
    private VBox addPart;

    @FXML
    private Button addPartbtn;

    @FXML
    private Button carCancel;

    @FXML
    private Button carConfirm;

    @FXML
    private Label carExtColorLbl;

    @FXML
    private TextField carExtColorText;

    @FXML
    private ImageView carImage;

    @FXML
    private Label carIntColorLbl;

    @FXML
    private TextField carIntColorText;

    @FXML
    private Label carModelLbl;

    @FXML
    private TextField carModelText;

    @FXML
    private Label carPriceLbl;

    @FXML
    private TextField carPriceText;

    @FXML
    private Label carQtyLbl;

    @FXML
    private TextField carQtyText;

    @FXML
    private Label carTrimLbl;

    @FXML
    private TextField carTrimText;

    @FXML
    private Label carYearLbl;

    @FXML
    private TextField carYearText;

    @FXML
    private RadioButton dieselRadio;

    @FXML
    private CheckBox electricCheckBox;

    @FXML
    private Button exportCSV;

    @FXML
    private BorderPane extraPane;

    @FXML
    private ToggleGroup fuelTypeGroup;

    @FXML
    private RadioButton gasolineRadio;

    @FXML
    private RadioButton hybridRadio;

    @FXML
    private TableColumn<?, ?> idCol;

    @FXML
    private Button inventoryAdd;

    @FXML
    private ComboBox<String> inventoryBox;

    @FXML
    private TableView<?> inventoryCarTable;

    @FXML
    private HBox inventoryPane;

    @FXML
    private VBox modelsPane;

    @FXML
    private CheckBox modelsSelectAll;

    @FXML
    private GridPane modelsShowBox;

    @FXML
    private TableColumn<?, ?> nameCol;

    @FXML
    private Button partCancle;

    @FXML
    private Button partConfirm;

    @FXML
    private Label partDescriptionLbl;

    @FXML
    private TextField partDescriptionText;

    @FXML
    private ImageView partImage;

    @FXML
    private Label partNameLbl;

    @FXML
    private TextField partNameText;

    @FXML
    private Label partPriceLbl;

    @FXML
    private TextField partPriceText;

    @FXML
    private Label partQtyLbl;

    @FXML
    private TextField partQtyText;

    @FXML
    private ComboBox<?> partRelativeComboBox;

    @FXML
    private TableColumn<?, ?> qtyCol;

    @FXML
    private Button refreshTable;

    @FXML
    private TextField searchBar;

    @FXML
    private Button searchBtn;

    @FXML
    private VBox seriesPane;

    @FXML
    private CheckBox seriesSelectAll;

    @FXML
    private Label showTableRows;

    @FXML
    private TableColumn<?, ?> statusCol;

    @FXML
    private CheckBox car718;

    @FXML
    private CheckBox car911;

    @FXML
    private CheckBox carCayenne;

    @FXML
    private CheckBox carMacan;

    @FXML
    private CheckBox carPanamera;

    @FXML
    private CheckBox carTaycan;

    @FXML
    void carHandlerDrop(DragEvent event) {

    }

    @FXML
    void carHandlerOver(DragEvent event) {

    }

    @FXML
    void clickAddCarbtn(ActionEvent event) {
        addCarbtn.setDisable(true);
        addPartbtn.setDisable(false);
        fadeInOut(true,addCar,addPart);
    }

    @FXML
    void clickAddPartbtn(ActionEvent event) {
        addCarbtn.setDisable(false);
        addPartbtn.setDisable(true);
        fadeInOut(true,addPart,addCar);
    }

    @FXML
    void clickCarCancel(ActionEvent event) {

    }

    @FXML
    void clickCarConfirm(ActionEvent event) {

    }

    @FXML
    void clickExportCSV(ActionEvent event) {

    }

    @FXML
    void clickInventoryAdd(ActionEvent event) {
            fadeInOut(true,extraPane,inventoryPane);
    }

    @FXML
    void clickModelsSelectAll(ActionEvent event) {
        selectAll(modelsSelectAll.isSelected(),modelsSelectAll);
    }

    @FXML
    void clickPartCancle(ActionEvent event) {

    }

    @FXML
    void clickPartConfirm(ActionEvent event) {

    }

    @FXML
    void clickRefreshTable(ActionEvent event) {

    }

    @FXML
    void clickSearchBtn(ActionEvent event) {

    }

    @FXML
    void clickSelect718(ActionEvent event) {
        selectAll(car718.isSelected(),car718);

    }

    @FXML
    void clickSelect911(ActionEvent event) {
        selectAll(car911.isSelected(),car911);

    }

    @FXML
    void clickSelectCayenne(ActionEvent event) {
        selectAll(carCayenne.isSelected(),carCayenne);

    }

    @FXML
    void clickSelectMacan(ActionEvent event) {
        selectAll(carMacan.isSelected(),carMacan);

    }

    @FXML
    void clickSelectPanamera(ActionEvent event) {
        selectAll(carPanamera.isSelected(),carPanamera);

    }

    @FXML
    void clickSelectTaycan(ActionEvent event) {
        selectAll(carTaycan.isSelected(),carTaycan);

    }
    @FXML
    void clickSeriesSelectAll(ActionEvent event) {
        selectAll(seriesSelectAll.isSelected(),seriesSelectAll);
    }

    @FXML
    void extraPaneMouseClick(MouseEvent event) {
           fadeInOut(false,extraPane,inventoryPane);
    }


    @FXML
    void partHandlerDrop(DragEvent event) {

    }

    @FXML
    void partHandlerOver(DragEvent event) {

    }


    @FXML
    void clickInventoryBox(ActionEvent event) {
        String selectedValue = inventoryBox.getValue();

        if ("Cars".equals(selectedValue)) {
            selectAll(true,seriesSelectAll);
            fadeInOut(true,modelsPane,null);
            fadeInOut(true, seriesPane, null);
        } else if ("Parts".equals(selectedValue)) {
            fadeInOut(false,modelsPane,null);
            fadeInOut(false, seriesPane, null);

        }
    }


    @FXML
    private void initialize(){
            extraPane.setVisible(false);
            inventoryPane.setOpacity(1);
            addCar.setVisible(true);
            addPart.setVisible(false);
            addCarbtn.setDisable(true);
            addPartbtn.setDisable(false);

            inventoryBox.getItems().addAll("Cars","Parts");
            inventoryBox.setValue("Cars");
            selectAll(true,seriesSelectAll);

            setupFloatingLabel(partNameText, partNameLbl);
            setupFloatingLabel(partQtyText, partQtyLbl);
            setupFloatingLabel(partPriceText, partPriceLbl);
            setupFloatingLabel(partDescriptionText, partDescriptionLbl);

            setupFloatingLabel(carModelText, carModelLbl);
            setupFloatingLabel(carTrimText, carTrimLbl);
            setupFloatingLabel(carYearText, carYearLbl);
            setupFloatingLabel(carQtyText, carQtyLbl);
            setupFloatingLabel(carPriceText, carPriceLbl);
            setupFloatingLabel(carExtColorText, carExtColorLbl);
            setupFloatingLabel(carIntColorText, carIntColorLbl);
    }
    //true is in or select and false is out or unselect
    private void fadeInOut(boolean check, Node cNode,Node bNode){

        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(500));
        fade.setNode(cNode);
        if(bNode == inventoryPane) {
            if(check){
                fade.setFromValue(0);
                fade.setToValue(1);

            }else{
                fade.setFromValue(1);
                fade.setToValue(0);
            }
            fade.play();
            if (check) {
                cNode.setVisible(true);
                bNode.setOpacity(0.5);
            } else {
                cNode.setVisible(false);
                bNode.setOpacity(1);
            }
        }else{
            if(check) {
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
                cNode.setVisible(true);
                if (bNode != null) {
                    bNode.setVisible(false);
                }
            }else{
                fade.setFromValue(1);
                fade.setToValue(0);
                cNode.setVisible(false);
                fade.play();
            }
        }
    }

    private void selectAll(boolean check, CheckBox in){

        if (in == seriesSelectAll) {
            in.setSelected(check);
            car718.setSelected(check);
            car911.setSelected(check);
            carCayenne.setSelected(check);
            carMacan.setSelected(check);
            carPanamera.setSelected(check);
            carTaycan.setSelected(check);
            modelsSelectAll.setSelected(check);
        }else{
            seriesSelectAll.setSelected(false);
            modelsSelectAll.setSelected(true);
            in.setSelected(check);

            if(carTaycan.isSelected() && car911.isSelected() &&
                    carCayenne.isSelected() && car718.isSelected() &&
                    carMacan.isSelected() && carPanamera.isSelected()
                ){
                seriesSelectAll.setSelected(true);
            }

        }
        if(!carTaycan.isSelected() && !car911.isSelected() &&
                !carCayenne.isSelected() && !car718.isSelected() &&
                !carMacan.isSelected() && !carPanamera.isSelected()
        ){
            if(modelsPane.isVisible()) {
                fadeInOut(false, modelsPane, null);
            }
        }else {
            if(!modelsPane.isVisible()){
                fadeInOut(true,modelsPane,null);
            }
        }
    }


    private void setupFloatingLabel(TextField field, Label label) {

        EventHandler<MouseEvent> onEnter = e -> translateInOut(true, label, field);
        EventHandler<MouseEvent> onExit = e -> translateInOut(false, label, field);

        field.setOnMouseEntered(onEnter);
        label.setOnMouseEntered(onEnter);
        field.setOnMouseExited(onExit);
        label.setOnMouseExited(onExit);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                translateInOut(true, label, field);
            } else {
                translateInOut(false, label, field);
            }
        });
    }


    private void translateInOut(boolean check,Label label,TextField field){

        TranslateTransition translate = new TranslateTransition(Duration.millis(500),label);

        if(check){
            translate.setToY(-25);
        }else {
            if (!field.isFocused() && field.getText().isBlank()) {
                translate.setToY(0);
            } else {
                translate.setToY(-25);
            }
        }
        translate.play();

    }

}
