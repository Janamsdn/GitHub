package com.jana.android.lab;

import java.io.Serializable;

import com.google.api.client.util.Key;

/** Implement this class from "Serializable"
* So that you can pass this class Object to another using Intents
* */
@SuppressWarnings("serial")
public class RestaurantsDetails implements Serializable {

	@Key
	public String status;
	
	@Key
	public Restaurants result;

	@Override
	public String toString() {
		if (result!=null) {
			return result.toString();
		}
		return super.toString();
	}
}
