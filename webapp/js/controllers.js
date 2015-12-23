'use strict';

/**
 * The root bookstoreApp module.
 *
 * @type {bookstoreApp|*|{}}
 */
var bookstoreApp = bookstoreApp || {};

/**
 * @ngdoc module
 * @name bookControllers
 *
 * @description
 * Angular module for controllers.
 *
 */
bookstoreApp.controllers = angular.module('bookControllers', ['ui.bootstrap']);

/**
 * @ngdoc controller
 * @name MyProfileCtrl
 *
 * @description
 * A controller used for the My Profile page.
 */
bookstoreApp.controllers.controller('MyProfileCtrl',
    function ($scope, $log, oauth2Provider, HTTP_ERRORS) {
        $scope.submitted = false;
        $scope.loading = false;

        /**
         * The initial profile retrieved from the server to know the dirty state.
         * @type {{}}
         */
        $scope.initialProfile = {};
        
        $scope.cart = [];


        /**
         * Initializes the My profile page.
         * Update the profile if the user's profile has been stored.
         */
        $scope.init = function () {
            var retrieveProfileCallback = function () {
                $scope.profile = {};
                $scope.loading = true;
                gapi.client.bookstore.getProfile().
                    execute(function (resp) {
                        $scope.$apply(function () {
                            $scope.loading = false;
                            if (resp.error) {
                                // Failed to get a user profile.
                            } else {
                                // Succeeded to get the user profile.
                                $scope.profile.displayName = resp.result.displayName;
                                $scope.profile.address = resp.result.address;
                                $scope.initialProfile = resp.result;
                            }
                        });
                    }
                );
            };
            if (!oauth2Provider.signedIn) {
                var modalInstance = oauth2Provider.showLoginModal();
                modalInstance.result.then(retrieveProfileCallback);
            } else {
                retrieveProfileCallback();
            }
            
            gapi.client.bookstore.getCartCreated().
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to query the cart created : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages);

                    } else {
                        // The request has succeeded.
                        $scope.submitted = false;
                        $scope.messages = 'DONE';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);

                        $scope.cart = [];
                        angular.forEach(resp.items, function (cart) {
                            $scope.cart.push(cart);
                        });
                    }
                    $scope.submitted = true;
                });
            });
            
        };


        /**
         * Invokes the bookstore.saveProfile API.
         *
         */
        $scope.saveProfile = function () {
            $scope.submitted = true;
            $scope.loading = true;
            gapi.client.bookstore.saveProfile($scope.profile).
                execute(function (resp) {
                    $scope.$apply(function () {
                        $scope.loading = false;
                        if (resp.error) {
                            // The request has failed.
                            var errorMessage = resp.error.message || '';
                            $scope.messages = 'Failed to update a profile : ' + errorMessage;
                            $scope.alertStatus = 'warning';
                            $log.error($scope.messages + 'Profile : ' + JSON.stringify($scope.profile));

                            if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                                oauth2Provider.showLoginModal();
                                return;
                            }
                        } else {
                            // The request has succeeded.
                            $scope.messages = 'The profile has been updated';
                            $scope.alertStatus = 'success';
                            $scope.submitted = false;
                            $scope.initialProfile = {
                                displayName: $scope.profile.displayName,
                                teeShirtSize: $scope.profile.address
                            };

                            $log.info($scope.messages + JSON.stringify(resp.result));
                        }
                    });
                });
        };
        
        
        /**
         * Namespace for the pagination.
         * @type {{}|*}
         */
        $scope.pagination = $scope.pagination || {};
        $scope.pagination.currentPage = 0;
        $scope.pagination.pageSize = 20;
        /**
         * Returns the number of the pages in the pagination.
         *
         * @returns {number}
         */
        $scope.pagination.numberOfPages = function () {
            return Math.ceil($scope.cart.length / $scope.pagination.pageSize);
        };

        /**
         * Returns an array including the numbers from 1 to the number of the pages.
         *
         * @returns {Array}
         */
        $scope.pagination.pageArray = function () {
            var pages = [];
            var numberOfPages = $scope.pagination.numberOfPages();
            for (var i = 0; i < numberOfPages; i++) {
                pages.push(i);
            }
            return pages;
        };

        /**
         * Checks if the target element that invokes the click event has the "disabled" class.
         *
         * @param event the click event
         * @returns {boolean} if the target element that has been clicked has the "disabled" class.
         */
        $scope.pagination.isDisabled = function (event) {
            return angular.element(event.target).hasClass('disabled');
        }
        




});


