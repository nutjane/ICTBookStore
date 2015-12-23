package com.itcs443.bookstore.form;

import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

/**
 * A simple Java object (POJO) representing a checkout data form sent from the client.
 */
public class CheckoutForm {

    private String address;
    private String cardName;
    private String cardNumber;
    private String cardExpiryDate;
    private Integer cardCcv;

    private CheckoutForm() {}


    public CheckoutForm(String name, String address, String cardName, 
    		String cardNumber, String cardExpiryDate, int cardCcv) {
        this.address = address;
        this.cardName = cardName;
        this.cardCcv = cardCcv;
        this.cardExpiryDate = cardExpiryDate;
        this.cardNumber = cardNumber;
        
    }

    public String getAddress() {
        return address;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpiryDate() {
        return cardExpiryDate;
    }
    
    public int getCardCcv(){
    	return cardCcv;
    }

}
