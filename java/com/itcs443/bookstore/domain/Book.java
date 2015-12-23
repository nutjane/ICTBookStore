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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Book class stores book date.
 */
@Entity
@Cache
public class Book {

    private static final String DEFAULT_AUTHOR = "No Author";

    private static final List<String> DEFAULT_CATEGORY = ImmutableList.of("Default", "Category");

    /**
     * The id for the datastore key.
     * We use automatic id assignment for entities of Book class.
     */
    @Id
    private long id;

    @Index
    private String name;
    private String description;


    /**
     * Topics related to this book.
     */
    @Index
    private List<String> category;

    /**
     * The name of the city that the book takes place.
     */
    @Index(IfNotDefault.class) 
    private String author;
    
    private Double price;


    /**
     * Just making the default constructor private.
     */
    private Book() {}

    public Book(final long id,
                      final BookForm bookForm) {
        Preconditions.checkNotNull(bookForm.getName(), "The name is required");
        this.id = id;
        updateWithBookForm(bookForm);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    // Get a String version of the key
    public String getWebsafeKey() {
        return Key.create(Book.class, id).getString();
    }

    /**
     * Returns a defensive copy of category if not null.
     * @return a defensive copy of category if not null.
     */
    public List<String> getCategory() {
        return category == null ? null : ImmutableList.copyOf(category);
    }

    public String getAuthor() {
        return author;
    }
    
    public double getPrice(){
    	return price;
    }
    
    
    /**
     * Updates the Book with BookForm.
     * This method is used upon object creation as well as updating existing Book.
     *
     * @param bookForm contains form data sent from the client.
     */
    public void updateWithBookForm(BookForm bookForm) {
        this.name = bookForm.getName();
        this.description = bookForm.getDescription();
        this.category = bookForm.getCategory();
        this.author = bookForm.getAuthor() == null ? DEFAULT_AUTHOR : bookForm.getAuthor();
        this.price = bookForm.getPrice();
    }



}