/**
 * @ngdoc controller
 * @name CreatBookCtrl
 *
 * @description
 * A controller used for the Create a new book.
 */
bookstoreApp.controllers.controller('CreatBookCtrl',
    function ($scope, $log, oauth2Provider, HTTP_ERRORS) {

        /**
         * The book object being edited in the page.
         * @type {{}|*}
         */
        $scope.book = $scope.book || {};

        /**
         * Holds the default values for the input candidates for topics select.
         * @type {string[]}
         */
        $scope.categories = [			
			'Art & Photography',
			'Audio Books',
			'Biography',
			'Business, Finance & Law',
			'Children Books',
			'Computing',
			'Crafts and Hobbies',
			'Crime & Thriller',
			'Dictionaries & Languages',
			'Entertainment',
			'Fiction',
			'Food & Drink',
			'Graphic Novels, Anime & Manga',
			'Health',
			'History & Archaeology',
			'Home & Garden',
			'Humour',
			'Medical',
			'Mind, Body & Spirit',
			'Natural History',
			'Personal Development',
			'Poetry & Drama',
			'Reference',
			'Religion',
			'Romance',
			'Science & Geography',
			'Science Fiction, Fantasy & Horror',
			'Society & Social Sciences',
			'Sport',
			'Stationery',
			'Teaching Resources & Education',
			'Technology & Engineering',
			'Transport',
			'Travel & Holiday Guides'
        ];


        /**
         * Tests if $scope.book is valid.
         * @param bookForm the form object from the create_books.html page.
         * @returns {boolean|*} true if valid, false otherwise.
         */
        $scope.isValidBook = function (bookForm) {
            return !bookForm.$invalid;
        }

        /**
         * Invokes the bookstore.createBook API.
         *
         * @param bookForm the form object.
         */
        $scope.createBook = function (bookForm) {

            $scope.loading = true;
            gapi.client.bookstore.createBook($scope.book).
                execute(function (resp) {
                    $scope.$apply(function () {
                        $scope.loading = false;
                        if (resp.error) {
                            // The request has failed.
                            var errorMessage = resp.error.message || '';
                            $scope.messages = 'Failed to create a book : ' + errorMessage;
                            $scope.alertStatus = 'warning';
                            $log.error($scope.messages + ' Book : ' + JSON.stringify($scope.book));

                            if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                                oauth2Provider.showLoginModal();
                                return;
                            }
                        } else {
                            // The request has succeeded.
                            $scope.messages = 'The book has been created : ' + resp.result.name;
                            $scope.alertStatus = 'success';
                            $scope.submitted = false;
                            $scope.book = {};
                            $log.info($scope.messages + ' : ' + JSON.stringify(resp.result));
                        }
                    });
                });
        };
    });

/**
 * @ngdoc controller
 * @name ShowBookCtrl
 *
 * @description
 * A controller used for the Show books page.
 */
