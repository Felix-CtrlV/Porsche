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
import java.io.*;
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
    private Label carPriceLbl;

    @FXML
    private TextField carPriceText;

    @FXML
    private Label carQtyLbl;

    @FXML
    private TextField carQtyText;

    @FXML
    private Label carModelLbl;

    @FXML
    private TextField carModelText;

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
    private RadioButton electricRadio;

    @FXML
    private Button exportCSV;

    @FXML
    private BorderPane extraPane;

    @FXML
    private ToggleGroup fuelTypeGroup;

    @FXML
    private Label fuelTypeLbl;

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
    private TextField partRelativeComboBox;

    @FXML
    private Label partRelativeLbl;

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        // Set default file name based on current view
        String defaultFileName = inventoryBox.getValue() + "_inventory_" + 
                                 java.time.LocalDate.now() + ".csv";
        fileChooser.setInitialFileName(defaultFileName);
        
        // Show save dialog
        Stage stage = (Stage) exportCSV.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Get current table items
                ObservableList<inventory> items = inventoryTable.getItems();
                
                if (items.isEmpty()) {
                    showAlert("No Data", "There is no data to export.", Alert.AlertType.WARNING);
                    return;
                }
                
                // Determine if we're exporting cars or parts based on first item
                boolean isCar = items.get(0).getSeries() != null && !items.get(0).getSeries().isEmpty();
                
                // Write CSV header
                if (isCar) {
                    writer.write("ID,Name,Series,Exterior Color,Interior Color,Fuel Type,Production Year,Quantity,Price,Status");
                } else {
                    writer.write("ID,Name,For Car,Description,Quantity,Price,Status");
                }
                writer.newLine();
                
                // Write data rows
                for (inventory item : items) {
                    if (isCar) {
                        writer.write(escapeCSV(item.getInventoryId()) + ",");
                        writer.write(escapeCSV(item.getName()) + ",");
                        writer.write(escapeCSV(item.getSeries()) + ",");
                        writer.write(escapeCSV(item.getExtColor()) + ",");
                        writer.write(escapeCSV(item.getIntColor()) + ",");
                        writer.write(escapeCSV(item.getFuelType()) + ",");
                        writer.write(item.getProductYear() + ",");
                        writer.write(item.getQty() + ",");
                        writer.write(item.getPrice() + ",");
                        writer.write(escapeCSV(item.getStatus()));
                    } else {
                        writer.write(escapeCSV(item.getInventoryId()) + ",");
                        writer.write(escapeCSV(item.getName()) + ",");
                        writer.write(escapeCSV(item.getForCar()) + ",");
                        writer.write(escapeCSV(item.getDescription()) + ",");
                        writer.write(item.getQty() + ",");
                        writer.write(item.getPrice() + ",");
                        writer.write(escapeCSV(item.getStatus()));
                    }
                    writer.newLine();
                }
                
                showAlert("Export Successful", "Data exported successfully to:\n" + file.getAbsolutePath(), 
                         Alert.AlertType.INFORMATION);
                
            } catch (IOException e) {
                showAlert("Export Failed", "Failed to export data: " + e.getMessage(), 
                         Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        
        // Preserve the current view state (Available or Unavailable)
        if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
            showUnavailableItems();
        } else {
            if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                showTable("cars");
            }else{
                showTable("parts");
            }
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

        // Refresh data from database to ensure latest state
        setCarsTable();
        setPartsTable();
        inventoryData.clear();
        inventoryData.addAll(carsData);
        inventoryData.addAll(partsData);

        if ("Cars".equals(selectedValue)) {
            setSelect(true,seriesSelectAll);
            fadeInOut(true, seriesPane, null);
            // Check if we're in unavailable view
            if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
                showUnavailableItems();
            } else {
                showTable("cars");
            }
        } else if ("Parts".equals(selectedValue)) {
            fadeInOut(false, seriesPane, null);
            // Check if we're in unavailable view
            if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
                showUnavailableItems();
            } else {
                showTable("parts");
            }
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
            setupFloatingLabel(partRelativeComboBox, partRelativeLbl);

            setupFloatingLabel(carModelText, carModelLbl);
            setupFloatingLabel(carTrimText, carTrimLbl);
            setupFloatingLabel(carYearText, carYearLbl);
            setupFloatingLabel(carQtyText, carQtyLbl);
            setupFloatingLabel(carPriceText, carPriceLbl);
            setupFloatingLabel(carExtColorText, carExtColorLbl);
            setupFloatingLabel(carIntColorText, carIntColorLbl);

            // Setup autocomplete for part car names (only once)
            populateCarNamesComboBox();
            populateEditPartCarNames();

            // Setup fuel type asterisk toggle
            fuelTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                String labelText = fuelTypeLbl.getText();
                if (newToggle != null) {
                    // Radio button selected - remove asterisk
                    if (labelText.contains(" *")) {
                        fuelTypeLbl.setText(labelText.replace(" *", ""));
                    }
                } else {
                    // No selection - add asterisk
                    if (!labelText.contains(" *") && !labelText.endsWith("*")) {
                        fuelTypeLbl.setText(labelText + " *");
                    }
                }
            });

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
            setupRowFactory();
    }
    List<File> file = new ArrayList<>();
    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();

    private ObservableList<inventory> inventoryData = FXCollections.observableArrayList();
    private ObservableList<inventory> carsData = FXCollections.observableArrayList();
    private ObservableList<inventory> carsOffData = FXCollections.observableArrayList();
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

        } else {
            seriesSelectAll.setSelected(false);

            in.setSelected(check);

            if(carTaycan.isSelected() && car911.isSelected() &&
                    carCayenne.isSelected() && car718.isSelected() &&
                    carMacan.isSelected() && carPanamera.isSelected()
                ){
                seriesSelectAll.setSelected(true);

            }


        }
        
        // Check current view state and maintain it
        if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
            showUnavailableItems();
        } else {
            showTable("cars");
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
        
        // Hide/show asterisk based on text content
        field.textProperty().addListener((obs, oldText, newText) -> {
            String labelText = label.getText();
            if (newText != null && !newText.trim().isEmpty()) {
                // Has text - remove asterisk if present
                if (labelText.contains(" *")) {
                    label.setText(labelText.replace(" *", ""));
                }
            } else {
                // Empty - add asterisk if not present
                if (!labelText.contains(" *") && !labelText.endsWith("*")) {
                    label.setText(labelText + " *");
                }
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
                    String trim = carTrimText.getText().trim();
                    String extColor = carExtColorText.getText().trim();
                    String intColor = carIntColorText.getText().trim();
                    RadioButton selectedFuel = (RadioButton) fuelTypeGroup.getSelectedToggle();
                    String fuel_type = (selectedFuel != null) ? selectedFuel.getText() : "";
                    String qty = carQtyText.getText().trim();
                    String price = carPriceText.getText().trim();

                    allEmpty = img.isEmpty() && trim.isEmpty() &&
                            extColor.isEmpty() && intColor.isEmpty() &&
                            fuel_type.isEmpty() && qty.isEmpty() && price.isEmpty();

                } else if (path.contains("partsAdd")) {
                    String img = file.isEmpty() ? "" : String.valueOf(file.get(0));
                    String name = partNameText.getText().trim();
                    String qty = partQtyText.getText().trim();
                    String price = partPriceText.getText().trim();
                    String description = partDescriptionText.getText().trim();
                    String forCar = partRelativeComboBox.getText().trim();

                    allEmpty = img.isEmpty() && name.isEmpty() && qty.isEmpty() &&
                            price.isEmpty() && description.isEmpty() && forCar.isEmpty();

                }else if(path.equalsIgnoreCase("carsEdit")){
                    // Convert database fuel type abbreviations to display names
                    String fuelType = editPath.getFuelType().toUpperCase().trim();
                    String fuel = "";
                    
                    switch(fuelType) {
                        case "PET":
                            fuel = "Petrol";
                            break;
                        case "HYB":
                            fuel = "Hybrid (Petrol+Electric)";
                            break;
                        case "ELEC":
                            fuel = "Electric";
                            break;
                        case "DSL":
                            fuel = "Diesel";
                            break;
                        default:
                            fuel = editPath.getFuelType();
                            break;
                    }
                    
                    boolean sameImage = true;
                    boolean hasOriginalPhoto = editPath.getPhoto() != null 
                                                && !editPath.getPhoto().trim().isEmpty() 
                                                && !editPath.getPhoto().trim().equalsIgnoreCase("null");
                    boolean hasNewPhoto = !file.isEmpty();
                    
                    if (!hasNewPhoto) {
                        // No new photo selected - image hasn't changed
                        sameImage = true;
                    } else if (hasOriginalPhoto && hasNewPhoto) {
                        // Both have photos - compare them
                        File selectedFile = file.get(0);
                        try {
                            String selectedPath = selectedFile.getCanonicalPath();
                            // Resolve the original path to handle relative paths
                            File originalFile = resolveImagePath(editPath.getPhoto());
                            String originalPath = originalFile != null ? originalFile.getCanonicalPath() : editPath.getPhoto();
                            sameImage = selectedPath.equals(originalPath);
                        } catch (IOException e) {
                            // If we can't resolve paths, assume they're the same if file exists
                            sameImage = true;
                        }
                    } else if (!hasOriginalPhoto && hasNewPhoto) {
                        // Original had no photo, but new photo selected - different
                        sameImage = false;
                    }
                    allEmpty = sameImage &&
                            Objects.equals(editCarName.getText().trim(), editPath.getName() != null ? editPath.getName().trim() : null)
                            && Objects.equals(editCarUsage.getText().trim(), fuel)
                            && Objects.equals(editCarProductAt.getText().trim(), String.valueOf(editPath.getProductYear()))
                            && Objects.equals(editCarIntColor.getText().trim(), editPath.getIntColor() != null ? editPath.getIntColor().trim() : null)
                            && Objects.equals(editCarExtColor.getText().trim(), editPath.getExtColor() != null ? editPath.getExtColor().trim() : null)
                            && Objects.equals(editCarQty.getText().trim(), String.valueOf(editPath.getQty()))
                            && Objects.equals(editCarPrice.getText().trim(), String.valueOf(editPath.getPrice()));


                }else if(path.equalsIgnoreCase("partsEdit")){
                    String photoPath = editPath.getPhoto();
                    boolean sameImage = true;
                    boolean hasOriginalPhoto = photoPath != null && !photoPath.trim().isEmpty() && !photoPath.trim().equalsIgnoreCase("null");
                    boolean hasNewPhoto = !file.isEmpty();
                    
                    if (!hasNewPhoto) {
                        // No new photo selected - image hasn't changed
                        sameImage = true;
                    } else if (hasOriginalPhoto && hasNewPhoto) {
                        // Both have photos - compare them
                        File selectedFile = file.get(0);
                        try {
                            String selectedPath = selectedFile.getCanonicalPath();
                            // Resolve the original path to handle relative paths
                            File originalFile = resolveImagePath(photoPath);
                            String originalPath = originalFile != null ? originalFile.getCanonicalPath() : photoPath;
                            sameImage = selectedPath.equals(originalPath);
                        } catch (IOException e) {
                            // If we can't resolve paths, assume they're the same if file exists
                            sameImage = true;
                        }
                    } else if (!hasOriginalPhoto && hasNewPhoto) {
                        // Original had no photo, but new photo selected - different
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
    //for alernt there have "carsAdd" "partsAdd" "carsEdit" "partsEdit" "partsUpdate" "carsUpdate" "partsDelete" "carsDelete" "carsRestore" "partsRestore"
    // and if true that will  show the information and if false that will show warning
    private void alertForm(boolean check,String in) throws IOException {

        if(check){
            // Perform the actual insert/add operation
            try {
                if(in.contains("carsAdd")){
                    insertCar();
                }else if(in.contains("partsAdd")){
                    insertPart();
                }
            } catch (SQLException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Failed to add: " + e.getMessage());
                errorAlert.showAndWait();
                return;
            }
            
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
            }else if(in.contains("carsUpdate")){
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
            }else if(in.contains("partsRestore")){
                alertController.setAlert("Do you want to restore this part?");
            }else if(in.contains("carsRestore")){
                alertController.setAlert("Do you want to restore this car?");
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
            }else if( in.equals("carsRestore") || in.equals("partsRestore")){
                alertController.confirmBtn.setOnAction(e -> {
                   tableAddUpdateDelete(true);
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
                    carYearText.clear();
                    carModelText.clear();
                    carTrimText.clear();
                    carExtColorText.clear();
                    carIntColorText.clear();
                    carPriceText.clear();
                    carQtyText.clear();
                    carImage.setImage(null);
                    gasolineRadio.setSelected(false);
                    electricRadio.setSelected(false);
                    hybridRadio.setSelected(false);
                    dieselRadio.setSelected(false);
                    file.clear();
                }
                if (addPartbtn.isDisable()) {
                    partNameText.clear();
                    partDescriptionText.clear();
                    partPriceText.clear();
                    partQtyText.clear();
                    partImage.setImage(null);
                    partRelativeComboBox.clear();
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
                } else if (in.equals("carsUpdate") || in.equals("partsUpdate")) {
                    // Close edit pane after update
                    editPane.setVisible(false);
                    fadeInOut(false, extraPane, inventoryPane);
                } else {
                    fadeInOut(false, extraPane, inventoryPane);
                }


    }
    
    private ContextMenu partCarSuggestions = new ContextMenu();
    private ContextMenu editPartCarSuggestions = new ContextMenu();
    
    private void populateCarNamesComboBox() {
        // Set up autocomplete for part relative car field
        partRelativeComboBox.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                partCarSuggestions.hide();
                return;
            }
            // Clear previous suggestions
            partCarSuggestions.getItems().clear();

            // Find matching cars
            List<MenuItem> matches = new ArrayList<>();
            String searchText = newText.toLowerCase();
            
            // Search in available cars
            for (inventory car : carsData) {
                String carName = car.getName().toLowerCase();
                if (carName.contains(searchText)) {
                    String suggestionText = car.getName();
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        partRelativeComboBox.setText(suggestionText);
                        partCarSuggestions.hide();
                    });

                    matches.add(item);
                }
            }
            
            // Search in unavailable cars
            for (inventory car : carsOffData) {
                String carName = car.getName().toLowerCase();
                if (carName.contains(searchText)) {
                    String suggestionText = car.getName() + " (Unavailable)";
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        partRelativeComboBox.setText(car.getName());
                        partCarSuggestions.hide();
                    });

                    matches.add(item);
                }
            }

            // Show suggestions if matches found
            if (!matches.isEmpty()) {
                partCarSuggestions.getItems().addAll(matches);
                partCarSuggestions.show(partRelativeComboBox, Side.BOTTOM, 0, 0);
            } else {
                partCarSuggestions.hide();
            }
        });

        // Hide suggestions when text field loses focus
        partRelativeComboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                partCarSuggestions.hide();
            }
        });
    }
    
    private void populateEditPartCarNames() {
        // Set up autocomplete for edit part car field
        editPartForCar.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                editPartCarSuggestions.hide();
                return;
            }
            // Clear previous suggestions
            editPartCarSuggestions.getItems().clear();

            // Find matching cars
            List<MenuItem> matches = new ArrayList<>();
            String searchText = newText.toLowerCase();
            
            // Search in available cars
            for (inventory car : carsData) {
                String carName = car.getName().toLowerCase();
                if (carName.contains(searchText)) {
                    String suggestionText = car.getName();
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        editPartForCar.setText(suggestionText);
                        editPartCarSuggestions.hide();
                    });

                    matches.add(item);
                }
            }
            
            // Search in unavailable cars
            for (inventory car : carsOffData) {
                String carName = car.getName().toLowerCase();
                if (carName.contains(searchText)) {
                    String suggestionText = car.getName() + " (Unavailable)";
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        editPartForCar.setText(car.getName());
                        editPartCarSuggestions.hide();
                    });

                    matches.add(item);
                }
            }

            // Show suggestions if matches found
            if (!matches.isEmpty()) {
                editPartCarSuggestions.getItems().addAll(matches);
                editPartCarSuggestions.show(editPartForCar, Side.BOTTOM, 0, 0);
            } else {
                editPartCarSuggestions.hide();
            }
        });

        // Hide suggestions when text field loses focus
        editPartForCar.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                editPartCarSuggestions.hide();
            }
        });
    }
    
    private void setActionColumn() {
        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionCol.setCellFactory(param -> new TableCell<inventory, inventory>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button restoreButton = new Button();
            private final HBox buttonsContainer = new HBox(8);
            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BAN));
                restoreButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.UNDO));
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                restoreButton.setStyle("-fx-background-color: transparent;");
                editButton.setCursor(Cursor.HAND);
                deleteButton.setCursor(Cursor.HAND);
                restoreButton.setCursor(Cursor.HAND);
                addHoverAnimation(editButton);
                addHoverAnimation(deleteButton);
                addHoverAnimation(restoreButton);
                
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

                restoreButton.setOnAction(event -> {
                    inventory item = getTableView().getItems().get(getIndex());
                    editPath = item;
                    try {
                        if (item.getInventoryId().contains("C")) {
                            alertForm(false, "carsRestore");
                        } else {
                            alertForm(false, "partsRestore");
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
                    // Clear and rebuild buttons based on status
                    buttonsContainer.getChildren().clear();
                    
                    if ("Unavailable".equals(item.getStatus())) {
                        // Show only restore button for unavailable items (status = false)
                        buttonsContainer.getChildren().add(restoreButton);
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                        restoreButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #28a745;");
                    } else {
                        // Status is true (available) - show edit and delete buttons
                        buttonsContainer.getChildren().addAll(editButton, deleteButton);
                        
                        // Apply styling based on quantity
                        if ("Out".equals(item.getStatus()) || item.getQty() <= 0) {
                            // Gray out out of stock items but keep both buttons
                            setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #6c757d;");
                            editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d;");
                            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d;");
                        } else {
                            // Normal styling for available items with stock
                            setStyle("");
                            editButton.setStyle("-fx-background-color: transparent;");
                            deleteButton.setStyle("-fx-background-color: transparent;");
                        }
                    }
                    
                    setGraphic(buttonsContainer);
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
    
    private void setupRowFactory() {
        inventoryTable.setRowFactory(tv -> new TableRow<inventory>() {
            @Override
            protected void updateItem(inventory item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setStyle("");
                } else {
                    // Apply different colors based on status
                    if ("Unavailable".equals(item.getStatus())) {
                        // Yellow/warning color for items with qty but marked unavailable
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else if ("Out".equals(item.getStatus()) || item.getQty() <= 0) {
                        // Light gray for out of stock items
                        setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #6c757d;");
                    } else {
                        // Normal styling for available items
                        setStyle("");
                    }
                }
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
            // Apply series filter to unavailable cars
            boolean selectAll = seriesSelectAll.isSelected();
            ObservableList<inventory> filterData = FXCollections.observableArrayList();
            String c911 = car911.isSelected() ? "911" : "";
            String c718 = car718.isSelected() ? "718":"";
            String cCayenne = carCayenne.isSelected() ? "cayenne" : "";
            String cMacan = carMacan.isSelected()? "macan":"";
            String cPanamera = carPanamera.isSelected()? "panamera":"";
            String cTaycan = carTaycan.isSelected()? "taycan" : "";

            if(selectAll){
                filterData.addAll(carsOffData);
            }else{
                // Filter by series
                for(inventory i : carsOffData){
                    String series = i.getSeries().toLowerCase().trim();
                    
                    if(     (c911.equals(series) && !c911.isBlank()) ||
                            (c718.equals(series) && !c718.isBlank()) ||
                            (cCayenne.equals(series) && !cCayenne.isBlank()) ||
                            (cMacan.equals(series) && !cMacan.isBlank()) ||
                            (cPanamera.equals(series) && !cPanamera.isBlank()) ||
                            (cTaycan.equals(series) && !cTaycan.isBlank())
                        ){
                        filterData.add(i);
                    }
                }
            }
            
            inventoryTable.setItems(filterData);
            showTableRows.setText("Showing " + filterData.size() + " of " + carsOffData.size() + " items");
        } else {
            inventoryTable.setItems(partsOffDate);
            showTableRows.setText("Showing " + partsOffDate.size() + " of " + partsOffDate.size() + " items");
        }
    }

    private void setCarsTable(){
        try{
            carsData.clear();
            carsOffData.clear();
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
                String inventoryId = String.format("C-%03d", id);
                
                // Separate based on check boolean and qty
                if(check){
                    // Available items (check = true)
                    carsData.add(new inventory(id, inventoryId, name, extColor, intColor, fuels, productYear, qty, price, status, photoUrl));
                }else {
                    // check = false
                    if(qty > 0){
                        // Has quantity but marked unavailable - show in available table with different color
                        status = "Unavailable";
                        carsData.add(new inventory(id, inventoryId, name, extColor, intColor, fuels, productYear, qty, price, status, photoUrl));
                    }else{
                        // No quantity and marked unavailable - show in unavailable table
                        status = "Unavailable";
                        carsOffData.add(new inventory(id, inventoryId, name, extColor, intColor, fuels, productYear, qty, price, status, photoUrl));
                    }
                }

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
                String inventoryId = String.format("P-%03d", id);
                
                // Separate based on check boolean and qty
                if(check){
                    // Available items (check = true)
                    partsData.add(new inventory(id,inventoryId,name,forCar,description,qty,price,status,photoUrl));
                }else{
                    // check = false
                    if(qty > 0){
                        // Has quantity but marked unavailable - show in available table with different color
                        status = "Unavailable";
                        partsData.add(new inventory(id,inventoryId,name,forCar,description,qty,price,status,photoUrl));
                    }else{
                        // No quantity and marked unavailable - show in unavailable table
                        status = "Unavailable";
                        partsOffDate.add(new inventory(id,inventoryId,name,forCar,description,qty,price,status,photoUrl));
                    }
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
            // Filter by series
            for(inventory i : carsData){
                String series = i.getSeries().toLowerCase().trim();
                
                if(     (c911.equals(series) && !c911.isBlank()) ||
                        (c718.equals(series) && !c718.isBlank()) ||
                        (cCayenne.equals(series) && !cCayenne.isBlank()) ||
                        (cMacan.equals(series) && !cMacan.isBlank()) ||
                        (cPanamera.equals(series) && !cPanamera.isBlank()) ||
                        (cTaycan.equals(series) && !cTaycan.isBlank())
                    ){
                    filterData.add(i);
                }
            }
        }
        
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

            // Find matching items
            List<MenuItem> matches = new ArrayList<>();
            String searchText = newText.toLowerCase();
            
            // Search in available items
            for (inventory i : inventoryData) {
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
            
            // Search in unavailable cars
            for (inventory i : carsOffData) {
                String id = String.valueOf(i.getInventoryId().toLowerCase());
                String name = i.getName().toLowerCase();

                if (id.contains(searchText) || name.contains(searchText)) {
                    String suggestionText = i.getInventoryId() + " - " + i.getName() + " (Unavailable)";
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        searchBar.setText(i.getInventoryId() + " - " + i.getName());
                        searchSuggestions.hide();
                        searchDate.clear();
                        searchDate.add(i);
                        showTable("search");
                    });

                    matches.add(item);
                }
            }
            
            // Search in unavailable parts
            for (inventory i : partsOffDate) {
                String id = String.valueOf(i.getInventoryId().toLowerCase());
                String name = i.getName().toLowerCase();

                if (id.contains(searchText) || name.contains(searchText)) {
                    String suggestionText = i.getInventoryId() + " - " + i.getName() + " (Unavailable)";
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        searchBar.setText(i.getInventoryId() + " - " + i.getName());
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

        // Search in available items (inventoryData)
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
        
        // Search in unavailable cars
        for (inventory i : carsOffData) {
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
        
        // Search in unavailable parts
        for (inventory i : partsOffDate) {
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
            // Clear previous event handlers to prevent stacking
            editCarRevert.setOnAction(null);
            editCarApply.setOnAction(null);
            
            editCarRevert.setOnAction(e->{
                setEditCar();
            });
            editCarApply.setOnAction(e->{
                try {
                    boolean success = tableAddUpdateDelete(true);
                    if (success) {
                        // Show success alert
                        alertForm(true, "carsUpdate");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Update Failed");
                        errorAlert.setContentText("Failed to update the car. Please check all fields.");
                        errorAlert.showAndWait();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }else{
            editCar.setVisible(false);
            editPart.setVisible(true);
            editTitle.setText("(Part)");
            setEditPart();
            populateEditPartCarNames();
            // Clear previous event handlers to prevent stacking
            editPartRevert.setOnAction(null);
            editPartApply.setOnAction(null);
            
            editPartRevert.setOnAction(e->{
                setEditPart();
            });
            editPartApply.setOnAction(e->{
                try {
                    boolean success = tableAddUpdateDelete(true);
                    if (success) {
                        // Show success alert
                        alertForm(true, "partsUpdate");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Update Failed");
                        errorAlert.setContentText("Failed to update the part. Please check all fields.");
                        errorAlert.showAndWait();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        fadeInOut(true,extraPane,inventoryPane);
    }
    private void setEditCar(){
        editCarId.setText(editPath.getInventoryId());
        editCarSeries.setText(editPath.getSeries());
        editCarName.setText(editPath.getName());
        
        // Handle image loading with proper null/empty checks
        if (editPath.getPhoto() != null && !editPath.getPhoto().trim().isEmpty()) {
            file.clear();
            File imageFile = resolveImagePath(editPath.getPhoto());
            
            // Check if file exists before trying to load it
            if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
                file.add(imageFile);
                try {
                    Image img = new Image(new FileInputStream(imageFile));
                    editCarImg.setImage(img);
                } catch (FileNotFoundException e) {
                    System.err.println("Image file not found: " + editPath.getPhoto());
                    editCarImg.setImage(null);
                    file.clear();
                }
            } else {
                // File doesn't exist or path is invalid
                System.err.println("Image file does not exist: " + editPath.getPhoto());
                editCarImg.setImage(null);
                file.clear();
            }
        } else {
            // No photo path in database
            editCarImg.setImage(null);
            file.clear();
        }
        
        editCarExtColor.setText(editPath.getExtColor());
        editCarIntColor.setText(editPath.getIntColor());
        editCarQty.setText(String.valueOf(editPath.getQty()));
        editCarPrice.setText(String.valueOf(editPath.getPrice()));
        editCarProductAt.setText(String.valueOf(editPath.getProductYear()));
        
        // Convert database fuel type abbreviations to display names
        String fuelType = editPath.getFuelType().toUpperCase().trim();
        String fuel = "";
        
        switch(fuelType) {
            case "PET":
                fuel = "Petrol";
                break;
            case "HYB":
                fuel = "Hybrid (Petrol+Electric)";
                break;
            case "ELEC":
                fuel = "Electric";
                break;
            case "DSL":
                fuel = "Diesel";
                break;
            default:
                // Fallback for any other values
                fuel = editPath.getFuelType();
                break;
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
        
        // Handle image loading with proper null/empty checks
        if (editPath.getPhoto() != null && !editPath.getPhoto().trim().isEmpty()) {
            file.clear();
            File imageFile = resolveImagePath(editPath.getPhoto());
            
            // Check if file exists before trying to load it
            if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
                file.add(imageFile);
                try {
                    Image img = new Image(new FileInputStream(imageFile));
                    editPartImg.setImage(img);
                } catch (FileNotFoundException e) {
                    System.err.println("Image file not found: " + editPath.getPhoto());
                    editPartImg.setImage(null);
                    file.clear();
                }
            } else {
                // File doesn't exist or path is invalid
                System.err.println("Image file does not exist: " + editPath.getPhoto());
                editPartImg.setImage(null);
                file.clear();
            }
        } else {
            // No photo path in database
            editPartImg.setImage(null);
            file.clear();
        }
    }
    private void restoreItem() {
        try {
            // Restore - Mark as available (no validation needed)
            if (editPath.getInventoryId().contains("C")) {
                String sql = "UPDATE cars SET car_status = ? WHERE car_id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setBoolean(1, true);
                ps.setInt(2, editPath.getId());
                int rowsAffected = ps.executeUpdate();
                System.out.println("Cars restored: " + rowsAffected + " rows affected for car_id: " + editPath.getId());
                ps.close();
            } else {
                String sql = "UPDATE car_parts SET part_status = ? WHERE part_id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setBoolean(1, true);
                ps.setInt(2, editPath.getId());
                int rowsAffected = ps.executeUpdate();
                System.out.println("Parts restored: " + rowsAffected + " rows affected for part_id: " + editPath.getId());
                ps.close();
            }
            
            // Refresh table data from database
            setCarsTable();
            setPartsTable();
            inventoryData.clear();
            inventoryData.addAll(carsData);
            inventoryData.addAll(partsData);
            
            // Refresh the displayed table - preserve current view state
            if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
                showUnavailableItems();
            } else {
                if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                    showTable("cars");
                }else{
                    showTable("parts");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean tableAddUpdateDelete(boolean check){
        try {
            // Check if this is an edit operation (from edit pane) or delete
            if (editPane.isVisible() && check) {
                // EDIT/UPDATE operation
                boolean updateSuccess = false;
                if (editPath.getInventoryId().contains("C")) {
                    // Update Car
                    updateSuccess = updateCar();
                } else {
                    // Update Part
                    updateSuccess = updatePart();
                }
                
                if (!updateSuccess) {
                    return false;
                }
                
                // Refresh table data from database
                setCarsTable();
                setPartsTable();
                inventoryData.clear();
                inventoryData.addAll(carsData);
                inventoryData.addAll(partsData);
                
                // Refresh the displayed table - preserve current view state
                if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
                    showUnavailableItems();
                } else {
                    if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                        showTable("cars");
                    }else{
                        showTable("parts");
                    }
                }
                
                // Don't close edit pane here - let alertForm handle it after showing success message
                // fadeInOut and clearCarPartForm will be called from alertForm
                return true;
                
            } else if (check) {
                // Restore - Mark as available
                if (editPath.getInventoryId().contains("C")) {
                    String sql = "UPDATE cars SET car_status = ? WHERE car_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setBoolean(1, true);
                    ps.setInt(2, editPath.getId());
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Cars restored: " + rowsAffected + " rows affected for car_id: " + editPath.getId());
                    ps.close();
                } else {
                    String sql = "UPDATE car_parts SET part_status = ? WHERE part_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setBoolean(1, true);
                    ps.setInt(2, editPath.getId());
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Parts restored: " + rowsAffected + " rows affected for part_id: " + editPath.getId());
                    ps.close();
                }
                
                // Refresh table data from database
                setCarsTable();
                setPartsTable();
                inventoryData.clear();
                inventoryData.addAll(carsData);
                inventoryData.addAll(partsData);
                
                // Refresh the displayed table - preserve current view state
                if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
                    showUnavailableItems();
                } else {
                    if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                        showTable("cars");
                    }else{
                        showTable("parts");
                    }
                }
                return true;

            } else {
                // Delete/Mark as unavailable
                if (editPath.getInventoryId().contains("C")) {
                    String sql = "UPDATE cars SET car_status = ? WHERE car_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setBoolean(1, false);
                    ps.setInt(2, editPath.getId());
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Cars updated: " + rowsAffected + " rows affected for car_id: " + editPath.getId());
                    ps.close();


                } else {
                    String sql = "UPDATE car_parts SET part_status = ? WHERE part_id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setBoolean(1, false);
                    ps.setInt(2, editPath.getId());
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Parts updated: " + rowsAffected + " rows affected for part_id: " + editPath.getId());
                    ps.close();
                }
                
                // Refresh table data from database
                setCarsTable();
                setPartsTable();
                inventoryData.clear();
                inventoryData.addAll(carsData);
                inventoryData.addAll(partsData);
                
                // Close the edit pane
                fadeInOut(false, extraPane, inventoryPane);
                editPane.setVisible(false);
                clearCarPartForm(editPath.getInventoryId().contains("C") ? "carsEdit" : "partsEdit");
                
                // Refresh the displayed table
                if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                    showTable("cars");
                }else{
                    showTable("parts");
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertCar() throws SQLException {
        // Get values from add car form
        String modelName = carModelText.getText().trim();
        String trimName = carTrimText.getText().trim();
        String extColor = carExtColorText.getText().trim();
        String intColor = carIntColorText.getText().trim();
        RadioButton selectedFuel = (RadioButton) fuelTypeGroup.getSelectedToggle();
        String fuelType = (selectedFuel != null) ? selectedFuel.getText() : "";
        int year = Integer.parseInt(carYearText.getText().trim());
        int qty = Integer.parseInt(carQtyText.getText().trim());
        double price = Double.parseDouble(carPriceText.getText().trim());
        
        // Handle image
        String photoPath = null;
        if (!file.isEmpty()) {
            photoPath = file.get(0).getAbsolutePath();
        }
        
        // Call stored procedure
        String sql = "{CALL insertFullCar(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        CallableStatement cs = con.prepareCall(sql);
        cs.setString(1, modelName);
        cs.setString(2, trimName);
        cs.setString(3, extColor);
        cs.setString(4, intColor);
        cs.setString(5, fuelType);
        cs.setInt(6, year);
        cs.setInt(7, qty);
        cs.setDouble(8, price);
        cs.setString(9, photoPath);
        
        cs.execute();
        cs.close();
        
        // Refresh table data
        setCarsTable();
        setPartsTable();
        inventoryData.clear();
        inventoryData.addAll(carsData);
        inventoryData.addAll(partsData);
        
        // Refresh the displayed table - preserve current view state
        if (switchTable != null && switchTable.getText() != null && switchTable.getText().contains("Unavailable")) {
            showUnavailableItems();
        } else {
            if(inventoryBox.getValue().equalsIgnoreCase("Cars")){
                showTable("cars");
            }else{
                showTable("parts");
            }
        }
    }
    
    private void insertPart() throws SQLException {
        // TODO: Implement insert part logic
        System.out.println("Insert part not yet implemented");
    }
    
    private boolean updateCar() throws SQLException {
        // Get values from edit form with null safety checks
        if (editCarName == null || editCarName.getText() == null ||
            editCarSeries == null || editCarSeries.getText() == null ||
            editCarExtColor == null || editCarExtColor.getText() == null ||
            editCarIntColor == null || editCarIntColor.getText() == null ||
            editCarUsage == null || editCarUsage.getText() == null ||
            editCarProductAt == null || editCarProductAt.getText() == null ||
            editCarQty == null || editCarQty.getText() == null ||
            editCarPrice == null || editCarPrice.getText() == null) {
            System.err.println("Error: One or more edit car fields are null");
            return false;
        }
        
        // editCarName contains the FULL car name that user entered
        // Don't combine with editCarSeries - just use editCarName directly
        String fullCarName = editCarName.getText().trim();
        String extColor = editCarExtColor.getText().trim();
        String intColor = editCarIntColor.getText().trim();
        String fuelType = editCarUsage.getText().trim();
        
        // Validate and parse numeric fields
        if (editCarProductAt.getText().trim().isEmpty()) {
            System.err.println("Error: Production year is empty");
            return false;
        }
        if (editCarQty.getText().trim().isEmpty()) {
            System.err.println("Error: Quantity is empty");
            return false;
        }
        if (editCarPrice.getText().trim().isEmpty()) {
            System.err.println("Error: Price is empty");
            return false;
        }
        
        int year = Integer.parseInt(editCarProductAt.getText().trim());
        int qty = Integer.parseInt(editCarQty.getText().trim());
        double price = Double.parseDouble(editCarPrice.getText().trim());
        
        // The procedure will parse fullCarName: "911 Carrera T" -> model="911 Carrera", trim="T"
        
        // Handle image
        String photoPath = editPath.getPhoto(); // Keep existing photo by default
        if (!file.isEmpty()) {
            photoPath = file.get(0).getAbsolutePath();
        }
        
        // Call updateFullCar stored procedure
        // This handles: model parsing, photo overwrite, and car update
        CallableStatement cs = con.prepareCall("{CALL updateFullCar(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
        cs.setInt(1, editPath.getId());           // in_car_id
        cs.setString(2, fullCarName);              // in_car_name (will be parsed by procedure)
        cs.setString(3, extColor);                 // in_car_color
        cs.setString(4, intColor);                 // in_interior_color
        cs.setString(5, fuelType);                 // in_fuel_type (procedure handles conversion)
        cs.setInt(6, year);                        // in_production_year
        cs.setInt(7, qty);                         // in_car_qty
        cs.setDouble(8, price);                    // in_price
        cs.setString(9, photoPath);                // in_photo_url
        
        cs.execute();
        System.out.println("Car updated successfully via updateFullCar procedure for car_id: " + editPath.getId());
        cs.close();
        return true;
    }
    
    private boolean updatePart() throws SQLException {
        // Get values from edit form with null safety checks
        if (editPartName == null || editPartName.getText() == null) {
            System.err.println("Error: editPartName is null");
            return false;
        }
        if (editPartDescription == null || editPartDescription.getText() == null) {
            System.err.println("Error: editPartDescription is null");
            return false;
        }
        if (editPartForCar == null || editPartForCar.getText() == null) {
            System.err.println("Error: editPartForCar is null");
            return false;
        }
        if (editPartQty == null || editPartQty.getText() == null) {
            System.err.println("Error: editPartQty is null");
            return false;
        }
        if (editPartPrice == null || editPartPrice.getText() == null) {
            System.err.println("Error: editPartPrice is null");
            return false;
        }
        
        String partName = editPartName.getText().trim();
        String description = editPartDescription.getText().trim();
        String forCarModel = editPartForCar.getText().trim();
        int qty = Integer.parseInt(editPartQty.getText().trim());
        double price = Double.parseDouble(editPartPrice.getText().trim());
        
        // Handle image
        String photoPath = editPath.getPhoto(); // Keep existing photo by default
        if (!file.isEmpty()) {
            photoPath = file.get(0).getAbsolutePath();
        }
        
        // Call updateFullPart stored procedure
        // This handles: car lookup by model, photo update, and part update
        CallableStatement cs = con.prepareCall("{CALL updateFullPart(?, ?, ?, ?, ?, ?, ?)}");
        cs.setInt(1, editPath.getId());           // in_part_id
        cs.setString(2, partName);                 // in_part_name
        cs.setString(3, description);              // in_description
        cs.setString(4, forCarModel);              // in_for_car_model
        cs.setInt(5, qty);                         // in_part_qty
        cs.setDouble(6, price);                    // in_price
        cs.setString(7, photoPath);                // in_photo_url
        
        cs.execute();
        System.out.println("Part updated successfully via updateFullPart procedure for part_id: " + editPath.getId());
        cs.close();
        return true;
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
    
    /**
     * Resolves image path - handles absolute, relative, and network (UNC) paths
     * Supports:
     * - Network paths: \\ServerName\SharedFolder\Image\car.png
     * - Absolute paths: D:\Porsche\Image\car.png
     * - Relative paths: /Image/car.png or Image/car.png
     */
    private File resolveImagePath(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return null;
        }
        
        // Handle network (UNC) paths: \\ServerName\SharedFolder\...
        if (photoPath.startsWith("\\\\") || photoPath.startsWith("//")) {
            File networkFile = new File(photoPath);
            if (networkFile.exists()) {
                System.out.println("Found image on network path: " + photoPath);
                return networkFile;
            } else {
                System.err.println("Network path not accessible: " + photoPath);
                return networkFile; // Return anyway for error handling
            }
        }
        
        File imageFile = new File(photoPath);
        
        // If it's already an absolute path and exists, return it
        if (imageFile.isAbsolute() && imageFile.exists()) {
            System.out.println("Found image at absolute path: " + photoPath);
            return imageFile;
        }
        
        // Try to resolve as relative path from project directory
        // Get the project root directory (where src folder is located)
        String projectRoot = System.getProperty("user.dir");
        
        // Remove leading slash if present for relative path construction
        String relativePath = photoPath.startsWith("/") || photoPath.startsWith("\\") 
                              ? photoPath.substring(1) 
                              : photoPath;
        
        // First try from project root
        File relativeFile = new File(projectRoot, relativePath);
        if (relativeFile.exists()) {
            System.out.println("Found image at relative path: " + relativeFile.getAbsolutePath());
            return relativeFile;
        }
        
        // Try from src/main/resources
        File resourceFile = new File(projectRoot, "src/main/resources/" + relativePath);
        if (resourceFile.exists()) {
            System.out.println("Found image in resources: " + resourceFile.getAbsolutePath());
            return resourceFile;
        }
        
        // Try from Images folder in project root (common location)
        File imagesFolder = new File(projectRoot, "Images/" + relativePath);
        if (imagesFolder.exists()) {
            System.out.println("Found image in Images folder: " + imagesFolder.getAbsolutePath());
            return imagesFolder;
        }
        
        System.err.println("Image not found at any location: " + photoPath);
        System.err.println("Tried locations:");
        System.err.println("  1. " + photoPath + " (absolute)");
        System.err.println("  2. " + relativeFile.getAbsolutePath());
        System.err.println("  3. " + resourceFile.getAbsolutePath());
        System.err.println("  4. " + imagesFolder.getAbsolutePath());
        
        // Return original file object even if it doesn't exist (for error handling)
        return imageFile;
    }


}
