package com.itcs443.bookstore.form;

/**
 * a profile form on the client side.
 */
public class ProfileForm {
    /**
     * Any string user wants us to display him/her on this system.
     */
    private String displayName;
    private String address;

    private ProfileForm () {}


    public ProfileForm(String displayName, String address) {
        this.displayName = displayName;
        this.address = address;
    }

    public String getDisplayName() {
        return displayName;
    }
    
	public String getAddress(){
		return address;
	}
	

}
