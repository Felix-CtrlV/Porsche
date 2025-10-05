package Controllers;

import Database.Porsche_DB;
import Model.inventory;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class managerInventoryController {

    @FXML
    private TableColumn<inventory, Double> PriceCol;

    @FXML
    private TableColumn<inventory,?> actionCol;

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
    private TableColumn<inventory, Integer> idCol;

    @FXML
    private Button inventoryAdd;

    @FXML
    private ComboBox<String> inventoryBox;

    @FXML
    private TableView<inventory> inventoryTable;

    @FXML
    private HBox inventoryPane;

    @FXML
    private VBox modelsPane;

    @FXML
    private CheckBox modelsSelectAll;

    @FXML
    private GridPane modelsShowBox;

    @FXML
    private TableColumn<inventory, String > nameCol;

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
    private ComboBox<String > partRelativeComboBox;

    @FXML
    private TableColumn<inventory, Integer> qtyCol;

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
    private TableColumn<inventory, String > statusCol;

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

    public managerInventoryController() throws SQLException, ClassNotFoundException {
    }


    @FXML
    void carHandlerDrop(DragEvent event) throws FileNotFoundException {
        file = event.getDragboard().getFiles();
        Image img = new Image(new FileInputStream(file.get(0)));
        carImage.setImage(img);
    }

    @FXML
    void carHandlerOver(DragEvent event) {
        if(event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    void partHandlerDrop(DragEvent event) throws FileNotFoundException {
        file = event.getDragboard().getFiles();
        Image img = new Image(new FileInputStream(file.get(0)));
        partImage.setImage(img);
    }

    @FXML
    void partHandlerOver(DragEvent event) {
        if(event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
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
    void clickCarCancel(ActionEvent event) throws SQLException {
        hasData(false,"cars");
    }

    @FXML
    void clickCarConfirm(ActionEvent event) {

    }

    @FXML
    void clickExportCSV(ActionEvent event) {

    }

    @FXML
    void clickInventoryAdd(ActionEvent event) {
            addCarbtn.setDisable(true);
            addPartbtn.setDisable(false);
            addCar.setVisible(true);
            addPart.setVisible(false);
            fadeInOut(true,extraPane,inventoryPane);
    }

    @FXML
    void clickModelsSelectAll(ActionEvent event) {
        selectAll(modelsSelectAll.isSelected(),modelsSelectAll);
    }

    @FXML
    void clickPartCancle(ActionEvent event) throws SQLException {
        hasData(false,"parts");
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
    void extraPaneMouseClick(MouseEvent event) throws SQLException {
        if(addCarbtn.isDisable()) {
            hasData(false,"cars" );
        }else{
            hasData(false,"parts");
        }
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

    //true is in or select and false is out or unselect

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

            idCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getId()));
            nameCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getName()));
            statusCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getStatus()));
            qtyCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getQty()));

    }
    List<File> file = new ArrayList<>();
    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();

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

        TranslateTransition translate = new TranslateTransition(Duration.millis(250),label);

        if(check){
            translate.setToY(-25);
            translate.setToX(-10);
        }else {
            if (!field.isFocused() && field.getText().isBlank()) {
                translate.setToY(0);
                translate.setToX(0);
            } else {
                translate.setToY(-25);
                translate.setToX(-10);
            }
        }
        translate.play();

    }

    private void clearCarPartForm() {
        if(addCarbtn.isDisable()) {
            carModelText.clear();
            carYearText.clear();
            carTrimText.clear();
            carExtColorText.clear();
            carIntColorText.clear();
            carPriceText.clear();
            carQtyText.clear();
            carImage.setImage(null);
            gasolineRadio.setSelected(false);
            electricCheckBox.setSelected(false);
        }else {
            partNameText.clear();
            partDescriptionText.clear();
            partPriceText.clear();
            partQtyText.clear();
            partImage.setImage(null);
            partRelativeComboBox.getSelectionModel().clearSelection();

        }
        fadeInOut(false, extraPane, inventoryPane);
    }

    private void hasData(boolean check,String path) throws SQLException {
        if(path.equals("cars")){
            String img = String.valueOf(file.get(0));
            System.out.println(img);
            String models = carModelText.getText();
            String trim = carTrimText.getText();
            String extColor = carExtColorText.getText();
            String intColor = carIntColorText.getText();
            String fuel_type = gasolineRadio.getTypeSelector().toString();
            String qty = carQtyText.getText();
            String price = carPriceText.getText();

            if(img.isEmpty()&& models.isEmpty() && trim.isEmpty() &&
                    extColor.isEmpty() && intColor.isEmpty() &&
                    fuel_type.isEmpty() && qty.isEmpty() && price.isEmpty()
                ){
                clearCarPartForm();
            }else {
                if (check) {
                    CallableStatement cs = con.prepareCall("");

                    clearCarPartForm();
                } else {
                    alertForm("addCar");
                }
            }

        }else{
            String img = String.valueOf(file.get(0));
            String name = partNameText.getText();
            String qty = partQtyText.getText();
            String price = partPriceText.getText();
            String description = partDescriptionText.getText();
            if(img.isEmpty() && name.isEmpty() && qty.isEmpty() && price.isEmpty() && description.isEmpty()){
                clearCarPartForm();
            }else {
                if (check){
                    CallableStatement cs = con.prepareCall("");

                    clearCarPartForm();
                }else{
                    alertForm("addPart");
                }
            }
        }
    }
    //for alernt there have "addCar" "addPart" "editCar" "editPart"
    private void alertForm(String check){

        if(check.equalsIgnoreCase("addcar")){

        }else if(check.equalsIgnoreCase("addpart")){

        }else if(check.equalsIgnoreCase("editCar")){

        }else if(check.equalsIgnoreCase("editPart")){

        }else{

        }
    }


}
