package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class managerOverviewController {

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private Button arrowLeftbtn;

    @FXML
    private Button arrowRightbtn;

    @FXML
    private VBox attendanceBox;

    @FXML
    private PieChart attendancePieChart;

    @FXML
    private TableView<?> attendanceTable;

    @FXML
    private Circle carCircle;

    @FXML
    private Button carbtn;

    @FXML
    private StackPane carouselStackPane;

    @FXML
    private ChoiceBox<?> monthBox;

    @FXML
    private Circle partCircle;

    @FXML
    private Button partbtn;

    @FXML
    private VBox scrollPaneCarPart;

    @FXML
    private VBox scrollPaneStaff;

    @FXML
    private TableColumn<?, ?> signInTimeCol;

    @FXML
    private TableColumn<?, ?> statusCol;

    @FXML
    private VBox targetBox;

    @FXML
    private Text targetCar;

    @FXML
    private Label targetCarMessagelbl;

    @FXML
    private Label targetOverCar;

    @FXML
    private Label targetOverPart;

    @FXML
    private Text targetPart;

    @FXML
    private Label targetPartMessagelbl;

    @FXML
    private HBox targetlayer;

    @FXML
    private TableColumn<?, ?> workerCol;

    @FXML
    private ChoiceBox<?> yearBox;

    @FXML
    void clickArrowLeftbtn(ActionEvent event) {

    }

    @FXML
    void clickArrowRightbtn(ActionEvent event) {

    }

    @FXML
    void clickCarbtn(ActionEvent event) {

    }

    @FXML
    void clickPartbtn(ActionEvent event) {

    }

}
