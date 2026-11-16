package id.edu.homeschool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
public final class Pengajar extends Pengguna {
    private String expertise;
    private String qualifications = "";

    public Pengajar() { super(); }
    public Pengajar(String name, String email, String phone, String expertise, String qualifications) {
        super(name, email, phone); setExpertise(expertise); setQualifications(qualifications);
    }
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = required(expertise, "Bidang keahlian"); }
    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications == null ? "" : qualifications.trim(); }
    @JsonIgnore
    @Override public String getRoleLabel() { return "Pengajar"; }
}
