package com.itcs443.bookstore.form;

import static com.itcs443.bookstore.service.OfyService.ofy;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.cmd.Query;
import com.itcs443.bookstore.domain.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A simple Java object (POJO) representing a query options for Book.
 */
public class BookQueryForm {

    private static final Logger LOG = Logger.getLogger(BookQueryForm.class.getName());

    /**
     * Enum representing a field type.
     */
    public static enum FieldType {
        STRING, INTEGER
    }

    /**
     * Enum representing a field.
     */
    public static enum Field {
        NAME("name", FieldType.STRING),
        AUTHOR("author", FieldType.STRING);

        private String fieldName;

        private FieldType fieldType;

        private Field(String fieldName, FieldType fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        private String getFieldName() {
            return this.fieldName;
        }
    }


    /**
     * A class representing a single filter for the query.
     */
    public static class Filter {
        private Field field;
        private String value;

        public Filter () {}

        public Filter(Field field, String value) {
            this.field = field;
            this.value = value;
        }

        public Field getField() {
            return field;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * A list of query filters.
     */
    private List<Filter> filters = new ArrayList<>(0);

    /**
     * Holds the first inequalityFilter for checking the feasibility of the whole query.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Filter inequalityFilter;

    public BookQueryForm() {}

    /**
     * Checks the feasibility of the whole query.
     */
    private void checkFilters() {
    	if(this.filters.size() > 1){
    		throw new IllegalArgumentException(
                    "Now allow only one filter at a time");
    	}

    }

    /**
     * Getter for filters.
     *
     * @return The List of filters.
     */
    public List<Filter> getFilters() {
        return ImmutableList.copyOf(filters);
    }

    /**
     * Adds a query filter.
     *
     * @param filter A Filter object for the query.
     * @return this for method chaining.
     */
    public BookQueryForm filter(Filter filter) {

        filters.add(filter);
        return this;
    }

    /**
     * Returns an Objectify Query object for the specified filters.
     *
     * @return an Objectify Query.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Query<Book> getQuery() {
        // First check the feasibility of inequality filters.
        checkFilters();
        Query<Book> query = ofy().load().type(Book.class);
        
        for (Filter filter : this.filters) {
            // Applies filters in order.
            if (filter.field.fieldType == FieldType.STRING) {
                query = query.filter(filter.field.getFieldName() + " >=", filter.value);
                query = query.filter(filter.field.getFieldName()  + " <", filter.value + "\ufffd");

                
            } 
        }
        LOG.info(query.toString());
        return query;
    }
}
