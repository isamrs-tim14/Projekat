package com.isamrs.tim14.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.isamrs.tim14.dto.GraphsDTO;
import com.isamrs.tim14.model.FlightReservation;
import com.isamrs.tim14.model.Hotel;
import com.isamrs.tim14.model.HotelAdmin;
import com.isamrs.tim14.model.RegisteredUser;
import com.isamrs.tim14.model.Room;
import com.isamrs.tim14.model.RoomReservation;
import com.isamrs.tim14.model.User;
import com.isamrs.tim14.repository.IFlightReservationRepository;
import com.isamrs.tim14.repository.IHotelRepository;
import com.isamrs.tim14.repository.IRoomRepository;
import com.isamrs.tim14.repository.IRoomReservationRepository;

@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class RoomReservationService {

	@Autowired
	IRoomReservationRepository roomReservationRepository;
	
	@Autowired
	IHotelRepository hotelRepository;
	
	@Autowired
	IRoomRepository roomRepository;
	
	@Autowired
	IFlightReservationRepository flightReservationRepository;
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = PessimisticLockException.class)
	public RoomReservation save(RoomReservation roomReservation) {
		Integer[] rooms = new Integer[roomReservation.getRooms().size()];
		
		Iterator<Room> iterator = roomReservation.getRooms().iterator();
		
		int i = 0;
		while(iterator.hasNext()) {
			Room room = iterator.next();
			
			rooms[i] = room.getId();
			i++;
		}
		
		List<Room> roomsInReservations = roomRepository.findAllRooms(rooms);
		
		Hotel managedHotel = hotelRepository.getHotelById(roomReservation.getHotel().getId());
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if(user instanceof RegisteredUser) {
			RegisteredUser registered = (RegisteredUser) user;
			roomReservation.setQuick(false);
			roomReservation.setRegisteredUser(registered);
		}else {
			roomReservation.setQuick(true);
			roomReservation.setRegisteredUser(null);
		}
		
		for(Room room : roomReservation.getRooms()) {
			if(roomReservationRepository.getReservation(room.getId(), roomReservation.getStart(), roomReservation.getEnd()) != null) {
				throw new PessimisticLockException();
			}
		}
		
		roomReservationRepository.save(roomReservation);
		managedHotel.getReservations().add(roomReservation);
		
		if(user instanceof RegisteredUser) {
			RegisteredUser registered = (RegisteredUser) user;
			registered.getRoomReservations().add(roomReservation);
		}
		
		for(Room r : roomsInReservations) {
			r.getReservations().add(roomReservation);
		}
		
		return roomReservation;
	}
	
	public ArrayList<RoomReservation> getQuickRoomReservations(String hotelID) {
		Hotel managedHotel = hotelRepository.getHotelById(Integer.parseInt(hotelID));
		ArrayList<RoomReservation> result = new ArrayList<RoomReservation>();
		for(RoomReservation res : managedHotel.getReservations()) {
			if(res.getRegisteredUser() == null) {
				result.add(res);
			}
		}
		return result;
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false, rollbackFor = OptimisticLockException.class)
	public RoomReservation saveQuick(String roomReservationID) {
		RoomReservation managedReservation = roomReservationRepository.findOneById(Integer.parseInt(roomReservationID));
		RegisteredUser user = (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		managedReservation.setRegisteredUser(user);
		return managedReservation;
	}
	
	public Collection<Room> getRoomsHistory() {
		ArrayList<Room> allRooms = new ArrayList<Room>();
		RegisteredUser u = ((RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		for (RoomReservation rr : u.getRoomReservations()) {
			if (rr.getEnd().after(new Date(System.currentTimeMillis()))) {
				for (Room r: rr.getRooms()) {
					if (!allRooms.contains(r))
						allRooms.add(r);
				}
			}
		}
		return allRooms;
	}
	
	public RoomReservation getOneQuickReservation(Integer id) {
		RoomReservation reservation = roomReservationRepository.getReservationById(id);
		return reservation;
	}
	
	public GraphsDTO getRoomsDaily() throws ParseException {
		GraphsDTO graph = new GraphsDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date start = null;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); 
		c.add(Calendar.DATE, -13);
		start = sdf.parse(sdf.format(c.getTime()));
		HotelAdmin admin = (HotelAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Set<Room> rooms = admin.getHotel().getRooms();
		for(int i = 0; i< 14; i++) {
			start = sdf.parse(sdf.format(c.getTime()));
			String dateX = sdf.format(start);
			graph.getX().add(dateX);
			int sum = 0;
			for(Room r: rooms) {
				for(RoomReservation rr : r.getReservations()) {
					if(sdf.parse(sdf.format(rr.getStart())).equals(start)) {
						sum++;
					}
				}
			}
			graph.getY().add(sum);
			c.add(Calendar.DATE, 1);
		}	
		return graph;
	}
	
	
	public GraphsDTO getRoomsWeekly() throws ParseException {
		GraphsDTO graph = new GraphsDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM");
		Date end = null;
		Date start = null;
		Calendar c = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.WEEK_OF_YEAR, -13);
		start = sdf.parse(sdf.format(c.getTime()));
		c.add(Calendar.WEEK_OF_YEAR, 1);
		end = sdf.parse(sdf.format(c.getTime()));
		HotelAdmin admin = (HotelAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Set<Room> rooms = admin.getHotel().getRooms();
		for(int i = 0; i<12; i++) {
			start = end;
			c.add(Calendar.DATE, 7);
			end = sdf.parse(sdf.format(c.getTime()));
			int sum = 0;
			for(Room r: rooms) {
				for(RoomReservation rr : r.getReservations()) {
					if(sdf.parse(sdf.format(rr.getStart())).before(end) && sdf.parse(sdf.format(rr.getStart())).after(start) || sdf.parse(sdf.format(rr.getStart())).equals(end)) {
						sum++;
					}
				}
			}
			c2.setTime(start);
			c2.add(Calendar.DATE, 1);
			start = sdf.parse(sdf.format(c2.getTime()));
			String dateX = sdf2.format(start)+"-"+sdf2.format(end);
			graph.getX().add(dateX);
			graph.getY().add(sum);
		}
		return graph;
	}
	
	
	public GraphsDTO getRoomsMonthly() throws ParseException {
		GraphsDTO graph = new GraphsDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
		Date date = null;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -11);
		date = sdf.parse(sdf.format(c.getTime()));
		HotelAdmin admin = (HotelAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Set<Room> rooms = admin.getHotel().getRooms();
		for(int i = 0; i<12; i++) {
			String dateX = sdf.format(c.getTime());
			date = sdf.parse(sdf.format(c.getTime()));
			int sum = 0;
			for(Room r: rooms) {
				for(RoomReservation rr : r.getReservations()) {
					if(sdf.parse(sdf.format(rr.getStart())).equals(date)) {
						sum++;
					}
				}
			}
			graph.getX().add(dateX);
			graph.getY().add(sum);
			c.add(Calendar.MONTH, 1);
		}
		return graph;
	}
	
	public Collection<RoomReservation> getUserRoomReservations() {
		RegisteredUser u = ((RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return u.getRoomReservations();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void cancelRoomReservation(String reservationID) {
		RoomReservation res = roomReservationRepository.getOne(Integer.parseInt(reservationID));
		
		Iterator<RoomReservation> iterator = null;
		for (Room r : res.getRooms()) {
			Room room = roomRepository.getOne(r.getId());
			
			iterator = room.getReservations().iterator();
			while(iterator.hasNext()) {
				RoomReservation reservation = iterator.next();
				
				if(reservation.getId() == Integer.parseInt(reservationID)) {
					iterator.remove();
					break;
				}
			}
		}
		
		List<FlightReservation> flightReservations = flightReservationRepository.findReservationsByRoomReservation(Integer.parseInt(reservationID));
		
		for(FlightReservation flightReservation : flightReservations)
			if(flightReservation.getRoomReservation().getId() == res.getId())
				flightReservation.setRoomReservation(null);
		
		if(!res.getQuick())
			roomReservationRepository.delete(res);
		else
			res.setRegisteredUser(null);
	}
	
}
