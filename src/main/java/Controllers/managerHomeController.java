package Controllers;

import Database.Porsche_DB;
import Model.ManagerOfAttendanceView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class managerHomeController {

    @FXML
    private Label attendance_date_label;

    @FXML
    private PieChart attendancePieChart;

    @FXML
    private TableView<ManagerOfAttendanceView> attendance_table;

    @FXML
    private BarChart<Integer, String> car_barchart;

    @FXML
    private Button monthNext;

    @FXML
    private Button monthPrev;

    @FXML
    private Label monthlbl;

    @FXML
    private BarChart<Integer, String> part_barchart;

    @FXML
    private Label percentagelbl;

    @FXML
    private TableColumn<ManagerOfAttendanceView, String > user_name_col;

    @FXML
    private TableColumn<ManagerOfAttendanceView, Time> user_signin_time_col;

    @FXML
    private Button yearNext;

    @FXML
    private Button yearPrev;

    @FXML
    private Label yearlbl;

    private LocalDate today = LocalDate.now();
    private DateTimeFormatter fmonth = DateTimeFormatter.ofPattern("MMM");

    private DateTimeFormatter fyear = DateTimeFormatter.ofPattern("yyyy");

    private int currentMonth = today.getMonthValue();
    private int currentYear = today.getYear();

    public managerHomeController() throws SQLException, ClassNotFoundException {
    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();

    @FXML
    void clickMonthNext(ActionEvent event) throws SQLException, ClassNotFoundException {
        currentMonth++;
        if(currentMonth>12){ currentMonth=1 ; currentYear++;};
        updateYearMonthLabel();
        BestSellingCars(currentMonth,currentYear);
        BestSellingParts(currentMonth,currentYear);
    }

    @FXML
    void clickMonthPrev(ActionEvent event) throws SQLException, ClassNotFoundException {
        currentMonth--;
        if (currentMonth<1){currentMonth =12 ; currentYear--;};
       updateYearMonthLabel();
        BestSellingCars(currentMonth,currentYear);
        BestSellingParts(currentMonth,currentYear);
    }

    @FXML
    void clickYearNext(ActionEvent event) throws SQLException, ClassNotFoundException {
       currentYear++;
       if(today.getYear() == currentYear){
           if (currentMonth> today.getMonthValue()){
               currentMonth = today.getMonthValue();
           }
       }
       updateYearMonthLabel();
        BestSellingCars(currentMonth,currentYear);
        BestSellingParts(currentMonth,currentYear);
    }

    @FXML
    void clickYearPrev(ActionEvent event) throws SQLException, ClassNotFoundException {
        currentYear--;
        updateYearMonthLabel();
        BestSellingCars(currentMonth,currentYear);
        BestSellingParts(currentMonth,currentYear);
    }


    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {




        monthlbl.setText(today.format(fmonth));
        yearNext.setDisable(true);
        yearlbl.setText(today.format(fyear));
        monthNext.setDisable(true);
        DateTimeFormatter fdate = DateTimeFormatter.ofPattern("yyy-MM-dd");
        attendance_date_label.setText(today.format(fdate));





        user_name_col.setCellValueFactory(e->
                new SimpleStringProperty(e.getValue().getWorkers()));
        user_signin_time_col.setCellValueFactory(e->
                new ReadOnlyObjectWrapper<Time>(e.getValue().getSign_in_time()));

        attendance_table.setItems(AttendanceTableView());
//        attendance_table.setStyle("-fx-border-color: lightgray; -fx-border-width: 0.5px;");
        BestSellingCars(currentMonth,currentYear);
        BestSellingParts(currentMonth,currentYear);
        attendance_piechart();
    }


    private ObservableList<ManagerOfAttendanceView> AttendanceTableView() throws SQLException, ClassNotFoundException {


        ObservableList<ManagerOfAttendanceView> temporylist = FXCollections.observableArrayList();

        CallableStatement cs =con.prepareCall("CALL managerattendanceview()");
        ResultSet rs = cs.executeQuery();

        while(rs.next()) {
            String name = rs.getString(1);
            Time in_time = rs.getTime(2);
            ManagerOfAttendanceView managerOfAttendanceView = new ManagerOfAttendanceView(name, in_time);

            temporylist.add(managerOfAttendanceView);
        }

        rs.close();
        return temporylist;
    }

    private void BestSellingCars(int month, int year) throws SQLException, ClassNotFoundException {
        XYChart.Series<Integer, String> cseries = new XYChart.Series<>();
        List<XYChart.Data<Integer,String>> datalist = new ArrayList<>();

        CallableStatement cstmt = con.prepareCall("call bestsellingcars(?,?)");
        cstmt.setInt(1,month);
        cstmt.setInt(2,year);
        ResultSet rs = cstmt.executeQuery();



        while (rs.next()) {
            String cname = rs.getString(1);
            int cqty = rs.getInt(2);
            datalist.add(new XYChart.Data<>(cqty,cname));

        }
        rs.close();
        cstmt.close();

        Collections.reverse(datalist);
//        datalist.sort((d1,d2) ->Integer.compare(d2.getXValue(),d1.getXValue()));
        for (XYChart.Data<Integer, String> data : datalist) {
            cseries.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Label label = new Label(data.getYValue());
                    label.setStyle("-fx-font-family:Aerial; -fx-font-weight:700; -fx-text-fill:Black;");
                    StackPane stack = (StackPane) newNode;
                    stack.getChildren().add(label);
                }
            });
        };

        car_barchart.getData().clear();
        car_barchart.getData().add(cseries);
    }

    private void BestSellingParts(int month,int year) throws SQLException, ClassNotFoundException {
        XYChart.Series<Integer, String> pseries = new XYChart.Series<>();
        List<XYChart.Data<Integer,String>> datalist = new ArrayList<>();


        CallableStatement cstmt = con.prepareCall("call bestsellingparts(?,?)");
        cstmt.setInt(1,month);
        cstmt.setInt(2,year);
        ResultSet rs = cstmt.executeQuery();

        while (rs.next()) {
            String pname = rs.getString(1);
            int pqty = rs.getInt(2);
            datalist.add(new XYChart.Data<>(pqty,pname));

        }

        rs.close();
        cstmt.close();

        Collections.reverse(datalist);
//        datalist.sort((d1,d2) ->Integer.compare(d2.getXValue(),d1.getXValue()));

        for (XYChart.Data<Integer, String> data : datalist) {
            pseries.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Label label = new Label(data.getYValue());
                    label.setStyle("-fx-font-family:Aerial; -fx-font-weight:700; -fx-text-fill:Black;");
                    StackPane stack = (StackPane) newNode;
                    stack.getChildren().add(label);
                }
            });
        };

        part_barchart.getData().clear();
        part_barchart.getData().add(pseries);
    }

    private  void attendance_piechart() throws SQLException, ClassNotFoundException {
        ObservableList<PieChart.Data> piechartdata = FXCollections.observableArrayList();
        String rating ="0 %";

        CallableStatement cs = con.prepareCall("call attendancepercentage_managerview();");
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            Integer attendedUsers = rs.getInt(1);
            Integer workingUsers = rs.getInt(2);
            rating = rs.getString(3);
            piechartdata.add(new PieChart.Data("Attendance Workers",attendedUsers));
            piechartdata.add(new PieChart.Data("Total Workers",workingUsers));
        }
        rs.close();
        cs.close();

        attendancePieChart.setData(piechartdata);
        percentagelbl.setText(rating);
    }

    private void updateYearMonthLabel(){
        Year nyear = Year.of(currentYear);
        int curyear = Integer.parseInt(today.format(fyear));

        yearlbl.setText(nyear.format(fyear));



        Month nmonth = Month.of(currentMonth);
        String formattedMonth = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Sep"

        int curmonth = today.getMonthValue();
        monthlbl.setText(formattedMonth);

        if(currentYear>= curyear){
            yearNext.setDisable(true);
            if(currentMonth >= curmonth){
                monthNext.setDisable(true);
            }else{
                monthNext.setDisable(false);
            }
        }else{
            yearNext.setDisable(false);
            monthNext.setDisable(false);
        }
    }

}
