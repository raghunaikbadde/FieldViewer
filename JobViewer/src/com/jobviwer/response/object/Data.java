package com.jobviwer.response.object;

public class Data {
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "ClassPojo [user = " + user + "]";
	}
}