bookstoreApp.controllers.controller('ShowBookCtrl', function ($scope, $log, oauth2Provider, HTTP_ERRORS) {

    /**
     * Holds the status if the query is being executed.
     * @type {boolean}
     */
    $scope.submitted = false;

    $scope.selectedTab = 'ALL';

    /**
     * Holds the filters that will be applied when queryBooksAll is invoked.
     * @type {Array}
     */
    $scope.filters = [
    ];

    $scope.filtereableFields = [
        {enumValue: 'NAME', displayName: 'Name'},
        {enumValue: 'AUTHOR', displayName: 'Author'}
    ]


    /**
     * Holds the Books currently displayed in the page.
     * @type {Array}
     */
    $scope.books = [];

    /**
     * Holds the state if offcanvas is enabled.
     *
     * @type {boolean}
     */
    $scope.isOffcanvasEnabled = false;

    /**
     * Sets the selected tab to 'ALL'
     */
    $scope.tabAllSelected = function () {
        $scope.selectedTab = 'ALL';
        $scope.queryBooks();
    };



    /**
     * Toggles the status of the offcanvas.
     */
    $scope.toggleOffcanvas = function () {
        $scope.isOffcanvasEnabled = !$scope.isOffcanvasEnabled;
    };

    /**
     * Namespace for the pagination.
     * @type {{}|*}
     */
    $scope.pagination = $scope.pagination || {};
    $scope.pagination.currentPage = 0;
    $scope.pagination.pageSize = 20;
    /**
     * Returns the number of the pages in the pagination.
     *
     * @returns {number}
     */
    $scope.pagination.numberOfPages = function () {
        return Math.ceil($scope.books.length / $scope.pagination.pageSize);
    };

    /**
     * Returns an array including the numbers from 1 to the number of the pages.
     *
     * @returns {Array}
     */
    $scope.pagination.pageArray = function () {
        var pages = [];
        var numberOfPages = $scope.pagination.numberOfPages();
        for (var i = 0; i < numberOfPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    /**
     * Checks if the target element that invokes the click event has the "disabled" class.
     *
     * @param event the click event
     * @returns {boolean} if the target element that has been clicked has the "disabled" class.
     */
    $scope.pagination.isDisabled = function (event) {
        return angular.element(event.target).hasClass('disabled');
    }

    /**
     * Adds a filter and set the default value.
     */
    $scope.addFilter = function () {
        $scope.filters.push({
            field: $scope.filtereableFields[0],
            value: ''
        })
    };

    /**
     * Clears all filters.
     */
    $scope.clearFilters = function () {
        $scope.filters = [];
    };

    /**
     * Removes the filter specified by the index from $scope.filters.
     *
     * @param index
     */
    $scope.removeFilter = function (index) {
        if ($scope.filters[index]) {
            $scope.filters.splice(index, 1);
        }
    };

    /**
     * Query the books depending on the tab currently selected.
     *
     */
    $scope.queryBooks = function () {
        $scope.submitted = false;
        if ($scope.selectedTab == 'ALL') {
            $scope.queryBooksAll();
        } 
    };

    /**
     * Invokes the bookstore.queryBooks API.
     */
    $scope.queryBooksAll = function () {
        var sendFilters = {
            filters: []
        }
        for (var i = 0; i < $scope.filters.length; i++) {
            var filter = $scope.filters[i];
            if (filter.field && filter.value) {
                sendFilters.filters.push({
                    field: filter.field.enumValue,
                    value: filter.value
                });
            }
        }
        $scope.loading = true;
        gapi.client.bookstore.queryBooks(sendFilters).
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to query : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages + ' filters : ' + JSON.stringify(sendFilters));
                    } else {
                        // The request has succeeded.
                        $scope.submitted = false;
                        $scope.messages = 'Query succeeded : ' + JSON.stringify(sendFilters);
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);

                        $scope.books = [];
                        angular.forEach(resp.items, function (book) {
                            $scope.books.push(book);
                        });
                    }
                    $scope.submitted = true;
                });
            });
    }



});


/**
 * @ngdoc controller
 * @name BookDetailCtrl
 *
 * @description
 * A controller used for the book detail page.
 */
