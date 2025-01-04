/**  API Contract
- rest api will be used between client and services

API -> GetListOfCities()

API -> GetListOfCinemaByCity(CityId)

API -> GetAllShowTimesByMovieAndCity(cityId, movieId, dateTime)

API -> GetMoviesByCinemaAndCity(CityId, CinemaId)

API -> GetShowTimingByMovieAndCinema(movieId,CinemaId)

API -> GetAvailableSeats(movieId,cinemaId,showTimeId)

API -> VerifyUserSelectedSeatsAvailable(movietid,cinemaId,showTimeId,seats)

API -> BlockUserSelectedSeats(showTimeId, List<Integer> seatIds)

API -> BookUserSelectedSeat(showTimeId, List<Integer> seatIds, cinemaId)

API -> CallbackTimeoutForUserSelectedSeats(showTimeId, List<Integer> seatIds)

*/
// API implentation for Browsing theatres currently running the show (movie selected) in the town, including show timing by a chosen date

// contoller class
// controller endpoints

// read case
@GetMapping("/bms/v1/showtimes-by-movie-city")
public ResponseEntity<List<showTimes>> GetAllShowTimesByMovieAndCity(@RequestParam(value = "movieId", required = true) final Integer movieId,
                                        @RequestParam(value = "cityId") final Integer cityId, final JwtAuthenticationToken auth) throws Throwable {
                                            //auth i.e jwt token being verfied by common lib added as dependency in this service
                                            try{
                                                List<showTimes> showTimesList = this.service.getShowTimesByMovieAndCity();
                                                response = ResponseEntity
                                                .status(HttpStatus.OK)
                                                .header("x-Result-Count", String.valueOf(showTimesList.size()))
                                                .body(showTimesList);
                                            }catch (final FeignException e) {
                                                logger.error("error occurred {}", e.getErrorMessage());
                                                response = propagateError(e);
                                            }
                                            return response;                                        
                                        }



// write case

@PostMapping("/bms/v1/book-show/{showtime-id}")
public ResponseEntity BookUserSelectedSeat(@PathVariable("showtime-id") final String showTimeId, @RequestParam(value = "seat-ids") final List<Integer> seatIds, @RequestParam(value = "cinema-id") final Integer cinemaId , final JwtAuthenticationToken auth) {
		User user = auth.getUser(auth);
        logger.debug("booking the show {}", showTimeId);
		ResponseEntity response;
        try {
            final Booking booking = this.service.BookUserSelectedSeat(showTimeId, seatIds, cinemaId, user.getId());
            response = ResponseEntity.ok(booking);
        } catch (final FeignException ex) {
            logger.error("error occurred {}", e.getErrorMessage());
            response = propagateError(e);
        }
        return response;
    }

@PostMapping("/bms/v1/make-payment/{booking-id}")
public ResponseEntity payForBooking(@PathVariable("booking-id") final String bookingId, @RequestParam(value = "payment-method") final PaymentMethod paymentMethod final JwtAuthenticationToken auth) {
		User user = auth.getUser(auth);
        logger.debug("making payment against the booking id {}", bookingId);
		ResponseEntity response;
        try {
            final List<Ticket> tickets = paymentService.makePaymentForBooking(bookingId, paymentMethod, user.getId());
            response = ResponseEntity.ok(tickets);
        } catch (final FeignException ex) {
            logger.error("error occurred {}", e.getErrorMessage());
            response = propagateError(e);
        }
        return tickets;
    }


// service class
@Autowired
BookingRepository bookingRepository


@Autowired
CinemaRepository CinemaRepository

@Autowired
ShowTimeRepository ShowTimeRepository

@Autowired
RedisRepository redisRep

// get showtimes
public List<showTimes> getShowTimesByMovieAndCity(final String cuid, final Integer movieId, final Integer cityId) throws ExecutionException, InterruptedException {
        try {
                List<integer> cinemas = CinemaRepository.findAllByCity(cityId).stream().map(c -> c.getId()).collect(Collectors.toList());
                List<ShowTimes> showTimes = ShowTimeRepository.findAllShowTimesByMovieAndCinema(movieId, List<Integer> cinemaId);
        }catch(Exception ex) {
                logger.error("DB error occurred {}", ex.getErrorMessage());
                throw ex;
        }
        return showTimes;
  }
  
  
// book/reserve seats for a showtime  
public Booking BookUserSelectedSeat(final String showTimeId, final List<Integer> seatIds, final Integer cinemaId, final Integer userId) throws ExecutionException, InterruptedException {
		Booking booking = null;
        try {
                 // fetch each seat price based on it's type gold/standard and add their price also apply 50% discount on third ticket
				 float totalPrice;
				 var ticketCount = seatIds.size();
				 for(int i = 0; i < ticketCount; i++){
					  SeatType seatType = SeatTypeRepository.findById(seatId);
					  totalPrice += i == 2 ? seatType.getPrice()*.5 : seatType.getPrice();
				 }
				 // create a new Booking 
				 var paymentStatus = "pending";
				 booking = new booking(showTimeId,userId, ticketCount , bookingTimestamp, paymentStatus);	
				 Booking booking = bookingRepository.insert(booking);
				 // update this booking in redis cache with expiry set 10 min and a redis callback which will roll back this txn i.e deleting this booking and unblock seats				 
				 redisRep.setkey(booking, 10000);	 
        }catch(Exception ex) {
                logger.error("error occurred {}", ex.getErrorMessage());
                throw ex;
        } 
		return booking;		
  }

//  
public Booking makePaymentForBooking(final String bookingId, final PaymentMethod method, final Integer userId) throws ExecutionException, InterruptedException {
		
		Payment payment = null;
		List<Ticket> tickets = null;
		var paymentStatus = null;
        try {
                 // fetch each seat price based on it's type gold/standard and add their price also apply 50% discount on third ticket
				 Booking booking = bookingRepository.findById(bookingId);
				 try{
					 Response<Payment> paymentResponse = paymentApiGateway.makePayment(booking.getTotalPrice(), method, userId);
				 }catch(Exception ex){
					  logger.error("payment failure occurred {}", ex.getErrorMessage());
				 }
				 // create a new Booking 
				 if(paymentResponse == Status.ok){
					 paymentStatus = "confirmed";
					 // if payment is success no need to wait for redis key to expire,  delete the entry immediately
					 redisRep.removekey(booking);	 
					 // generates Tickets
					 tickets =  generateTickets(booking, paymentResponse.getPayment());
					 
				 }else{
					 paymentStatus = "failed"; 					 
				 }
				 booking.setPaymentStatus(paymentStatus);
			     Booking booking = bookingRepository.update(booking);
				 
        }catch(Exception ex) {
                logger.error("error occurred while fetching booking information {}", ex.getErrorMessage());
                throw ex;
        } 
		return tickets;		
  }
