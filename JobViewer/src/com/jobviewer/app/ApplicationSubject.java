package com.jobviewer.app;

public class ApplicationSubject extends Subject {
	public void exit() {
		notifyObservers();
	}

	public void logOutApplication() {
		logOut();
	}

	public void flush() {
		flushApp();
	}

	public String getScreenAtTop() {
		return getTopActivity();
	}

	public void goToStartUpScreenFromShoutOut() {
		goToStartUpScreen();
	}
}