bookstoreApp.controllers.controller('BookDetailCtrl', function ($scope, $log, $routeParams, HTTP_ERRORS) {
    $scope.book = {};

    $scope.isUserAttending = false;

    /**
     * Initializes the book detail page.
     * Invokes the book.getBook method and sets the returned book in the $scope.
     *
     */
    $scope.init = function () {
        $scope.loading = true;
        gapi.client.bookstore.getBook({
            websafeBookKey: $routeParams.websafeBookKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    // The request has failed.
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Failed to get the book : ' + $routeParams.websafeKey
                        + ' ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);
                } else {
                    // The request has succeeded.
                    $scope.alertStatus = 'success';
                    $scope.book = resp.result;
                }
            });
        });


    };


    /**
     * Invokes the bookstore.addToCart method.
     */
    $scope.addBookToCart = function () {
        $scope.loading = true;
        gapi.client.bookstore.addBookToCart({
            websafeBookKey: $routeParams.websafeBookKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    // The request has failed.
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Failed to add this book to cart : ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);

                    if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                        oauth2Provider.showLoginModal();
                        return;
                    }
                } else {
                    if (resp.result) {
                        // Register succeeded.
                        $scope.messages = 'Successfully Added to cart';
                        $scope.alertStatus = 'success';
                        $scope.isUserAttending = true;
                    } else {
                        $scope.messages = 'Failed to add this book to cart';
                        $scope.alertStatus = 'warning';
                    }
                }
            });
        });
    };


});



/**
 * @ngdoc controller
 * @name MyCartCtrl
 *
 * @description
 * A controller used for the Show books page.
 */
bookstoreApp.controllers.controller('MyCartCtrl', function ($scope, $log, oauth2Provider, HTTP_ERRORS, $location, $route) {


    /**
     * Holds the books currently displayed in the page.
     * @type {Array}
     */
    $scope.books = [];
    $scope.submitted = false;


    /**
     * Namespace for the pagination.
     * @type {{}|*}
     */
    $scope.pagination = $scope.pagination || {};
    $scope.pagination.currentPage = 0;
    $scope.pagination.pageSize = 20;
    /**
     * Returns the number of the pages in the pagination.
     *
     * @returns {number}
     */
    $scope.pagination.numberOfPages = function () {
        return Math.ceil($scope.books.length / $scope.pagination.pageSize);
    };

    /**
     * Returns an array including the numbers from 1 to the number of the pages.
     *
     * @returns {Array}
     */
    $scope.pagination.pageArray = function () {
        var pages = [];
        var numberOfPages = $scope.pagination.numberOfPages();
        for (var i = 0; i < numberOfPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    /**
     * Checks if the target element that invokes the click event has the "disabled" class.
     *
     * @param event the click event
     * @returns {boolean} if the target element that has been clicked has the "disabled" class.
     */
    $scope.pagination.isDisabled = function (event) {
        return angular.element(event.target).hasClass('disabled');
    }
    


    /**
     * Invokes the bookstore.getBookInCart API.
     */
    $scope.init = function () {
        $scope.loading = true;
        gapi.client.bookstore.getBookInCart().
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to get books : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages + ' fail : ');
                        
                        if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                            oauth2Provider.showLoginModal();
                            return;
                        }
                        
                    } else {
                        // The request has succeeded.
                        $scope.messages = 'DONE';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);
                        $scope.submitted = false;
                        $scope.books = [];
                        angular.forEach(resp.items, function (book) {
                            $scope.books.push(book);
                        });
                    }
                    $scope.submitted = true;
                });
            });
        
        $scope.cart = {}; 
        gapi.client.bookstore.getCartData().
        execute(function (resp) {
        $scope.$apply(function () {
            $scope.loading = false;
            if (resp.error) {
                // The request has failed.
                var errorMessage = resp.error.message || '';
                $scope.messages = 'Failed to get the cart ' + errorMessage;
                $scope.alertStatus = 'warning';
                $log.error($scope.messages);
            } else {
                // The request has succeeded.
                $scope.cart = resp.result;
            }
        });
    });
    }
    
    /**
     * The book object being edited in the page.
     * @type {{}|*}
     */
    $scope.cart = $scope.cart || {};


    /**
     * Tests if $scope.cart is valid.
     * @param bookForm the form object from the create_books.html page.
     * @returns {boolean|*} true if valid, false otherwise.
     */
    $scope.isValidChckout = function (checkoutForm) {
        return !checkoutForm.$invalid;
    }

    /**
     * Invokes the bookstore.checkout API.
     *
     * @param checkoutForm the form object.
     */
    $scope.checkout = function (checkoutForm) {
        if (!$scope.isValidChckout(checkoutForm)) {
            return;
        }
        $scope.loading = true;
        gapi.client.bookstore.checkout($scope.cart).
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to checkout : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages + ' Cart : ' + JSON.stringify($scope.cart));

                        if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                            oauth2Provider.showLoginModal();
                            return;
                        }
                    } else {
                        // The request has succeeded.
                        $scope.messages = 'Checked out! Thanks! : ' + resp.result.name;
                        $scope.alertStatus = 'success';
                        $scope.submitted = false;
                        $scope.book = {};
                        $log.info($scope.messages + ' : ' + JSON.stringify(resp.result));
                        $location.path( "/thankyou" );
                    }
                });
            });
    };
    
    
    /**
     * Invokes the bookstore.removeBookFromCart API.
     *
     * @param websafeBookKey
     */
    $scope.removeBookFromCart = function (websafeBookKey) {
        if (websafeBookKey == null) {
            return;
        }

        $scope.loading = true;
        gapi.client.bookstore.removeBookFromCart({'websafeBookKey': websafeBookKey}).
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to remove a book : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages + ' Book : ' + JSON.stringify(websafeBookKey));

                        if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                            oauth2Provider.showLoginModal();
                            return;
                        }
                    } else {
                        // The request has succeeded.
                        $scope.messages = 'Removed from cart : ' + resp.result.name;
                        $scope.alertStatus = 'success';
                        $scope.submitted = false;
                        $scope.book = {};
                        $log.info($scope.messages + ' : ' + JSON.stringify(resp.result));
                        $route.reload();

                    }
                });
            });
    };
    
    
    
});



