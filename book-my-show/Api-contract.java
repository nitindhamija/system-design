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

API -> BookUserSelectedSeat(showTimeId, List<Integer> seatIds, dateTime, cinemaId)

API -> CallbackTimeoutForUserSelectedSeats(showTimeId, List<Integer> seatIds)

*/
// API implentation for Browsing theatres currently running the show (movie selected) in the town, including show timing by a chosen date

// controller endpoint
@GetMapping("/bms/v1/showtimes-by-movie-city")
public ResponseEntity<List<showTimes>> GetAllShowTimesByMovieAndCity(@RequestParam(value = "movieId", required = true) final Integer movieId,
                                        @RequestParam(value = "cityId") final Integer cityId, final JwtAuthenticationToken auth) throws Throwable {
                                            //auth i.e jwt token being verfied by common lib added as dependency in this service
                                            try{
                                                List<showTimes> showTimesList = service.getShowTimesByMovieAndCity();
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


// service 


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
