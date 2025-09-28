package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Model.managerOverview;

import java.time.YearMonth;

public class bestSellerController {

    @FXML
    private Label hourRate;

    @FXML
    private ImageView image;

    @FXML
    private Label name;

    @FXML
    private Label rank;

    @FXML
    private Label revenueRate;

    @FXML
    private Label soldRevenue;

    @FXML
    private Label workHour;

    public void setData(managerOverview overview, int month, int year) {
        name.setText(overview.getStaffName());
        rank.setText(overview.getRank());

        int pwh = overview.getPrevWorkHour();
        int wh = overview.getWorkHour();

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int hours = 24 * daysInMonth;

        double hrate = ((double) pwh / hours * 100) - ((double) wh / hours * 100);

        workHour.setText(wh + " Hours");
        hourRate.setText(String.format("%.2f%% of this month", hrate));

        double sale = overview.getTotalSale();
        double psale = overview.getPrevTotalSale();

        soldRevenue.setText(String.format("%.2f", sale));

        double revenueRateValue = 0;
        if (psale != 0) {
            revenueRateValue = ((sale - psale) / psale) * 100;
        }
        revenueRate.setText(String.format("%.2f%% from last month", revenueRateValue));


        String photoUrl = overview.getStaffPhoto();

        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                Image staffImage = new Image(photoUrl, true);
                image.setImage(staffImage);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());

                image.setImage(new Image(getClass().getResource("/Image/usericon.png").toExternalForm()));
            }
        } else {

            image.setImage(new Image(getClass().getResource("/Image/usericon.png").toExternalForm()));
        }

    }
}