/**
 * @ngdoc controller
 * @name CartDetailCtrl
 *
 * @description
 * A controller used for the cart detail page.
 */
bookstoreApp.controllers.controller('CartDetailCtrl', function ($scope, $log, $routeParams, HTTP_ERRORS) {


    /**
     * Holds the books currently displayed in the page.
     * @type {Array}
     */
    $scope.books = [];

    $scope.cart = {};
    
    /**
     * Initializes the cart detail page.
     * Invokes the bookstore.getCart method and sets the returned cart in the $scope.
     *
     */
    $scope.init = function () {
        $scope.loading = true;
        gapi.client.bookstore.getCart({
            websafeCartKey: $routeParams.websafeCartKey
        }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    // The request has failed.
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Failed to get the cart : ' + $routeParams.websafeKey
                        + ' ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages);
                } else {
                    // The request has succeeded.
                    $scope.alertStatus = 'success';
                    $scope.cart = resp.result;
                }
            });
        });
        
        gapi.client.bookstore.getBookInCartHistory({
        	websafeCartKey: $routeParams.websafeCartKey
            }).execute(function (resp) {
            $scope.$apply(function () {
                $scope.loading = false;
                if (resp.error) {
                    // The request has failed.
                    var errorMessage = resp.error.message || '';
                    $scope.messages = 'Failed to get books : ' + errorMessage;
                    $scope.alertStatus = 'warning';
                    $log.error($scope.messages + ' fail : ');
                    
                    if (resp.code && resp.code == HTTP_ERRORS.UNAUTHORIZED) {
                        oauth2Provider.showLoginModal();
                        return;
                    }
                    
                } else {
                    // The request has succeeded.
                    $scope.messages = 'DONE';
                    $scope.alertStatus = 'success';
                    $log.info($scope.messages);
                    $scope.submitted = false;
                    $scope.books = [];
                    angular.forEach(resp.items, function (book) {
                        $scope.books.push(book);
                    });
                }
                $scope.submitted = true;
            });
        });


    };
    

    /**
     * Namespace for the pagination.
     * @type {{}|*}
     */
    $scope.pagination = $scope.pagination || {};
    $scope.pagination.currentPage = 0;
    $scope.pagination.pageSize = 20;
    /**
     * Returns the number of the pages in the pagination.
     *
     * @returns {number}
     */
    $scope.pagination.numberOfPages = function () {
        return Math.ceil($scope.books.length / $scope.pagination.pageSize);
    };

    /**
     * Returns an array including the numbers from 1 to the number of the pages.
     *
     * @returns {Array}
     */
    $scope.pagination.pageArray = function () {
        var pages = [];
        var numberOfPages = $scope.pagination.numberOfPages();
        for (var i = 0; i < numberOfPages; i++) {
            pages.push(i);
        }
        return pages;
    };

    /**
     * Checks if the target element that invokes the click event has the "disabled" class.
     *
     * @param event the click event
     * @returns {boolean} if the target element that has been clicked has the "disabled" class.
     */
    $scope.pagination.isDisabled = function (event) {
        return angular.element(event.target).hasClass('disabled');
    }
    

	});

	/**
	 * @ngdoc controller
	 * @name AllCartCtrl
	 *
	 * @description
	 * A controller used for getting all orders (carts).
	 */
	bookstoreApp.controllers.controller('AllCartCtrl', function ($scope, $log, $routeParams, HTTP_ERRORS) {
	    
		$scope.cart = [];
	
	    $scope.isUserAttending = false;
	
	    /**
	     * Initializes the All cart page.
	     * Invokes the bookstore.getAllCart method and sets the returned all carts in the $scope.
	     *
	     */
	    $scope.init = function () {
	        $scope.loading = true;
            gapi.client.bookstore.getAllCart().
            execute(function (resp) {
                $scope.$apply(function () {
                    $scope.loading = false;
                    if (resp.error) {
                        // The request has failed.
                        var errorMessage = resp.error.message || '';
                        $scope.messages = 'Failed to query the cart created : ' + errorMessage;
                        $scope.alertStatus = 'warning';
                        $log.error($scope.messages);

                    } else {
                        // The request has succeeded.
                        $scope.submitted = false;
                        $scope.messages = 'DONE';
                        $scope.alertStatus = 'success';
                        $log.info($scope.messages);

                        $scope.cart = [];
                        angular.forEach(resp.items, function (cart) {
                            $scope.cart.push(cart);
                        });
                    }
                    $scope.submitted = true;
                });
            });
	    };
	    

	    /**
	     * Namespace for the pagination.
	     * @type {{}|*}
	     */
	    $scope.pagination = $scope.pagination || {};
	    $scope.pagination.currentPage = 0;
	    $scope.pagination.pageSize = 20;
	    /**
	     * Returns the number of the pages in the pagination.
	     *
	     * @returns {number}
	     */
	    $scope.pagination.numberOfPages = function () {
	        return Math.ceil($scope.cart.length / $scope.pagination.pageSize);
	    };

	    /**
	     * Returns an array including the numbers from 1 to the number of the pages.
	     *
	     * @returns {Array}
	     */
	    $scope.pagination.pageArray = function () {
	        var pages = [];
	        var numberOfPages = $scope.pagination.numberOfPages();
	        for (var i = 0; i < numberOfPages; i++) {
	            pages.push(i);
	        }
	        return pages;
	    };

	    /**
	     * Checks if the target element that invokes the click event has the "disabled" class.
	     *
	     * @param event the click event
	     * @returns {boolean} if the target element that has been clicked has the "disabled" class.
	     */
	    $scope.pagination.isDisabled = function (event) {
	        return angular.element(event.target).hasClass('disabled');
	    }
	    
	});



