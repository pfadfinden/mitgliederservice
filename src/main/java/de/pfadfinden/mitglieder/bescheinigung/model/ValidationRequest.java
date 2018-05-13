package de.pfadfinden.mitglieder.bescheinigung.model;

public class ValidationRequest {

    private int membershipNumber;
    private String lastName;
    private String firstName;
    private String challenge;
    private String dateOfBirth;
    private boolean reportAusweis;

    /* Special func */

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "membershipNumber=" + membershipNumber +
                ", lastName='" + lastName + "'" +
                ", firstName='" + firstName + "'" +
                ", dateOfBirth='" + dateOfBirth + "'" +
                ", captcha='" + challenge + "'" +
                '}';
    }

    /* Getter & Setter */
    public int getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(int membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isReportAusweis() {
        return reportAusweis;
    }

    public void setReportAusweis(boolean reportAusweis) {
        this.reportAusweis = reportAusweis;
    }
}
