package Controllers;

import Database.Porsche_DB;
import Model.inventory;
import Model.user;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.w3c.dom.Text;

import javax.xml.transform.Result;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class managerInventoryController {

    @FXML
    private TableColumn<inventory, Double> priceCol;

    @FXML
    private TableColumn<inventory,inventory> actionCol;

    @FXML
    private VBox addCar;

    @FXML
    private Button addCarbtn;

    @FXML
    private VBox addPart;

    @FXML
    private Button addPartbtn;

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
    private TableColumn<inventory, String> idCol;

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

    @FXML
    private ColumnConstraints modelsCol1;

    @FXML
    private ColumnConstraints modelsCol2;

    @FXML
    private VBox addPane;

    @FXML
    private VBox editPane;

    @FXML
    private Label editTitle;

    @FXML
    private VBox editCar;
    @FXML
    private Label editCarId;
    @FXML
    private Label editCarSeries;
    @FXML
    private TextField editCarName;
    @FXML
    private TextField editCarUsage;
    @FXML
    private TextField editCarExtColor;
    @FXML
    private TextField editCarIntColor;
    @FXML
    private TextField editCarQty;
    @FXML
    private TextField editCarPrice;
    @FXML
    private ImageView editCarImg;
    @FXML
    private  TextField editCarProductAt;
    @FXML
    private  Button editCarApply;
    @FXML
    private Button editCarRevert;

    @FXML
    private VBox editPart;
    @FXML
    private Label editPartId;
    @FXML
    private TextField editPartName;
    @FXML
    private TextField editPartForCar;
    @FXML
    private TextField editPartDescription;
    @FXML
    private TextField editPartQty;
    @FXML
    private TextField editPartPrice;
    @FXML
    private ImageView editPartImg;
    @FXML
    private Button editPartRevert;
    @FXML
    private Button editPartApply;
    @FXML
    private Button switchTable ;
    @FXML
    void clickSwitchTable(ActionEvent event){
        String currentText = switchTable.getText();

        if (currentText.contains("Available")) {
            // Switch to showing unavailable items
            switchTable.setText("Unavailable");
            showUnavailableItems();
        } else {
            // Switch to showing available items
            switchTable.setText("Available");
            showAvailableItems();
        }
    }

    public managerInventoryController() throws SQLException, ClassNotFoundException {
    }

    @FXML
    void carHandlerDrop(DragEvent event) throws FileNotFoundException {
        file.clear();
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
        file.clear();
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
    void clickAddCarbtn(ActionEvent event) throws SQLException {
        hasData(false,"partsAddBtn");
    }

    @FXML
    void clickAddPartbtn(ActionEvent event) throws SQLException {
        hasData(false,"carsAddBtn");
    }

    @FXML
    void clickCarCancel(ActionEvent event) throws SQLException {
        hasData(false,"carsAdd");
    }

    @FXML
    void clickCarConfirm(ActionEvent event) throws SQLException {
        hasData(true,"carsAdd");
    }

    @FXML
    void clickExportCSV(ActionEvent event) {

    }

    @FXML
    void clickInventoryAdd(ActionEvent event) {
            addPane.setVisible(true);
            editPane.setVisible(false);

            addCarbtn.setDisable(true);
            addPartbtn.setDisable(false);
            addCar.setVisible(true);
            addPart.setVisible(false);
            fadeInOut(true,extraPane,inventoryPane);
    }

    @FXML
    void clickModelsSelectAll(ActionEvent event) {
        setSelect(modelsSelectAll.isSelected(),modelsSelectAll);
    }

    @FXML
    void clickPartCancel(ActionEvent event) throws SQLException {

        hasData(false,"partsAdd");
    }

    @FXML
    void clickPartConfirm(ActionEvent event) throws SQLException {
        hasData(true,"partsAdd");
    }

    @FXML
    void clickRefreshTable(ActionEvent event) {
        setCarsTable();
        setPartsTable();
        inventoryData.clear();
        inventoryData.addAll(carsData);
        inventoryData.addAll(partsData);
        setupSearchBar();
        if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
            showTable("cars");
        }else{
            showTable("parts");
        }
    }

    @FXML
    void clickSearchBtn(ActionEvent event) {
        handleSearch();
    }

    @FXML
    void clickSelect718(ActionEvent event) {
        setSelect(car718.isSelected(),car718);
    }

    @FXML
    void clickSelect911(ActionEvent event) {
        setSelect(car911.isSelected(),car911);
    }

    @FXML
    void clickSelectCayenne(ActionEvent event) {
        setSelect(carCayenne.isSelected(),carCayenne);
    }

    @FXML
    void clickSelectMacan(ActionEvent event) {
        setSelect(carMacan.isSelected(),carMacan);
    }

    @FXML
    void clickSelectPanamera(ActionEvent event) {
        setSelect(carPanamera.isSelected(),carPanamera);
    }
    @FXML
    void clickSelectTaycan(ActionEvent event) {
        setSelect(carTaycan.isSelected(),carTaycan);
    }
    @FXML
    void clickSeriesSelectAll(ActionEvent event) {
        setSelect(seriesSelectAll.isSelected(),seriesSelectAll);
    }
    @FXML
    private void clickCarImage(MouseEvent event) {
        handleImageSelection(carImage);
    }

    @FXML
    private void clickPartImage(MouseEvent event) {
        handleImageSelection(partImage);
    }

    @FXML
    private void clickEditCarImage(MouseEvent event) {
        handleImageSelection(editCarImg);
    }

    @FXML
    private void clickEditPartImage(MouseEvent event) {
        handleImageSelection(editPartImg);
    }

    @FXML
    void extraPaneMouseClick(MouseEvent event) throws SQLException {
        if(addPane.isVisible()) {
            if (addCar.isVisible()) {
                hasData(false, "carsAdd");
            } else if (addPart.isVisible()) {
                hasData(false, "partsAdd");
            }
        }else {
            if (editTitle.getText().contains("Car")) {
                hasData(false, "carsEdit");
            } else {
                hasData(false, "partsEdit");

            }
        }

    }

    @FXML
    void clickInventoryBox(ActionEvent event) {
        String selectedValue = inventoryBox.getValue();

        if ("Cars".equals(selectedValue)) {
            setSelect(true,seriesSelectAll);
            fadeInOut(true,modelsPane,null);
            fadeInOut(true, seriesPane, null);
            showTable("cars");
        } else if ("Parts".equals(selectedValue)) {
            fadeInOut(false,modelsPane,null);
            fadeInOut(false, seriesPane, null);
            showTable("parts");
        }
    }
    @FXML
    void clickBackBtn(MouseEvent event) throws SQLException {
        if(editTitle.getText().contains("Car")) {
            hasData(false, "carsEdit");
        }else{
            hasData(false,"partsEdit");
        }
    }
    //true is in or select and false is out or unselect

    @FXML
    private void initialize(){
            extraPane.setVisible(false);
            inventoryPane.setOpacity(1);
            setCarsTable();
            setPartsTable();
            inventoryData.addAll(carsData);
            inventoryData.addAll(partsData);
            inventoryBox.getItems().addAll("Cars","Parts");
            inventoryBox.setValue("Cars");
            setSelect(true,seriesSelectAll);

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

            idCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getInventoryId()));
            nameCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getName()));
            statusCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getStatus()));
            qtyCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getQty()));
            priceCol.setCellValueFactory(d-> new ReadOnlyObjectWrapper<>(d.getValue().getPrice()));


            showTable("cars");

            carImage.setCursor(Cursor.HAND);
            partImage.setCursor(Cursor.HAND);
            editCarImg.setCursor(Cursor.HAND);
            editPartImg.setCursor(Cursor.HAND);
            carImage.setPickOnBounds(true);
            partImage.setPickOnBounds(true);
            editCarImg.setPickOnBounds(true);
            editPartImg.setPickOnBounds(true);

            setActionColumn();
            setupSearchBar();
    }
    List<File> file = new ArrayList<>();
    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();

    private ObservableList<inventory> inventoryData = FXCollections.observableArrayList();
    private ObservableList<inventory> carsData = FXCollections.observableArrayList();
    private ObservableList<inventory> carsOffData = FXCollections.observableArrayList();
    private HashMap<CheckBox,String > models = new HashMap<>();
    private ObservableList<inventory> partsData = FXCollections.observableArrayList();
    private ObservableList<inventory> partsOffDate = FXCollections.observableArrayList();
    private ObservableList<inventory> searchDate = FXCollections.observableArrayList();
    private inventory editPath;

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

    private void setSelect(boolean check, CheckBox in){
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
        showTable("cars");

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
    //in the path data there is carsAddBtn , partsAddBtn , carsAdd, partsAdd, carsEdit ,partsEdit
    private void hasData(boolean check, String path) throws SQLException {
            try{
                boolean allEmpty = true;
                if (path.contains("carsAdd")) {
                    String img = file.isEmpty() ? "" : String.valueOf(file.get(0));
                    String models = carModelText.getText().trim();
                    String trim = carTrimText.getText().trim();
                    String extColor = carExtColorText.getText().trim();
                    String intColor = carIntColorText.getText().trim();
                    RadioButton selectedFuel = (RadioButton) fuelTypeGroup.getSelectedToggle();
                    String fuel_type = (selectedFuel != null) ? selectedFuel.getText() : "";
                    String qty = carQtyText.getText().trim();
                    String price = carPriceText.getText().trim();

                    allEmpty = img.isEmpty() && models.isEmpty() && trim.isEmpty() &&
                            extColor.isEmpty() && intColor.isEmpty() &&
                            fuel_type.isEmpty() && qty.isEmpty() && price.isEmpty();

                } else if (path.contains("partsAdd")) {
                    String img = file.isEmpty() ? "" : String.valueOf(file.get(0));
                    String name = partNameText.getText().trim();
                    String qty = partQtyText.getText().trim();
                    String price = partPriceText.getText().trim();
                    String description = partDescriptionText.getText().trim();

                    allEmpty = img.isEmpty() && name.isEmpty() && qty.isEmpty() &&
                            price.isEmpty() && description.isEmpty();

                }else if(path.equalsIgnoreCase("carsEdit")){
                    String fuel = "";
                    if (editPath.getFuelType().contains("gas")) {
                        fuel += "Gasoline/";
                    } else if (editPath.getFuelType().contains("hy")) {
                        fuel += "Hybrid/";
                    } else if (editPath.getFuelType().contains("di")) {
                        fuel += "diesel/"; // fixed typo from "diseal"
                    } else {
                        fuel += editPath.getFuelType();
                    }
                    if (editPath.getFuelType().contains("ele")) {
                        fuel += "electric";
                    }
                    boolean sameImage = true;
                    if (!file.isEmpty() && editPath.getPhoto() != null) {
                        File selectedFile = file.get(0);
                        try {
                            String selectedPath = selectedFile.getCanonicalPath();
                            String originalPath = new File(editPath.getPhoto()).getCanonicalPath();
                            sameImage = selectedPath.equals(originalPath);
                        } catch (IOException e) {
                            sameImage = false;
                        }
                    } else if (file.isEmpty() && (editPath.getPhoto() == null || editPath.getPhoto().isEmpty())) {
                        sameImage = true;
                    } else {
                        sameImage = false;
                    }
                    allEmpty = sameImage &&
                            Objects.equals(editCarName.getText(), editPath.getName())
                            && editCarUsage.getText().equals(fuel)
                            && Objects.equals(editCarProductAt.getText(), String.valueOf(editPath.getProductYear()))
                            && Objects.equals(editCarIntColor.getText(), editPath.getIntColor())
                            && Objects.equals(editCarExtColor.getText(), editPath.getExtColor())
                            && Objects.equals(editCarQty.getText(), String.valueOf(editPath.getQty()))
                            && Objects.equals(editCarPrice.getText(), String.valueOf(editPath.getPrice()));


                }else if(path.equalsIgnoreCase("partsEdit")){
                    String photoPath = editPath.getPhoto();
                    boolean sameImage = true;
                    if (!file.isEmpty() && photoPath != null) {
                        File selectedFile = file.get(0);
                        try {
                            String selectedPath = selectedFile.getCanonicalPath();
                            String originalPath = new File(photoPath).getCanonicalPath();
                            sameImage = selectedPath.equals(originalPath);
                        } catch (IOException e) {
                            sameImage = false;
                        }
                    } else if ((file.isEmpty() || file.get(0) == null) && (photoPath == null || photoPath.isEmpty())) {
                        sameImage = true;
                    } else {
                        sameImage = false;
                    }
                    allEmpty =
                            sameImage &&
                                    Objects.equals(editPartName.getText(), editPath.getName())
                                    && Objects.equals(editPartForCar.getText(), editPath.getForCar())
                                    && Objects.equals(editPartDescription.getText(), editPath.getDescription())
                                    && Objects.equals(editPartPrice.getText(), String.valueOf(editPath.getPrice()))
                                    && Objects.equals(editPartQty.getText(), String.valueOf(editPath.getQty()));

                }

                if(check){
                    alertForm(check,path);
                }else  if (allEmpty && (path.equalsIgnoreCase("carsAddBtn") || path.equalsIgnoreCase("carsAdd")
                                        || path.equalsIgnoreCase("partsAddBtn") || path.equalsIgnoreCase("partsAdd")
                                        || path.equalsIgnoreCase("carsEdit") || path.equalsIgnoreCase("partsEdit")
                )){
                    clearCarPartForm(path);
                }else{
                    alertForm(check, path);
                }
            }catch ( IOException e){
                throw new RuntimeException(e);
            }
    }
    //for alernt there have "carsAdd" "partsAdd" "carsEdit" "partsEdit" "partsUpdate" "carsUpdate" "partsDelete" "carsDelete"
    // and if true that will  show the information and if false that will show warning
    private void alertForm(boolean check,String in) throws IOException {

        if(check){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Finished");
            if(in.contains("carsAdd")){
                alert.setContentText("Successfully Added The Car");
            }else if(in.contains("partsAdd")){
                alert.setContentText("Successfully Added The Part");
            }else if(in.contains("carsEdit")){
                alert.setContentText("Successfully Edited The Car");
            }else if(in.contains("partsEdit")){
                alert.setContentText("Successfully Edited The Part");
            }else if(in.contains("partsUpdate")){
                alert.setContentText("Successfully Updated The Part");
            }else if(in.contains("carsUpadate")){
                alert.setContentText("Successfully Updated The Car");
            }
            alert.showAndWait();
            clearCarPartForm(in);
        }else{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/alert.fxml"));
            Parent root = loader.load();

            alertController alertController = loader.getController();

            if(in.contains("carsAdd")){
                alertController.setAlert("You have unsaved changes for adding a car. Are you sure you want to cancel?");
            }else if(in.contains("partsAdd")){
                alertController.setAlert("You have unsaved changes for adding a part. Are you sure you want to cancel?");
            }else if(in.contains("carsEdit")){
                alertController.setAlert("You have unsaved changes for editing a car. Are you sure you want to cancel?");
            }else if(in.contains("partsEdit")){
                alertController.setAlert("You have unsaved changes for editing a part. Are you sure you want to cancel?");
            }else if(in.contains("partsDelete")){
                alertController.setAlert("Do you really want to Delete this part ?");
            }else if(in.contains("carsDelete")){
                alertController.setAlert("Do you really want to Delete this car ?");
            }
            Stage alertStage = new Stage();
            alertStage.initModality(Modality.APPLICATION_MODAL);
            alertStage.initStyle(StageStyle.TRANSPARENT);
            alertStage.setScene(new Scene(root));

            if (in.equals("carsAddBtn") || in.equals("partsAddBtn")
                    || in.equals("carsEdit") || in.equals("partsEdit")
                ){
                alertController.confirmBtn.setOnAction(e -> {
                    clearCarPartForm(in);
                    alertStage.close();
                });
                alertController.cancelBtn.setOnAction(e -> {
                    alertStage.close();
                });
            }else if( in.equals("carsDelete") || in.equals("partsDelete")){
                alertController.confirmBtn.setOnAction(e -> {
                   tableAddUpdateDelete(false);
                    alertStage.close();
                });
                alertController.cancelBtn.setOnAction(e -> {
                    alertStage.close();
                });
            }else {
                alertController.confirmBtn.setOnAction(e -> {
                    clearCarPartForm(in);
                    alertStage.close();
                });
                alertController.cancelBtn.setOnAction(e -> {
                    alertStage.close();
                });
            }

            alertStage.showAndWait();
        }

    }
    // in = carsBtn , partsBtn or null
    private void clearCarPartForm(String in) {
                if (addCarbtn.isDisable()) {
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
                    file.clear();
                }
                if (addPartbtn.isDisable()) {
                    partNameText.clear();
                    partDescriptionText.clear();
                    partPriceText.clear();
                    partQtyText.clear();
                    partImage.setImage(null);
                    partRelativeComboBox.getSelectionModel().clearSelection();
                    file.clear();
                }
                if(editPane.isVisible()){
                    if(editTitle.getText().contains("C")){
                        editCarId.setText(null);
                        editCarSeries.setText(null);
                        editCarName.setText(null);

                        editCarExtColor.setText(null);
                        editCarIntColor.setText(null);
                        editCarQty.setText(null);
                        editCarPrice.setText(null);
                        editCarProductAt.setText(null);
                        editCarUsage.setText(null);
                        editCarImg.setImage(null);
                    }else{
                        editPartId.setText(null);
                        editPartName.setText(null);
                        editPartDescription.setText(null);
                        editPartForCar.setText(null);
                        editPartQty.setText(null);
                        editPartPrice.setText(null);
                        editPartImg.setImage(null);
                        file.clear();
                    }
                    file.clear();
                }
                if (in.equals("carsAddBtn")) {
                    addCarbtn.setDisable(false);
                    addPartbtn.setDisable(true);
                    fadeInOut(true, addPart, addCar);
                } else if (in.equals("partsAddBtn")) {
                    addCarbtn.setDisable(true);
                    addPartbtn.setDisable(false);
                    fadeInOut(true, addCar, addPart);
                } else {
                    fadeInOut(false, extraPane, inventoryPane);
                }


    }
    private void setActionColumn() {
        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionCol.setCellFactory(param -> new TableCell<inventory, inventory>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttonsContainer = new HBox(8, editButton, deleteButton);
            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BAN));
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                editButton.setCursor(Cursor.HAND);
                deleteButton.setCursor(Cursor.HAND);
                addHoverAnimation(editButton);
                addHoverAnimation(deleteButton);
                // Set click actions
                editButton.setOnAction(event -> {
                    inventory item = getTableView().getItems().get(getIndex());
                    editPath = item;
                    editTable();
                });

                deleteButton.setOnAction(event -> {
                    inventory item = getTableView().getItems().get(getIndex());
                    editPath = item;
                    try {
                        if (item.getInventoryId().contains("C")) {
                            alertForm(false, "carsDelete");
                        } else {
                            alertForm(false, "partsDelete");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });

                buttonsContainer.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(inventory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setStyle(""); // Clear any previous styling
                } else {
                    setGraphic(buttonsContainer);

                    // Style based on availability
                    if ("Out".equals(item.getStatus()) || item.getQty() <= 0) {
                        // Gray out unavailable items
                        setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #6c757d;");
                        // Also style the buttons to be less prominent
                        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d;");
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d;");
                    } else {
                        // Normal styling for available items
                        setStyle("");
                        editButton.setStyle("-fx-background-color: transparent;");
                        deleteButton.setStyle("-fx-background-color: transparent;");
                    }
                }
            }
            private void addHoverAnimation(Button button) {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
                scaleUp.setToX(1.2);
                scaleUp.setToY(1.2);

                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);

                RotateTransition shake = new RotateTransition(Duration.millis(100), button);
                shake.setFromAngle(-5);
                shake.setToAngle(5);
                shake.setCycleCount(2);
                shake.setAutoReverse(true);

                button.setOnMouseEntered(e -> {
                    // Reset rotation just in case
                    button.setRotate(0);
                    scaleUp.playFromStart();
                    shake.playFromStart();
                });

                button.setOnMouseExited(e -> {
                    scaleDown.playFromStart();
                    // Ensure icon returns to normal angle
                    scaleDown.setOnFinished(ev -> button.setRotate(0));
                });
            }
        });
    }
    // for showTable check ("cars" , "parts" ,"search")
    private void showTable(String check){
        int sItems =0;
        int total = 0;
       if("cars".equals(check)){
           sItems = showCarsTable();
           total = carsData.size();
       }else if("parts".equals(check)){
          sItems = showPartsTable();
          total = partsData.size();
       }else{
           sItems = showSearchItemTable();
           total = inventoryData.size();
       }

        showTableRows.setText("Showing " + total + " of " + sItems + " items");
    }

    private void showAvailableItems(){
        // Show currently available items based on the selected inventory type
        if (inventoryBox.getValue().equalsIgnoreCase("Cars")) {
            showTable("cars");
        } else {
            showTable("parts");
        }
    }

    private void showUnavailableItems(){
        // Show items marked as Unavailable based on the selected inventory type
        inventoryTable.getItems().clear();
        if (inventoryBox.getValue().equalsIgnoreCase("Cars")) {
            inventoryTable.setItems(carsOffData);
            showTableRows.setText("Showing " + carsOffData.size() + " of " + carsOffData.size() + " items");
        } else {
            inventoryTable.setItems(partsOffDate);
            showTableRows.setText("Showing " + partsOffDate.size() + " of " + partsOffDate.size() + " items");
        }
    }

    private void setCarsTable(){
        try{
            carsData.clear();
            partsData.clear();
            models.clear();
            CallableStatement cs = con.prepareCall("CALL getAllCars()");
            ResultSet rs = cs.executeQuery();
            while (rs.next()){
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String extColor = rs.getString(3);
                String intColor = rs.getString(4);
                String fuels = rs.getString(5);
                int productYear = rs.getInt(6);
                int qty = rs.getInt(7);
                Double price = rs.getDouble(8);
                String photoUrl = rs.getString(9);
                Boolean check = rs.getBoolean(10);
                String status = (qty !=0) ?"On" : "Out";
                if(!check && qty == 0){
                    status = "Unavailable";
                }
                String inventoryId = String.format("C-%03d", id);
                if(status.equals("Unavailable")){
                    carsOffData.add(new inventory(id, inventoryId, name, extColor, intColor, fuels, productYear, qty, price, status, photoUrl));
                }else {
                    carsData.add(new inventory(id, inventoryId, name, extColor, intColor, fuels, productYear, qty, price, status, photoUrl));
                }

            }

            for(inventory i : carsData){
                models.put(new CheckBox(i.getModels()),i.getSeries());
            }
            cs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void setPartsTable(){
        try {
            partsOffDate.clear();
            partsData.clear();
            CallableStatement cs = con.prepareCall("CALL getAllParts()");
            ResultSet rs = cs.executeQuery();
            while ((rs.next())){
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String forCar = rs.getString(3);
                String description = rs.getString(4);
                int qty = rs.getInt(5);
                Double price = rs.getDouble(6);
                String photoUrl = rs.getString(7);
                Boolean check = rs.getBoolean(8);

                String status = (qty !=0) ?"On" : "Out";
                if(!check && qty == 0){
                    status = "Unavailable";
                }
                String inventoryId = String.format("P-%03d", id);
                if(status.equals("Unavailable")){
                    partsOffDate.add(new inventory(id,inventoryId,name,forCar,description,qty,price,status,photoUrl));
                }else{
                    partsData.add(new inventory(id,inventoryId,name,forCar,description,qty,price,status,photoUrl));
                }

            }
            cs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private int showCarsTable(){
        inventoryTable.getItems().clear();
        boolean selectAll = seriesSelectAll.isSelected();
        ObservableList<inventory> filterData = FXCollections.observableArrayList();
        String c911 = car911.isSelected() ? "911" : "";
        String c718 = car718.isSelected() ? "718":"";
        String cCayenne = carCayenne.isSelected() ? "cayenne" : "";
        String cMacan = carMacan.isSelected()? "macan":"";
        String cPanamera = carPanamera.isSelected()? "panamera":"";
        String cTaycan = carTaycan.isSelected()? "taycan" : "";

        if(selectAll){
            filterData.addAll(carsData);
        }else{
            for(inventory i : carsData){

                if(     (c911.contains(i.getSeries()) && !c911.isBlank()) ||
                        (c718.contains(i.getSeries()) && !c718.isBlank()) ||
                        (cCayenne.contains(i.getSeries()) && !cCayenne.isBlank()) ||
                        (cMacan.contains(i.getSeries()) && !cMacan.isBlank()) ||
                        (cPanamera.contains(i.getSeries()) && !cPanamera.isBlank()) ||
                        (cTaycan.contains(i.getSeries()) && !cTaycan.isBlank())
                    ){
                    filterData.add(i);

                }
            }
        }
        ArrayList<CheckBox> in = new ArrayList<>();
        for(inventory i : filterData) {
            for (HashMap.Entry<CheckBox, String> entry : models.entrySet()) {
                if(i.getModels().contains(entry.getValue())) {
                    in.add(entry.getKey());

                }
            }
        }
        setModels(in);
        inventoryTable.setItems(filterData);
        return  filterData.size();
    }
    private int showPartsTable(){
        inventoryTable.getItems().clear();
        ObservableList<inventory> filterData = FXCollections.observableArrayList();
        filterData.addAll(partsData);
        inventoryTable.setItems(filterData);
        return partsData.size();
    }
    private ContextMenu searchSuggestions = new ContextMenu();
    private void setupSearchBar() {
        // Set up key listener for Enter key
        searchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });
        // Set up text change listener for suggestions
        searchBar.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                searchSuggestions.hide();
                return;
            }
            // Clear previous suggestions
            searchSuggestions.getItems().clear();

            // Find matching staff
            List<MenuItem> matches = new ArrayList<>();
            for (inventory i : inventoryData) {
                String searchText = newText.toLowerCase();
                String id = String.valueOf(i.getInventoryId().toLowerCase());
                String name = i.getName().toLowerCase();

                if (id.contains(searchText) || name.contains(searchText)) {
                    String suggestionText = i.getInventoryId() + " - " + i.getName();
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        searchBar.setText(suggestionText);
                        searchSuggestions.hide();
                        searchDate.clear();
                        searchDate.add(i);
                        showTable("search");
                    });

                    matches.add(item);
                }
            }

            // Show suggestions if matches found
            if (!matches.isEmpty()) {
                searchSuggestions.getItems().addAll(matches);
                searchSuggestions.show(searchBar, Side.BOTTOM, 0, 0);
            } else {
                searchSuggestions.hide();
            }
        });

        // Hide suggestions when text field loses focus
        searchBar.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                searchSuggestions.hide();
            }
        });
    }
    private void handleSearch() {
        String searchText = searchBar.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }

        // Try to find a matching staff member
        for (inventory i :inventoryData) {
            String id = i.getInventoryId();
            String name = i.getName();

            // Check if search text matches ID or name
            if (searchText.equalsIgnoreCase(id) ||
                    searchText.equalsIgnoreCase(name) ||
                    searchText.equalsIgnoreCase(id + " - " + name)) {
                    searchDate.clear();
                    searchDate.add(i);
                    showTable("search");
                return;
            }
        }
    }

    private int showSearchItemTable(){
        inventoryTable.getItems().clear();
        ObservableList<inventory> filterData = FXCollections.observableArrayList();
        filterData.addAll(searchDate);
        inventoryTable.setItems(filterData);
        return searchDate.size();
    }

    private void editTable(){
        addPane.setVisible(false);
        editPane.setVisible(true);
        if (editPath.getInventoryId().contains("C")){
            editCar.setVisible(true);
            editPart.setVisible(false);
            editTitle.setText("(Car)");
            setEditCar();
            editCarRevert.setOnAction(e->{

                setEditCar();
            });
            editCarApply.setOnAction(e->{
                tableAddUpdateDelete(true);
            });
        }else{
            editCar.setVisible(false);
            editPart.setVisible(true);
            editTitle.setText("(Part)");
            setEditPart();
            editPartRevert.setOnAction(e->{
                setEditPart();
            });
            editPartApply.setOnAction(e->{
                tableAddUpdateDelete(true);
            });
        }
        fadeInOut(true,extraPane,inventoryPane);
    }
    private void setEditCar(){
        editCarId.setText(editPath.getInventoryId());
        editCarSeries.setText(editPath.getSeries());
        editCarName.setText(editPath.getName());
        if (editPath.getPhoto() != null && !editPath.getPhoto().trim().isEmpty()) {
            file.clear();
            file.add(new File(editPath.getPhoto()));
            if (!file.isEmpty()) {

                try {
                    Image img = new Image(new FileInputStream(file.get(0)));
                    editCarImg.setImage(img);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        editCarExtColor.setText(editPath.getExtColor());
        editCarIntColor.setText(editPath.getIntColor());
        editCarQty.setText(String.valueOf(editPath.getQty()));
        editCarPrice.setText(String.valueOf(editPath.getPrice()));
        editCarProductAt.setText(String.valueOf(editPath.getProductYear()));
        String fuel = "";
        if(editPath.getFuelType().contains("gas")){
            fuel += "Gasoline/";
        }else if(editPath.getFuelType().contains("hy")){
            fuel += "Hybrid/";
        }else if(editPath.getFuelType().contains("di")){
            fuel += "diseal/";
        }else {
            fuel +=editPath.getFuelType();
        }
        if(editPath.getFuelType().contains("ele")){
            fuel += "electric";
        }
        editCarUsage.setText(fuel);
    }
    private void setEditPart(){
        editPartId.setText(editPath.getInventoryId());
        editPartName.setText(editPath.getName());
        editPartDescription.setText(editPath.getDescription());
        editPartForCar.setText(editPath.getForCar());
        editPartQty.setText(String.valueOf(editPath.getQty()));
        editPartPrice.setText(String.valueOf(editPath.getPrice()));
        if (editPath.getPhoto() != null && !editPath.getPhoto().trim().isEmpty()) {
            file.clear();
            file.add(new File(editPath.getPhoto()));
            if (!file.isEmpty()) {

                try {
                    Image img = new Image(new FileInputStream(file.get(0)));
                    editPartImg.setImage(img);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private void tableAddUpdateDelete(boolean check){
        try {
            if (check) {
                if(addPane.isVisible()){

                }else {
                    if (editPath.getInventoryId().contains("C")) {

                    } else {

                    }
                }


            } else {
                if (editPath.getInventoryId().contains("C")) {
                    String sql = "UPDATE cars SET car_status= ? WHERE car_id = ? ";
                    PreparedStatement ps = con.prepareCall(sql);
                    ps.setBoolean(1,false);
                    ps.setInt(2,editPath.getId());
                    ps.execute();
                    for (inventory i : carsData){
                        if (i == editPath){
                            carsData.clear();
                        }

                    }
                    ps.close();

                } else {
                    String sql = "DELETE FROM car_parts WHERE part_id = ? ";
                    PreparedStatement ps = con.prepareCall(sql);
                    ps.setInt(1,editPath.getId());
                    ps.execute();
                    ps.close();
                    for(inventory i : partsData){
                        if(i == editPath){
                            partsData.clear();
                        }
                    }
                }

                for(inventory i : inventoryData){
                    if(i == editPath){
                        inventoryData.clear();
                    }
                }

            }
            if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                showTable("cars");
            }else{
                showTable("parts");
            }
        } catch ( SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setModels(ArrayList<CheckBox> in) {
        modelsShowBox.getChildren().clear();
        modelsCol1.setHgrow(Priority.ALWAYS);
        modelsCol2.setHgrow(Priority.ALWAYS);

        for (int i = 0; i < in.size(); i++) {
            int row = i / 2;
            int col = i % 2;

            CheckBox newBox = new CheckBox(in.get(i).getText());
            newBox.setSelected(in.get(i).isSelected());
            newBox.setOnAction(e->{

            });
            modelsShowBox.add(newBox, col, row);
        }

        modelsShowBox.setVisible(true);
    }

    private void handleImageSelection(ImageView targetImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(targetImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                targetImageView.setImage(image);
                file.clear();
                file.add(selectedFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


}