/**
 * @ngdoc controller
 * @name RootCtrl
 *
 * @description
 * The root controller having a scope of the body element and methods used in the application wide
 * such as user authentications.
 *
 */
bookstoreApp.controllers.controller('RootCtrl', function ($scope, $location, oauth2Provider) {

    /**
     * Returns if the viewLocation is the currently viewed page.
     *
     * @param viewLocation
     * @returns {boolean} true if viewLocation is the currently viewed page. Returns false otherwise.
     */
    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };

    /**
     * Returns the OAuth2 signedIn state.
     *
     * @returns {oauth2Provider.signedIn|*} true if siendIn, false otherwise.
     */
    $scope.getSignedInState = function () {
        return oauth2Provider.signedIn;
    };

    /**
     * Calls the OAuth2 authentication method.
     */
    $scope.signIn = function () {
        oauth2Provider.signIn(function () {
            gapi.client.oauth2.userinfo.get().execute(function (resp) {
                $scope.$apply(function () {
                    if (resp.email) {
                        oauth2Provider.signedIn = true;
                        $scope.alertStatus = 'success';
                        $scope.rootMessages = 'Logged in with ' + resp.email;
                    }
                });
            });
        });
    };

    /**
     * Render the signInButton and restore the credential if it's stored in the cookie.
     * (Just calling this to restore the credential from the stored cookie. So hiding the signInButton immediately
     *  after the rendering)
     */
    $scope.initSignInButton = function () {
        gapi.signin.render('signInButton', {
            'callback': function () {
                jQuery('#signInButton button').attr('disabled', 'true').css('cursor', 'default');
                if (gapi.auth.getToken() && gapi.auth.getToken().access_token) {
                    $scope.$apply(function () {
                        oauth2Provider.signedIn = true;
                    });
                }
            },
            'clientid': oauth2Provider.CLIENT_ID,
            'cookiepolicy': 'single_host_origin',
            'scope': oauth2Provider.SCOPES
        });
    };

    /**
     * Logs out the user.
     */
    $scope.signOut = function () {
        oauth2Provider.signOut();
        $scope.alertStatus = 'success';
        $scope.rootMessages = 'Logged out';
    };

    /**
     * Collapses the navbar on mobile devices.
     */
    $scope.collapseNavbar = function () {
        angular.element(document.querySelector('.navbar-collapse')).removeClass('in');
    };

});


