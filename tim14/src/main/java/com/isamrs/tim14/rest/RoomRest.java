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

@RestController
@RequestMapping("/api")
public class RoomRest {

	private RoomDAO roomDAO;
	
	@Autowired
	public RoomRest(RoomDAO roomDAO) {
		this.roomDAO = roomDAO;
	}
	
	@PreAuthorize("hasRole('ROLE_HOTELADMIN')")
	@RequestMapping(
			value = "/rooms",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Room> saveRoom(@RequestBody Room room) {
		Room newRoom = roomDAO.save(room);

		if(newRoom == null) {
			return new ResponseEntity<Room>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Room>(newRoom, HttpStatus.CREATED);
	}
	
	@RequestMapping(
			value = "/roomsSearch/{hotelId}/{arriveDate}/{dayNum}/{twoBed}/{threeBed}/{fourBed}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Room>> getRoomSearch(@PathVariable String hotelId, @PathVariable String arriveDate, @PathVariable String dayNum, @PathVariable String twoBed, 
			@PathVariable String threeBed, @PathVariable String fourBed){
		int id = Integer.parseInt(hotelId);
		int numberOfDays = Integer.parseInt(dayNum);
		boolean twoBeds = (twoBed.equals("true")) ? true: false;
		boolean threeBeds = (threeBed.equals("true")) ? true: false;
		boolean fourBeds = (fourBed.equals("true")) ? true: false;
		Collection<Room> rooms = roomDAO.getRoomsSearch(id, arriveDate, numberOfDays, twoBeds, threeBeds, fourBeds);
		
		return new ResponseEntity<Collection<Room>>(rooms, HttpStatus.OK);
	}
	
}
