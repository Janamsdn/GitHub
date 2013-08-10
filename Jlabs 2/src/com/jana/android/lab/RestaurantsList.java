package com.jana.android.lab;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.util.Key;


/** Implement this class from "Serializable"
* So that you can pass this class Object to another using Intents
* */
@SuppressWarnings("serial")
public class RestaurantsList  {

	@Key
	public String status;

	@Key
	public List<Restaurants> results;

}