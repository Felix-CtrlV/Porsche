package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    private ComboBox<?> inventoryBox;

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
    void carExtColorMouseClick(MouseEvent event) {

    }

    @FXML
    void carExtColorMouseEnter(MouseEvent event) {

    }

    @FXML
    void carHandlerDrop(DragEvent event) {

    }

    @FXML
    void carHandlerOver(DragEvent event) {

    }

    @FXML
    void carIntColorMouseClick(MouseEvent event) {

    }

    @FXML
    void carIntColorMouseEnter(MouseEvent event) {

    }

    @FXML
    void carIntColorMouseExit(MouseEvent event) {

    }

    @FXML
    void carModelMouseClick(MouseEvent event) {

    }

    @FXML
    void carModelMouseEnter(MouseEvent event) {

    }

    @FXML
    void carModelMouseExit(MouseEvent event) {

    }

    @FXML
    void carMouseExit(MouseEvent event) {

    }

    @FXML
    void carPriceMouseClick(MouseEvent event) {

    }

    @FXML
    void carPriceMouseEnter(MouseEvent event) {

    }

    @FXML
    void carPriceMouseExit(MouseEvent event) {

    }

    @FXML
    void carQtyMouseClick(MouseEvent event) {

    }

    @FXML
    void carQtyMouseEnter(MouseEvent event) {

    }

    @FXML
    void carQtyMouseExit(MouseEvent event) {

    }

    @FXML
    void carTrimMouseClick(MouseEvent event) {

    }

    @FXML
    void carTrimMouseEnter(MouseEvent event) {

    }

    @FXML
    void carTrimMouseExit(MouseEvent event) {

    }

    @FXML
    void carYearMouseClick(MouseEvent event) {

    }

    @FXML
    void carYearMouseEnter(MouseEvent event) {

    }

    @FXML
    void carYearMouseExit(MouseEvent event) {

    }

    @FXML
    void clickAddCarbtn(ActionEvent event) {

    }

    @FXML
    void clickAddPartbtn(ActionEvent event) {

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

    }

    @FXML
    void clickModelsSelectAll(ActionEvent event) {

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

    }

    @FXML
    void clickSelect911(ActionEvent event) {

    }

    @FXML
    void clickSelectCayenne(ActionEvent event) {

    }

    @FXML
    void clickSelectMacan(ActionEvent event) {

    }

    @FXML
    void clickSelectPanamera(ActionEvent event) {

    }

    @FXML
    void clickSelectTaycan(ActionEvent event) {

    }

    @FXML
    void clickSeriesSelectAll(ActionEvent event) {

    }

    @FXML
    void extraPaneMouseClick(MouseEvent event) {

    }

    @FXML
    void partDescriptionMouseClick(MouseEvent event) {

    }

    @FXML
    void partDescriptionMouseEnter(MouseEvent event) {

    }

    @FXML
    void partDescriptionMouseExit(MouseEvent event) {

    }

    @FXML
    void partHandlerDrop(DragEvent event) {

    }

    @FXML
    void partHandlerOver(DragEvent event) {

    }

    @FXML
    void partMouseClick(MouseEvent event) {

    }

    @FXML
    void partMouseExit(MouseEvent event) {

    }

    @FXML
    void partNameMouseClick(MouseEvent event) {

    }

    @FXML
    void partNameMouseEnter(MouseEvent event) {

    }

    @FXML
    void partNameMouseExit(MouseEvent event) {

    }

    @FXML
    void partQtyMouseClick(MouseEvent event) {

    }

    @FXML
    void partQtyMouseEnter(MouseEvent event) {

    }

    @FXML
    void partQtyMouseExit(MouseEvent event) {

    }

}
