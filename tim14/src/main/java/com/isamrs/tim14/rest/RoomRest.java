package com.isamrs.tim14.rest;

import java.util.Collection;

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

import com.isamrs.tim14.dao.RoomDAO;
import com.isamrs.tim14.model.Room;
import com.isamrs.tim14.service.RoomService;

@RestController
@RequestMapping("/api")
public class RoomRest {

	private RoomDAO roomDAO;
	
	@Autowired
	public RoomRest(RoomDAO roomDAO) {
		this.roomDAO = roomDAO;
	}
	
	@Autowired
	private RoomService roomService;
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/rooms",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Room> saveRoom(@RequestBody Room room) {
		Room newRoom = roomService.save(room);

		if(newRoom == null) {
			return new ResponseEntity<Room>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Room>(newRoom, HttpStatus.CREATED);
	}
	
	@RequestMapping(
			value = "/roomsSearch/{hotelId}/{arriveDateTS}/{departureDateTS}/{twoBed}/{threeBed}/{fourBed}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Room>> getRoomSearch(@PathVariable String hotelId, @PathVariable Long arriveDateTS, @PathVariable Long departureDateTS, @PathVariable String twoBed, 
			@PathVariable String threeBed, @PathVariable String fourBed){
		int id = Integer.parseInt(hotelId);
		boolean twoBeds = (twoBed.equals("true")) ? true: false;
		boolean threeBeds = (threeBed.equals("true")) ? true: false;
		boolean fourBeds = (fourBed.equals("true")) ? true: false;
		Collection<Room> rooms = roomService.getRoomsSearch(id, arriveDateTS, departureDateTS, twoBeds, threeBeds, fourBeds);
		
		return new ResponseEntity<Collection<Room>>(rooms, HttpStatus.OK);
	}
	
	@RequestMapping(
			value = "/allRoomsSearch/{hotelName}/{destination}/{start}/{end}/{twoBed}/{threeBed}/{fourBed}/{minPrice}/{maxPrice}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Room>> getAllRoomSearch(@PathVariable String hotelName, @PathVariable String destination, @PathVariable Long start, @PathVariable Long end, @PathVariable String twoBed, 
			@PathVariable String threeBed, @PathVariable String fourBed, @PathVariable Double minPrice, @PathVariable Double maxPrice){
		boolean twoBeds = (twoBed.equals("true")) ? true: false;
		boolean threeBeds = (threeBed.equals("true")) ? true: false;
		boolean fourBeds = (fourBed.equals("true")) ? true: false;
		Collection<Room> rooms = roomService.getAllRoomsSearch(hotelName, destination, start, end, twoBeds, threeBeds, fourBeds, minPrice, maxPrice);
		
		return new ResponseEntity<Collection<Room>>(rooms, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/unreservedRooms",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Room>> getUnreservedRooms(){
		Collection<Room> rooms = roomService.getUnreservedRooms();
		return new ResponseEntity<Collection<Room>>(rooms, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/removeRoom/{id}",
			method = RequestMethod.DELETE)
	public ResponseEntity<?> removeRoom(@PathVariable Integer id){
		roomService.removeRoom(id);
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/room/{id}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Room> getSelectedRoom(@PathVariable Integer id){
		Room room = roomService.getRoom(id);
		return new ResponseEntity<Room>(room, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/changeRoom",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Room> changeRoom(@RequestBody Room room) {
		Room newRoom = roomService.changeRoom(room);
		if(newRoom == null) {
			return new ResponseEntity<Room>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Room>(newRoom, HttpStatus.CREATED);
	}
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@RequestMapping(value = "/getGradeForRoom/{id}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getUserGrade(@PathVariable Integer id) {
		Integer grade = roomService.getGrade(id);
		return new ResponseEntity<Integer>(grade, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@RequestMapping(value = "/setGradeForRoom/{id}/{grade}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> setUserGrade(@PathVariable Integer id, @PathVariable Integer grade) {
		roomService.setGrade(id, grade);
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@RequestMapping(value = "/getMediumGradeForRoom/{id}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getGrade(@PathVariable Integer id) {
		Integer grade = roomService.getIntermediateGrade(id);
		return new ResponseEntity<Integer>(grade, HttpStatus.OK);
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/allRooms",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Room>> getAllRooms(){
		Collection<Room> rooms = roomService.getAllHotelRooms();
		return new ResponseEntity<Collection<Room>>(rooms, HttpStatus.OK);
	}
	
}
