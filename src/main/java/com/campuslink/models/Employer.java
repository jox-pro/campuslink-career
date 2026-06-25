package com.campuslink.models;

public class Employer {
    private int employerId;
    private int userId;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;

    public Employer() {}

    public Employer(int employerId, int userId, String companyName, String contactPerson,
                    String email, String phone, String address) {
        this.employerId = employerId; this.userId = userId; this.companyName = companyName;
        this.contactPerson = contactPerson; this.email = email;
        this.phone = phone; this.address = address;
    }

    public int getEmployerId() { return employerId; }
    public void setEmployerId(int employerId) { this.employerId = employerId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() { return "Employer{id=" + employerId + ", company='" + companyName + "'}"; }
}
