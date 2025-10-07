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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        hasData(false,"partsBtn");
    }

    @FXML
    void clickAddPartbtn(ActionEvent event) throws SQLException {
        hasData(false,"carsBtn");
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
            setActionColumn();

            showTable("cars");
            setupSearchBar();
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
    //in the path data there is cars (extra pane) , parts (extra pane), carsBtn , parstBtn , addCars, addParts
    private void hasData(boolean check, String path) throws SQLException {
        if (path.contains("car")) {
            String img = file.isEmpty() ? "" : String.valueOf(file.get(0));
            String models = carModelText.getText().trim();
            String trim = carTrimText.getText().trim();
            String extColor = carExtColorText.getText().trim();
            String intColor = carIntColorText.getText().trim();
            RadioButton selectedFuel = (RadioButton) fuelTypeGroup.getSelectedToggle();
            String fuel_type = (selectedFuel != null) ? selectedFuel.getText() : "";
            String qty = carQtyText.getText().trim();
            String price = carPriceText.getText().trim();

            boolean allEmpty = img.isEmpty() && models.isEmpty() && trim.isEmpty() &&
                    extColor.isEmpty() && intColor.isEmpty() &&
                    fuel_type.isEmpty() && qty.isEmpty() && price.isEmpty();


            try {
                if (check) {

                        alertForm(true, path);
                } else {

                    if (allEmpty && (path.equals("cars") || path.equals("carsAdd")) ) {
                        clearCarPartForm(path);
                    } else if (allEmpty && path.equals("carsBtn")) {
                            clearCarPartForm(path);
                    }else {
                        alertForm(check, path);
                    }
                }
            }catch ( IOException e){
                throw new RuntimeException(e);
            }

        } else if (path.contains("part")) {
            String img = file.isEmpty() ? "" : String.valueOf(file.get(0));
            String name = partNameText.getText().trim();
            String qty = partQtyText.getText().trim();
            String price = partPriceText.getText().trim();
            String description = partDescriptionText.getText().trim();

            boolean allEmpty = img.isEmpty() && name.isEmpty() && qty.isEmpty() &&
                    price.isEmpty() && description.isEmpty();


            try {
                if (check) {
                    alertForm(true, path);

                } else {
                    if (allEmpty && (path.equals("parts") || path.equals("partsAdd") )) {
                        clearCarPartForm(path);

                    } else if (allEmpty && path.equals("partsBtn")) {
                        clearCarPartForm(path);

                    }else {
                        alertForm(check, path);
                    }
                }
            } catch (IOException e) {
                        throw new RuntimeException(e);
            }
        }
    }
    //for alernt there have "carsAdd" "partsAdd" "carsEdit" "partsEdit" "partsUpdate" "carsUpdate" "partsDelete" "carsDelete"
    // and if true that will  show the information and if false that will show warning
    private void alertForm(boolean check,String in) throws IOException {

        if(check){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Finished");
            if(in.equalsIgnoreCase("carsAdd")){
                alert.setContentText("Successfully Added The Car");
            }else if(in.equalsIgnoreCase("partsAdd")){
                alert.setContentText("Successfully Added The Part");
            }else if(in.equalsIgnoreCase("carsEdit")){
                alert.setContentText("Successfully Edited The Car");
            }else if(in.equalsIgnoreCase("partsEdit")){
                alert.setContentText("Successfully Edited The Part");
            }else if(in.equalsIgnoreCase("partsUpdate")){
                alert.setContentText("Successfully Updated The Part");
            }else if(in.equalsIgnoreCase("carsUpadate")){
                alert.setContentText("Successfully Updated The Car");
            }
            alert.showAndWait();
            clearCarPartForm(in);
        }else{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/alert.fxml"));
            Parent root = loader.load();

            alertController alertController = loader.getController();

            if(in.equalsIgnoreCase("carsAdd")){
                alertController.setAlert("You have unsaved changes for adding a car. Are you sure you want to cancel?");
            }else if(in.equalsIgnoreCase("partsAdd")){
                alertController.setAlert("You have unsaved changes for adding a part. Are you sure you want to cancel?");
            }else if(in.equalsIgnoreCase("cartsEdit")){
                alertController.setAlert("You have unsaved changes for editing a car. Are you sure you want to cancel?");
            }else if(in.equalsIgnoreCase("partsEdit")){
                alertController.setAlert("You have unsaved changes for editing a part. Are you sure you want to cancel?");
            }else if(in.equalsIgnoreCase("partsDelete")){
                alertController.setAlert("Do you really want to Delete this part ?");
            }else if(in.equalsIgnoreCase("carsDelete")){
                alertController.setAlert("Do you really want to Delete this car ?");
            }
            Stage alertStage = new Stage();
            alertStage.initModality(Modality.APPLICATION_MODAL);
            alertStage.initStyle(StageStyle.TRANSPARENT);
            alertStage.setScene(new Scene(root));

            if (in.equals("carsBtn") || in.equals("partsBtn")){
                System.out.println(in);
                alertController.confirmBtn.setOnAction(e -> {
                    clearCarPartForm(in);
                    alertStage.close();
                });
                alertController.cancelBtn.setOnAction(e -> {
                    alertStage.close();
                });
            }else {
                System.out.println(in);
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
            }
            if(addPartbtn.isDisable()) {
                partNameText.clear();
                partDescriptionText.clear();
                partPriceText.clear();
                partQtyText.clear();
                partImage.setImage(null);
                partRelativeComboBox.getSelectionModel().clearSelection();

            }
            if(in.equals("carsBtn") ) {
                addCarbtn.setDisable(false);
                addPartbtn.setDisable(true);
                fadeInOut(true,addPart,addCar);
            }else if(in.equals("partsBtn")){
                addCarbtn.setDisable(true);
                addPartbtn.setDisable(false);
                fadeInOut(true,addCar,addPart);
            }else{
                fadeInOut(false, extraPane, inventoryPane);
            }
    }
    private ObservableList<inventory> inventoryData = FXCollections.observableArrayList();
    private ObservableList<inventory> carsData = FXCollections.observableArrayList();
    private HashMap<CheckBox,String > models = new HashMap<>();
    private ObservableList<inventory> partsData = FXCollections.observableArrayList();
    private ObservableList<inventory> searchDate = FXCollections.observableArrayList();
    private void setActionColumn() {
        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionCol.setCellFactory(param -> new TableCell<inventory, inventory>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttonsContainer = new HBox(8, editButton, deleteButton);
            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.EDIT));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
                editButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setStyle("-fx-background-color: transparent;");
                editButton.setCursor(Cursor.HAND);
                deleteButton.setCursor(Cursor.HAND);
                addHoverAnimation(editButton);
                addHoverAnimation(deleteButton);
                // Set click actions
                editButton.setOnAction(event -> {
                    inventory item = getTableView().getItems().get(getIndex());
                    editTable(item);
                });

                deleteButton.setOnAction(event -> {
                    inventory item = getTableView().getItems().get(getIndex());
                    tableUpdateDelete(false, item);
                });

                buttonsContainer.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(inventory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
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
    private void setCarsTable(){
        try{
            carsData.clear();
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
                String status = (qty !=0) ?"On" : "Out";
                String inventoryId = String.format("C-%03d", id);
                carsData.add(new inventory(inventoryId,name,extColor,intColor,fuels,productYear,qty,price,status,photoUrl));
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

                String status = (qty !=0) ?"On" : "Out";
                String inventoryId = String.format("P-%03d", id);
                partsData.add(new inventory(inventoryId,name,forCar,description,qty,price,status,photoUrl));
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

    private void editTable(inventory in){
        addPane.setVisible(false);
        editPane.setVisible(true);
        if (in.getInventoryId().contains("C")){
            editCar.setVisible(true);
            editPart.setVisible(false);
            editTitle.setText("(Car)");
            setEditCar(in);
            editCarRevert.setOnAction(e->{
                setEditCar(in);
            });
            editCarApply.setOnAction(e->{
                tableUpdateDelete(true,in);
            });
        }else{
            editCar.setVisible(false);
            editPart.setVisible(true);
            editTitle.setText("(Part)");
            setEditPart(in);
            editPartRevert.setOnAction(e->{
                setEditPart(in);
            });
            editPartApply.setOnAction(e->{
                tableUpdateDelete(true,in);
            });
        }
        fadeInOut(true,extraPane,inventoryPane);
    }
    private void setEditCar(inventory in){
        editCarId.setText(in.getInventoryId());
        editCarSeries.setText(in.getSeries());
        editCarName.setText(in.getName());
        if (in.getPhoto() != null && !in.getPhoto().trim().isEmpty()) {
            file.clear();
            file.add(new File(in.getPhoto()));
            if (!file.isEmpty()) {

                try {
                    Image img = new Image(new FileInputStream(file.get(0)));
                    editCarImg.setImage(img);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        editCarExtColor.setText(in.getExtColor());
        editCarIntColor.setText(in.getIntColor());
        editCarQty.setText(String.valueOf(in.getQty()));
        editCarPrice.setText(String.valueOf(in.getPrice()));
        editCarProductAt.setText(String.valueOf(in.getProductYear()));
        String fuel = "";
        if(in.getFuelType().contains("gas")){
            fuel += "Gasoline/";
        }else if(in.getFuelType().contains("hy")){
            fuel += "Hybrid/";
        }else if(in.getFuelType().contains("di")){
            fuel += "diseal/";
        }else {
            fuel +=in.getFuelType();
        }
        if(in.getFuelType().contains("ele")){
            fuel += "electric";
        }
        editCarUsage.setText(fuel);
    }
    private void setEditPart(inventory in){
        editPartId.setText(in.getInventoryId());
        editPartName.setText(in.getName());
        editPartDescription.setText(in.getDescription());
        editPartForCar.setText(in.getForCar());
        editPartQty.setText(String.valueOf(in.getQty()));
        editPartPrice.setText(String.valueOf(in.getPrice()));
        if (in.getPhoto() != null && !in.getPhoto().trim().isEmpty()) {
            file.clear();
            file.add(new File(in.getPhoto()));
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
    private void tableUpdateDelete(boolean check,inventory in){
        if (check){
            if(in.getInventoryId().contains("C")){

            }else{

            }
        }else{

            if(in.getInventoryId().contains("C")){

            }else{

            }
        }
    }

    private void setModels(ArrayList<CheckBox> in) {
        modelsShowBox.getChildren().clear();
        modelsCol1.setHgrow(Priority.ALWAYS);
        modelsCol2.setHgrow(Priority.ALWAYS);

        for(int i = 0; i < in.size(); i++){
            int row = i / 2;
            int col = i % 2;
            modelsShowBox.add(in.get(i), col, row);  // Correct order: column, row
        }
        modelsShowBox.setVisible(true);
    }

}
