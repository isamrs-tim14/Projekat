package com.isamrs.tim14.rest;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.isamrs.tim14.dao.HotelDAO;
import com.isamrs.tim14.dto.ServiceDiscount;
import com.isamrs.tim14.model.Hotel;
import com.isamrs.tim14.model.RentACar;
import com.isamrs.tim14.model.Room;
import com.isamrs.tim14.repository.IHotelRepository;
import com.isamrs.tim14.service.HotelService;

@RestController
@RequestMapping("/api")
public class HotelRest {
	
	private HotelDAO hotelDAO;
	
	@Autowired
	public HotelRest(HotelDAO hotelDAO) {
		this.hotelDAO = hotelDAO;
	}
	
	@Autowired
	private HotelService hotelService;
	
	@RequestMapping(
			value = "/hotels",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Hotel>> getHotels(){
		
		Collection<Hotel> hotels = hotelService.getHotels();
		
		return new ResponseEntity<Collection<Hotel>>(hotels, HttpStatus.OK);
	}
	

	@RequestMapping(
			value = "/hotelsSearch/{hotelName}/{hotelDestination}/{checkIn}/{checkOut}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Hotel>> getHotelsSearch(@PathVariable String hotelName, @PathVariable String hotelDestination, @PathVariable long checkIn, @PathVariable long checkOut){
		Collection<Hotel> hotels = hotelService.getHotelsSearch(hotelName, hotelDestination, checkIn, checkOut);
		
		return new ResponseEntity<Collection<Hotel>>(hotels, HttpStatus.OK);
	}
	
	@RequestMapping(
			value = "/hotels/{hotelID}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Hotel> getHotel(@PathVariable Integer hotelID) {
		Hotel hotel =  hotelService.getHotel(hotelID);
		if(hotel == null) {
			return new ResponseEntity<Hotel>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Hotel>(hotel, HttpStatus.OK);
	}
	
	@RequestMapping(
			value = "/hotels",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Hotel> saveHotel(@RequestBody Hotel hotel) {
		Hotel newHotel = hotelService.save(hotel);
		if(newHotel == null) {
			return new ResponseEntity<Hotel>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Hotel>(newHotel, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/setRoomDiscountServices",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> saveHotel(@RequestBody ServiceDiscount discount) {
		Hotel hotel = hotelService.setDiscount(discount.getDiscount());
		return new ResponseEntity<Boolean>(true, HttpStatus.ACCEPTED);
	}
	
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/changeHotel",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Hotel> changeHotel(@RequestBody Hotel hotel) {
		Hotel managedHotel = hotelService.changeHotel(hotel);
		if(managedHotel == null) {
			return new ResponseEntity<Hotel>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Hotel>(managedHotel, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@RequestMapping(value = "/getGradeForHotel/{id}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getUserGrade(@PathVariable Integer id) {
		Integer grade = hotelService.getGrade(id);
		return new ResponseEntity<Integer>(grade, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@RequestMapping(value = "/setGradeForHotel/{id}/{grade}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> setUserGrade(@PathVariable Integer id, @PathVariable Integer grade) {
		hotelService.setGrade(id, grade);
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@RequestMapping(
			value = "/reservedHotels",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Hotel>> getReservationHotel(){
		
		Collection<Hotel> res = hotelService.getHotelsFromReservations();
		
		return new ResponseEntity<Collection<Hotel>>(res, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/getGradeForHotel",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getGrade() {
		Integer grade = hotelService.getGradeHotel();
		return new ResponseEntity<Integer>(grade, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/getHotelIncomes/{startDate}/{endDate}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Double> getIncome(@PathVariable Long startDate, @PathVariable Long endDate) {
		Date start = new Date(startDate);
		Date end = new Date(endDate);
		double income = hotelService.getIncome(start, end);
		return new ResponseEntity<Double>(income, HttpStatus.OK);
	}
	
	
	
}
