package com.itcs443.bookstore.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;


@Entity
@Cache
public class Profile {
	private String displayName;
	private String mainEmail;
	private String address;
    private String onGoingCartId;



	//indicate that the userId is to be used in the Entity's key
	@Id String userId;
    
    /**
     * Public constructor for Profile.
     * @param userId The user id, obtained from the email
     * @param displayName Any string user wants us to display him/her on this system.
     * @param mainEmail User's main e-mail address.
     * 
     */
	
    public Profile (String userId, String displayName, 
    		String mainEmail, String address) {
    	this.userId = userId;
    	this.displayName = displayName;
    	this.mainEmail = mainEmail;
    	this.address = address;
    	this.onGoingCartId = null;

    }
    
	public String getDisplayName() {
		return displayName;
	}

	public String getMainEmail() {
		return mainEmail;
	}
	
	public String getAddress(){
		return address;
	}

	public String getUserId() {
		return userId;
	}
	
	public String getOnGoingCartId(){
		return onGoingCartId;
	}
	
	public void clearOnGoingCartId(){
		this.onGoingCartId = "";
	}
	
	public void update(String onGoingCartId){
		if(onGoingCartId !=null){
			this.onGoingCartId = onGoingCartId;
		}
	}
	
	public void update(String displayName, String address, String onGoingCartId){
		if(displayName !=null){
			this.displayName = displayName;
		}
		if(address != null){
			this.address = address;
		}

		if(onGoingCartId != null){
			this.onGoingCartId = onGoingCartId;
		}
	}
	
	public boolean haveOngoingOrder(){
		if(onGoingCartId == null || onGoingCartId.equals("")) return false;
		else return true;
	}

	/**
     * Just making the default constructor private.
     */
    private Profile() {}
    
	
	

}