/**
 * @ngdoc controller
 * @name OAuth2LoginModalCtrl
 *
 * @description
 * The controller for the modal dialog that is shown when an user needs to login to achive some functions.
 *
 */
bookstoreApp.controllers.controller('OAuth2LoginModalCtrl',
    function ($scope, $modalInstance, $rootScope, oauth2Provider) {
        $scope.singInViaModal = function () {
            oauth2Provider.signIn(function () {
                gapi.client.oauth2.userinfo.get().execute(function (resp) {
                    $scope.$root.$apply(function () {
                        oauth2Provider.signedIn = true;
                        $scope.$root.alertStatus = 'success';
                        $scope.$root.rootMessages = 'Logged in with ' + resp.email;
                    });

                    $modalInstance.close();
                });
            });
        };
    });

/**
 * @ngdoc controller
 * @name DatepickerCtrl
 *
 * @description
 * A controller that holds properties for a datepicker.
 */
bookstoreApp.controllers.controller('DatepickerCtrl', function ($scope) {
    $scope.today = function () {
        $scope.dt = new Date();
    };
    $scope.today();

    $scope.clear = function () {
        $scope.dt = null;
    };

    // Disable weekend selection
    $scope.disabled = function (date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.toggleMin = function () {
        $scope.minDate = ( $scope.minDate ) ? null : new Date();
    };
    $scope.toggleMin();

    $scope.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.opened = true;
    };

    $scope.dateOptions = {
        'year-format': "'yy'",
        'starting-day': 1
    };

    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
    $scope.format = $scope.formats[0];
});
