package project.MainClass;

import java.util.Date;

public class Staff_info {
        private  int staff_id;
        private String staff_name,staff_phone,staff_email,staff_address;
        private  boolean status;
        private Date staff_dob;



    public Staff_info() {
    }

    public Staff_info(int id, String name, String phone, String email, String address, Date dob, boolean status) {
        this.staff_id = id;
        this.staff_name = name;
        this.staff_phone = phone;
        this.staff_email = email;
        this.staff_address = address;
        this.staff_dob = dob;
        this.status = status;
    }


    public Date getStaff_dob() {
        return staff_dob;
    }

    public void setStaff_dob(Date staff_dob) {
        this.staff_dob = staff_dob;
    }

    public String getStaff_address() {
        return staff_address;
    }

    public void setStaff_address(String staff_address) {
        this.staff_address = staff_address;
    }

    public String getStaff_email() {
        return staff_email;
    }

    public void setStaff_email(String staff_email) {
        this.staff_email = staff_email;
    }

    public String getStaff_phone() {
        return staff_phone;
    }

    public void setStaff_phone(String staff_phone) {
        this.staff_phone = staff_phone;
    }

    public String getStaff_name() {
        return staff_name;
    }

    public void setStaff_name(String staff_name) {
        this.staff_name = staff_name;
    }

    public int getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(int staff_id) {
        this.staff_id = staff_id;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
