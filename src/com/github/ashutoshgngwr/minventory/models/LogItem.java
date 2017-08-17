package com.github.ashutoshgngwr.minventory.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogItem {

	private String name, time, toFrom, soldBought, dbTime;
	private int quantity;

	public LogItem(String name, String time, String toFrom, int quantity, boolean deleted) {
		this.name = name + (deleted ? " (Deleted Product)" : "");
		this.toFrom = toFrom;
		this.quantity = Math.abs(quantity);
		this.soldBought = quantity < 0 ? "sold" : "bought";
		this.dbTime = time;

		try {
			Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(time);
			this.time = new SimpleDateFormat("dd LLL, yyyy HH:mm").format(d);
		} catch (ParseException ignore) {

		}
	}

	public String getToFrom() {
		return toFrom;
	}

	public void setToFrom(String toFrom) {
		this.toFrom = toFrom;
	}

	public String getTime() {
		return time;
	}

	public String getDBTimestamp() {
		return dbTime;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getSoldBought() {
		return soldBought;
	}

	public void setSoldBought(String soldBought) {
		this.soldBought = soldBought;
	}
}
