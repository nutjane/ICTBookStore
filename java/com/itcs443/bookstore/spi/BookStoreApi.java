package com.itcs443.bookstore.spi;

import static com.itcs443.bookstore.service.OfyService.factory;
import static com.itcs443.bookstore.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import com.itcs443.bookstore.Constants;
import com.itcs443.bookstore.domain.Book;
import com.itcs443.bookstore.domain.Cart;
import com.itcs443.bookstore.domain.Profile;
import com.itcs443.bookstore.form.BookForm;
import com.itcs443.bookstore.form.CheckoutForm;
import com.itcs443.bookstore.form.BookQueryForm;
import com.itcs443.bookstore.form.ProfileForm;


/**
 * Defines bookstore APIs.
 */
@Api(name = "bookstore", 
	version = "v1", 
	scopes = { Constants.EMAIL_SCOPE }, 
	clientIds = {
        Constants.WEB_CLIENT_ID, 
        Constants.API_EXPLORER_CLIENT_ID }, 
        description = "API for the BookStore  Backend application.")
public class BookStoreApi {

    /*
     * Get the display name from the user's email. For example, if the email is
     * nutjane@example.com, then the display name becomes "nutjane."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }
    
    /**
     * Gets the Profile entity for the current user
     * or creates it if it doesn't exist
     * @param user
     * @return user's Profile
     */
    private static Profile getProfileFromUser(User user) {
        // First fetch the user's Profile from the datastore.
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        ofy().clear();
        if (profile == null) {
            // Create a new Profile if it doesn't exist.
            // Use default displayName and teeShirtSize
            String email = user.getEmail();
            profile = new Profile(user.getUserId(),
                    extractDefaultDisplayNameFromEmail(email), email, "");
        }
        return profile;
    }


    
    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", 
    		path = "profile", 
    		httpMethod = HttpMethod.POST)
    public Profile saveProfile(final User user, ProfileForm profileForm) throws UnauthorizedException {

        if( user == null){
        	throw new UnauthorizedException("Authorization Required!");
        }
        
        String mainEmail = user.getEmail();
        String userId = user.getUserId();
        
        String displayName = profileForm.getDisplayName();
        String address = profileForm.getAddress();
        
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        
        if(profile == null){
        	if(displayName == null){
        		displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
        	}

        	profile = new Profile(userId, displayName, mainEmail, address);
        
        } else{
        	profile.update(displayName, address, null);
        	
        }
        
        ofy().save().entity(profile).now();

        return profile;
    }

    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", 
    		path = "profile", 
    		httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId(); 
        Key key = Key.create(Profile.class, userId); 
        Profile profile = (Profile) ofy().load().key(key).now();
        return profile;
    }
    
    /**
     * Creates a new Book object and stores it to the datastore.
     *
     * @param user A user who invokes this method, null when the user is not signed in.
     * @param bookForm A BookForm object representing user's inputs.
     * @return A newly created Book Object.
     * @throws UnauthorizedException when the user is not signed in.
     */
    @ApiMethod(name = "createBook", 
    		path = "book", 
    		httpMethod = HttpMethod.POST)
    public Book createBook(final User user, final BookForm bookForm)
        throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        // Allocate a key for the book -- let App Engine allocate the ID
        // Don't forget to include the parent Profile in the allocated ID
        final Key<Book> bookKey = factory().allocateId( Book.class);

        // Get the bookId from the Key
        final long bookId = bookKey.getId();

        // Create a new Book Entity, specifying the user's Profile entity
        // as the parent of the book
        Book book = new Book(bookId, bookForm);

        // Save Book Entities
        ofy().save().entities(book).now();
        
         return book;
         }
    
    /**
     * Queries against the datastore with the given filters and returns the result.
     *
     * Normally this kind of method is supposed to get invoked by a GET HTTP method,
     * but we do it with POST, in order to receive bookQueryForm Object via the POST body.
     *
     * @param bookQueryForm A form object representing the query.
     * @return A List of Book that match the query.
     */
    @ApiMethod(
    		name = "queryBooks",
    		path = "queryBooks",
    		httpMethod = HttpMethod.POST
    )
    public List<Book> queryBooks(BookQueryForm bookQueryForm){
    	//Query<Book> query = ofy().load().type(Book.class).order("name");
    	
    	//return query.list();
    	return bookQueryForm.getQuery().list();
    }
    
    /**
     * Returns a list of cart that the user has checked out.
     *
     * @param user A user who invokes this method, null when the user is not signed in.
     * @return a list of cart that the user checked out.
     * @throws UnauthorizedException when the user is not signed in.
     */
    @ApiMethod(
            name = "getCartCreated",
            path = "getCartCreated",
            httpMethod = HttpMethod.GET
    )
    public List<Cart> getCartCreated(final User user) throws UnauthorizedException{
    	if(user == null){
    		throw new UnauthorizedException("Authorization required");
    	}
    	Profile profile = getProfile(user);
    	if(profile != null){
    		String userId = profile.getUserId();
        	Key profileKey = Key.create(Profile.class, userId);
        	return ofy().load().type(Cart.class).filter("checkedOut", Boolean.TRUE).ancestor(profileKey)
        			.list();
    	}
    	else return null;
    			
    }
    
    /**
     * Returns a list of carts that have been checked out.
     *
     * @return a list of carts that have been checked out
     */
    @ApiMethod(
            name = "getAllCart",
            path = "getAllCart",
            httpMethod = HttpMethod.GET
    )
    public List<Cart> getAllCart() throws UnauthorizedException{

    	return ofy().load().type(Cart.class).filter("checkedOut", Boolean.TRUE)
    			.list();
    	
    			
    }
    


    /**
     * Returns a Book object with the given bookId.
     *
     * @param websafeBookKey The String representation of the Book Key.
     * @return a Book object with the given bookId.
     * @throws NotFoundException when there is no Book with the given bookId.
     */
    @ApiMethod(
            name = "getBook",
            path = "book/{websafeBookKey}",
            httpMethod = HttpMethod.GET
    )
    public Book getBook(
            @Named("websafeBookKey") final String websafeBookKey)
            throws NotFoundException {
        Key<Book> bookKey = Key.create(websafeBookKey);
        Book book = ofy().load().key(bookKey).now();
        if (book == null) {
            throw new NotFoundException("No book found with key: " + websafeBookKey);
        }
        return book;
    }


 /**
     * Just a wrapper for Boolean.
     * We need this wrapped Boolean because endpoints functions must return
     * an object instance, they can't return a Type class such as
     * String or Integer or Boolean
     */
    public static class WrappedBoolean {

        private final Boolean result;
        private final String reason;

        public WrappedBoolean(Boolean result) {
            this.result = result;
            this.reason = "";
        }

        public WrappedBoolean(Boolean result, String reason) {
            this.result = result;
            this.reason = reason;
        }

        public Boolean getResult() {
            return result;
        }

        public String getReason() {
            return reason;
        }
    }

    
    
    @ApiMethod(
            name = "addBookToCart",
            path = "book/{websafeBookKey}",
            httpMethod = HttpMethod.POST
    )
    public WrappedBoolean addBookToCart(final User user,
            @Named("websafeBookKey") final String websafeBookKey)
            throws UnauthorizedException, NotFoundException,
            ForbiddenException, ConflictException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
            @Override
            public WrappedBoolean run() {
               
	                // Get the book key
	                Key<Book> bookKey = Key.create(websafeBookKey);
	
	                // Get the book entity from the datastore
	                Book book = ofy().load().key(bookKey).now();
	
	                // 404 when there is no Book with the given BookId.
	                if (book == null) {
	                    return new WrappedBoolean (false,
	                            "No Book found with key: "
	                                    + websafeBookKey);
	                }

	                // Get the user's Profile entity
	                Profile profile = getProfileFromUser(user);
	
	             // Has the user already had on-going cart (unchecked-out)?
                	//don't have on-going cart
                if(!profile.haveOngoingOrder()){

                    // Get the userId and profileKey
                    final String userId = user.getUserId();
                    Key<Profile> profileKey = Key.create(Profile.class, userId);
                    
                    final Key<Cart> cartKey = factory().allocateId(profileKey, Cart.class);
                    final long cartId = cartKey.getId();

                    Cart cart  = new Cart(cartId, userId);
                    profile.update(cart.getWebsafeKey());
                    
                    ofy().save().entities(cart, profile).now();
                }
                
	                Key<Cart> cartKey = Key.create(profile.getOnGoingCartId());
	                
	                Cart cart = ofy().load().key(cartKey).now();
	                
	                cart.addToBookKeys(websafeBookKey);
	                cart.addPrice(book.getPrice());
	                
	                ofy().save().entity(cart).now();

	                //done!
	                return new WrappedBoolean(true);
  
            }
        });
        // if result is false
        if (!result.getResult()) {
             throw new ForbiddenException("Unknown exception");
        }
        return result;
    }
    
    
    
    @ApiMethod(
            name = "removeBookFromCart",
            path = "removeBookFromCart",

            httpMethod = HttpMethod.POST
    )
    public WrappedBoolean removeBookFromCart(final User user,
            @Named("websafeBookKey") String websafeBookKey)
            throws UnauthorizedException, NotFoundException,
            ForbiddenException, ConflictException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        if(websafeBookKey == null){
            throw new NotFoundException("No book found with key: " + websafeBookKey);
        }

        final String webSafe = websafeBookKey.toString();
        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
            @Override
            public WrappedBoolean run() {
                

	                // Get the book key
	                Key<Book> bookKey = Key.create(webSafe);
	
	                // Get the book entity from the datastore
	                Book book = ofy().load().key(bookKey).now();
	
	                // 404 when there is no Book with the given BookId.
	                if (book == null) {
	                    return new WrappedBoolean (false,
	                            "No Book found with key: "
	                                    + webSafe);
	                }

	                // Get the user's Profile entity
	                Profile profile = getProfileFromUser(user);
	
	             // Has the user already had on-going cart (unchecked-out)?
                	//don't have on-going cart
                if(!profile.haveOngoingOrder()){

                	return new WrappedBoolean (false,
                            "This user has no on-going cart.");
                }
                
	                Key<Cart> cartKey = Key.create(profile.getOnGoingCartId());
	                
	                Cart cart = ofy().load().key(cartKey).now();
	                
	                cart.removeBookFromCart(webSafe);
	                cart.deductPrice(book.getPrice());
	                
	                ofy().save().entity(cart).now();
	                ofy().clear();

	                //done!
	                return new WrappedBoolean(true);
  
            }
        });
        // if result is false
        if (!result.getResult()) {
             throw new ForbiddenException("Unknown exception");
        }
        return result;
    }


    /**
     * Returns a collection of Book Object in an ongoing cart.
     *
     * @param user An user who invokes this method, null when the user is not signed in.
     * @return a Collection of Books that are in an ongoing cart.
     * @throws UnauthorizedException when the User object is null.
     */
    @ApiMethod(
            name = "getBookInCart",
            path = "getBookInCart",
            httpMethod = HttpMethod.GET
    )
    public Collection<Book> getBookInCart(final User user)
            throws UnauthorizedException, NotFoundException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        // Get the Profile entity for the user
        Profile profile = getProfileFromUser(user);
        if (profile == null) {
            throw new NotFoundException("Profile doesn't exist.");
        }

        if(!profile.haveOngoingOrder()){
        	return null;
        }
        // Get the key of the card to get cart entity
        Key<Cart> cartKey = Key.create(profile.getOnGoingCartId());
        Cart cart = ofy().load().key(cartKey).now();
        ofy().clear();
        
        if(cart != null){
            List<String> keyStringsBook = cart.getBookKeys();
            
            
            List<Book> books = new ArrayList<Book>();
            
            for(String keyString : keyStringsBook) {
           	 	Key<Book> bookKey = Key.create(keyString);
           	 	books.add(ofy().cache(false).load().key(bookKey).now());
            }
            
            return books;
        }else{
        	return null;
        }

        
    }
    
    
    /**
     * Returns a collection of Book Object that is in a given cart.
     *
     * @param user An user who invokes this method and websafeCartKey key of the cart.
     * @return a Collection of Book that the is in a given card
     * @throws UnauthorizedException when the User object is null.
     */
    @ApiMethod(
            name = "getBookInCartHistory",
            path = "cart/{websafeCartKey}",
            httpMethod = HttpMethod.GET
    )
    public Collection<Book> getBookInCartHistory(final User user,
    		@Named("websafeCartKey") final String websafeCartKey)
            throws UnauthorizedException, NotFoundException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        // Get the Profile entity for the user
        Profile profile = getProfileFromUser(user);
        if (profile == null) {
            throw new NotFoundException("Profile doesn't exist.");
        }
        
        // Get the key of the card to get cart entity
        Key<Cart> cartKey = Key.create(websafeCartKey);
        Cart cart = ofy().load().key(cartKey).now();
        ofy().clear();

        
        List<String> keyStringsBook = cart.getBookKeys();

        List<Book> books = new ArrayList<Book>();
        for(String keyString : keyStringsBook){
        	Key<Book> bookKey = Key.create(keyString);
        	books.add(ofy().load().key(bookKey).now());
        	ofy().clear();
        }
        
        return books;
    }
    
    
    @ApiMethod(
            name = "getCartData",
            path = "getCartData",
            httpMethod = HttpMethod.GET
    )
    public Cart getCartData(final User user)
            throws UnauthorizedException, NotFoundException {
        // If not signed in, throw a 401 error.
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        // Get the Profile entity for the user
        Profile profile = getProfileFromUser(user);
        if (profile == null) {
            throw new NotFoundException("Profile doesn't exist.");
        }

        if(profile.haveOngoingOrder()){
        	// Get the key of the card to get cart entity
            Key<Cart> cartKey = Key.create(profile.getOnGoingCartId());
            Cart cart = ofy().cache(false).load().key(cartKey).now();
            ofy().clear();
            
            return cart;
        }
        
        return null;
    }
    
    
    @ApiMethod(name = "checkout", 
    		path = "checkout", 
    		httpMethod = HttpMethod.POST)
    public WrappedBoolean  checkout(final User user, final CheckoutForm checkoutForm)
        throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        
        final Queue queue = QueueFactory.getDefaultQueue();

        // Start a transaction.
        Cart cart = ofy().transact(new Work<Cart>() {
            @Override
            public Cart run() {
            	Profile profile = getProfileFromUser(user);

                if(!profile.haveOngoingOrder()){
                	return null;
                }
                // Get the key of the card to get cart entity
                Key<Cart> cartKey = Key.create(profile.getOnGoingCartId());
                Cart cart = ofy().load().key(cartKey).now();
                
                cart.updateWithCheckoutForm(checkoutForm);
                cart.setToCheckOut(true);
                profile.clearOnGoingCartId(); //to clear ongoing cart

                // Save cart and profile Entities
                ofy().save().entities(cart, profile).now();
                
                
                queue.add(ofy().getTransaction(),
                        TaskOptions.Builder.withUrl("/tasks/send_confirmation_email")
                        .param("email", profile.getMainEmail())
                        .param("name", profile.getDisplayName())
                        .param("cartInfo", cart.toString()));
                ofy().clear();

                return cart;
            }
        });
        
         return new WrappedBoolean(true);
    }
    
    /**
     * Returns a Cart object with the given cartId.
     *
     * @param websafeCartKey The String representation of the Cart Key.
     * @return a Cart object with the given cartId.
     * @throws NotFoundException when there is no Cart with the given cartId.
     */
    @ApiMethod(
            name = "getCart",
            path = "cart/detail/{websafeCartKey}",
            httpMethod = HttpMethod.GET
    )
    public Cart getCart(
            @Named("websafeCartKey") final String websafeCartKey)
            throws NotFoundException {
        Key<Cart> cartKey = Key.create(websafeCartKey);
        Cart cart = ofy().load().key(cartKey).now();
        ofy().clear();
        if (cart == null) {
            throw new NotFoundException("No cart found with key: " + websafeCartKey);
        }
        return cart;
    }
    
   
    
}
