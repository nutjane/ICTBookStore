package com.itcs443.bookstore.form;

import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

/**
 * A simple Java object (POJO) representing a Book form sent from the client.
 */
public class BookForm {

    private String name;
    private String description;
    private List<String> category;
    private String author;
    private double price;


    private BookForm() {}


    public BookForm(String name, String description, List<String> category, String author,
                          double price) {
        this.name = name;
        this.description = description;
        this.category = category == null ? null : ImmutableList.copyOf(category);
        this.author = author;
        this.price = price;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCategory() {
        return category;
    }

    public String getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }

}
