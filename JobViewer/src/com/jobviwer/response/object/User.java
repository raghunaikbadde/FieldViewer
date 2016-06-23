package com.jobviwer.response.object;

public class User {
	private String email = "";

	private String userid = "";

	private String lastname = "";

	private String firstname = "";

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@Override
	public String toString() {
		return "ClassPojo [email = " + email + ", userid = " + userid
				+ ", lastname = " + lastname + ", firstname = " + firstname
				+ "]";
	}
}
