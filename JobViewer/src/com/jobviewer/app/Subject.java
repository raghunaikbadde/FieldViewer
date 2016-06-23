package com.jobviewer.app;

import java.util.ArrayList;
import java.util.List;

public class Subject {
	private List<Observer> observers = new ArrayList<Observer>();

	public void attach(Observer observer) {
		observers.add(observer);
	}

	public void detach(Observer observer) {
		observers.remove(observer);
	}

	protected void notifyObservers() {
		do {
			if (observers.size() != 0) {
				Observer observer = observers.get(0);
				observer.update(this);
			}
		} while (observers.size() != 0);
	}

	protected void flushApp() {
		int activitySize = observers.size();
		for (int i = activitySize - 1; i > 0; i--) {
			observers.get(i).update(this);
		}
	}

	protected void logOut() {
		int activitySize = observers.size();
		for (int i = activitySize - 1; i > 0; i--) {
			observers.get(i).update(this);
		}
	}

	protected void goToStartUpScreen() {
		int activitySize = observers.size();
		for (int i = activitySize - 1; i > 0; i--) {
			if (observers.get(i).toString().contains("ShoutOptionsActivity")
					|| observers.get(i).toString().contains("ShoutOutActivity")) {
				observers.get(i).update(this);
			}
		}
	}

	protected String getTopActivity() {
		if (observers != null && observers.size() != 0) {
			int activitySize = observers.size();
			return observers.get(activitySize - 1).getClass().toString();
		}
		return "";
	}

	public interface Observer {
		void update(Subject subject);
	}
}
