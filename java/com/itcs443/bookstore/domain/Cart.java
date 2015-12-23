package com.itcs443.bookstore.domain;

import static com.itcs443.bookstore.service.OfyService.ofy;

import com.googlecode.objectify.condition.IfNotDefault;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.itcs443.bookstore.form.BookForm;
import com.itcs443.bookstore.form.CheckoutForm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Cart class stores cart data.
 */
@Entity
public class Cart {

    private static final boolean DEFAULT_UNCHECKOUT = false;

    /**
     * The id for the datastore key.
     * We use automatic id assignment for entities of Cart class.
     */
    @Id
    private long id;
    
    @Index
    private boolean checkedOut;

    /**
     * Holds Profile key as the parent.
     */
    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey;


    /**
     * The userId of the customer.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String customerUserId;

    private Double total;
    private Date date;
    private String address;
    
    /**
     * payment details
     */
    private String cardNumber;
    private String cardName;
    private Integer cardCcv;
    private String cardExpiryDate;
    
    /**
     * BookID of this cart
     */
    private List<String> bookKeys = new ArrayList<>(0);
    
    
    /**
     * Constructor
     */
    private Cart() {}

    public Cart(final long id, final String userId) {
        this.id = id;
        this.profileKey = Key.create(Profile.class, userId);
        this.customerUserId = userId;
        this.checkedOut = DEFAULT_UNCHECKOUT;
        this.total = 0.0;
    }

    public long getId() {
        return id;
    }
    
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Profile> getProfileKey() {
        return profileKey;
    }

    // Get a String version of the key
    public String getWebsafeKey() {
        return Key.create(profileKey, Cart.class, id).getString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getCustomerUserID() {
        return customerUserId;
    }

    public boolean isCheckedOut(){
    	return this.checkedOut;
    }
    
    public void setToCheckOut(boolean checked){
    	if(checked) this.checkedOut = true;
    	else this.checkedOut = false;
    }
    
    /**
     * Returns customer display name.
     *
     * @return customer display name. If there is no Profile, return his/her userId.
     */
    public String getCustomerDisplayName() {
        Profile customer = ofy().load().key(getProfileKey()).now();
        if (customer == null) {
            return customerUserId;
        } else {
            return customer.getDisplayName();
        }
    }

    

    public String getCardNumber() {
        return cardNumber;
    }
    
    public String getCardExpiryDate() {
        return cardExpiryDate;
    }
    
    public String getCardName() {
        return cardName;
    }
    
    public Integer getCardCcv() {
        return cardCcv;
    }
    
    public String getAddress(){
    	return address;
    }
    
    public double getTotal(){
    	return total;
    }
    
    public void addPrice(double price){
    	this.total += price;
    }
    
    public void deductPrice(double price){
    	this.total -= price;
    }

	public List<String> getBookKeys() {
        return ImmutableList.copyOf(bookKeys);
    }
	    
	public void addToBookKeys(String bookKey) {
		bookKeys.add(bookKey);
	}
	
	public void removeBookFromCart(String bookKey) {
		bookKeys.remove(bookKey);
	}

	public Date getDate() {
		return date;
	}

    /**
     * Updates the cart with CheckoutForm.
     *
     * @param CheckoutForm contains form data sent from the client.
     */
    public void updateWithCheckoutForm(CheckoutForm checkoutForm) {
    	this.address = checkoutForm.getAddress();
    	this.cardCcv = checkoutForm.getCardCcv();
    	this.cardExpiryDate = checkoutForm.getCardExpiryDate();
    	this.cardName = checkoutForm.getCardName();
    	this.cardNumber = checkoutForm.getCardNumber();
    	this.date = new Date();
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Id: " + id + "\n")
                .append("Total: ").append(total).append("\n");
        stringBuilder.append("Date: ").append(date).append("\n");;
        stringBuilder.append("Ship To :").append(address).append("\n");;

        return stringBuilder.toString();
    }


}
