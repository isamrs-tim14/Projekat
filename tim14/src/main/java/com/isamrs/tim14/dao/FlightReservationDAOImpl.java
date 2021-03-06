package com.isamrs.tim14.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import com.isamrs.tim14.dto.GraphsDTO;
import com.isamrs.tim14.model.AirlineAdmin;
import com.isamrs.tim14.model.Flight;
import com.isamrs.tim14.model.FlightReservation;
import com.isamrs.tim14.model.RegisteredUser;
import com.isamrs.tim14.model.Seat;
import com.isamrs.tim14.service.EmailService;

@Repository
public class FlightReservationDAOImpl implements FlightReservationDAO {

	private EntityManager entityManager;
	
	@Autowired
	private EmailService mailService;
	
	@Autowired
	public FlightReservationDAOImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	@Override
	@Transactional
	public ResponseEntity<String> saveReservation(List<FlightReservation> reservations) {
		RegisteredUser user = (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		reservations.get(0).setUser(user);
		reservations.get(0).setPassportNumber("123456789");
		
		for(FlightReservation reservation : reservations) {
			Seat managedSeat = entityManager.find(Seat.class, reservation.getSeat().getId());
			managedSeat.setBusy(true);
			
			entityManager.persist(reservation);
			
			if(reservation.getUser().getId() != user.getId() && reservation.getUser() != null)
				try {
					String message = "User " + reservations.get(0).getUser().getEmail() + " inviting you to the flight " + reservation.getFlight().getFrom().getDestination().getName() + " --> " + reservation.getFlight().getTo().getDestination().getName() + ".\n\nAccept invitation: http://localhost:5000/api/flightReservation/acceptInvitation/" + reservation.getId() + ".\nDecline invitation: http://localhost:5000/api/flightReservation/declineInvitation/" + reservation.getId();
					mailService.sendNotificaitionAsync(reservation.getUser(), "Invitation on flight", message);
				} catch (MailException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return new ResponseEntity("Reservation successfully saved.", HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<String> makeQuickReservation(List<FlightReservation> reservations) {
		for(FlightReservation reservation : reservations) {
			Seat managedSeat = entityManager.find(Seat.class, reservation.getSeat().getId());
			managedSeat.setBusy(true);
			
			entityManager.persist(reservation);
		}
		
		return new ResponseEntity("Quick reservation successfully saved.", HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<List<FlightReservation>> getQuickTickets(Integer id) {
		Query query = entityManager.createQuery("SELECT fr FROM Flight f, FlightReservation fr WHERE f.airline.id = :airline_id AND f.id = fr.flight.id AND fr.user IS NULL AND fr.passenger IS NULL");
		query.setParameter("airline_id", id);
		
		List<FlightReservation> result = query.getResultList();
		
		for(FlightReservation reservation : result) {
		}
		
		return new ResponseEntity(result, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<String> buyQuickTicket(FlightReservation flightReservation) {
		FlightReservation reservation = entityManager.find(FlightReservation.class, flightReservation.getId());
		RegisteredUser user = (RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		reservation.setPassportNumber("123456789");
		reservation.setUser(user);
		reservation.setDateOfPurchase(new Timestamp(new Date().getTime()));
		reservation.setRoomReservation(flightReservation.getRoomReservation());
		reservation.setVehicleReservation(flightReservation.getVehicleReservation());
		
		return new ResponseEntity("Quick reservation successfully completed.", HttpStatus.OK);
	}

	@Override
	@Transactional
	public Collection<Flight> getFlightHistory() {
		ArrayList<Flight> allFlights= new ArrayList<Flight>();
		RegisteredUser u = ((RegisteredUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		for (FlightReservation fr : u.getFlightReservations()) {
			//if (vr.getEnd().after(new Date(System.currentTimeMillis()))) {
				if(!allFlights.contains(fr.getFlight())) {
						allFlights.add(fr.getFlight());
				}
			//}
		}
		return allFlights;
	}

	@Override
	@Transactional
	public ResponseEntity<FlightReservation> getQuickReservation(Integer reservationID) {
		FlightReservation reservation = entityManager.find(FlightReservation.class, reservationID);
		
		return new ResponseEntity<FlightReservation>(reservation, HttpStatus.OK);
	}

	@Override
	@Transactional
	public ResponseEntity<String> declineInvitation(Integer reservationID) {
		FlightReservation managedReservation = entityManager.find(FlightReservation.class, reservationID);
		
		if(managedReservation == null)
			return new ResponseEntity<String>("Invitation doesn't exists.", HttpStatus.NOT_ACCEPTABLE);
		
		entityManager.remove(managedReservation);
		
		return new ResponseEntity<String>("Invitation declined.", HttpStatus.OK);
	}
	
	@Override
	@Transactional
	public GraphsDTO getFlightsDaily() throws ParseException {
		GraphsDTO graph = new GraphsDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date start = null;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Now use today date.
		c.add(Calendar.DATE, -13);
		start = sdf.parse(sdf.format(c.getTime()));
		AirlineAdmin admin = (AirlineAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Collection<Flight> flights = admin.getAirline().getFlights();
		Query query = entityManager.createQuery("SELECT fr FROM FlightReservation fr");
		List<FlightReservation> reservations = query.getResultList();
		for(int i = 0; i<14; i++) {
			start = sdf.parse(sdf.format(c.getTime()));
			String dateX = sdf.format(start);
			graph.getX().add(dateX);
			int sum = 0;
			for(Flight f: flights) {
				for(FlightReservation fr : reservations) {
					if(fr.getFlight().getId()==f.getId()) {
						if(sdf.parse(sdf.format(fr.getFlight().getDepartureDate())).equals(start)) {
							sum++;
						}
					}
				}
			}
			graph.getY().add(sum);
			c.add(Calendar.DATE, 1);
		}	
		return graph;
	}

	@Override
	@Transactional
	public GraphsDTO getFlightsWeekly() throws ParseException {
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
		AirlineAdmin admin = (AirlineAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Collection<Flight> flights = admin.getAirline().getFlights();
		Query query = entityManager.createQuery("SELECT fr FROM FlightReservation fr");
		List<FlightReservation> reservations = query.getResultList();
		for(int i = 0; i<12; i++) {
			start = end;
			c.add(Calendar.DATE, 7);
			end = sdf.parse(sdf.format(c.getTime()));
			int sum = 0;
			for(Flight f: flights) {
				for(FlightReservation fr : reservations) {
					if(fr.getFlight().getId()==f.getId()) {
						if(sdf.parse(sdf.format(fr.getFlight().getDepartureDate())).before(end) && sdf.parse(sdf.format(fr.getFlight().getDepartureDate())).after(start) ||
						sdf.parse(sdf.format(fr.getFlight().getDepartureDate())).equals(end)) {
							sum++;
						}
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

	@Override
	@Transactional
	public GraphsDTO getFlightsMonthly() throws ParseException {
		GraphsDTO graph = new GraphsDTO();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
		Date date = null;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -11);
		date = sdf.parse(sdf.format(c.getTime()));
		AirlineAdmin admin = (AirlineAdmin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Collection<Flight> flights = admin.getAirline().getFlights();
		Query query = entityManager.createQuery("SELECT fr FROM FlightReservation fr");
		List<FlightReservation> reservations = query.getResultList();
		for(int i = 0; i<12; i++) {
			String dateX = sdf.format(c.getTime());
			date = sdf.parse(sdf.format(c.getTime()));
			int sum = 0;
			for(Flight f: flights) {
				for(FlightReservation fr : reservations) {
					if(fr.getFlight().getId()==f.getId()) {
						if(sdf.parse(sdf.format(fr.getFlight().getDepartureDate())).equals(date)) {
						sum++;
						}
					}
				}
			}
			graph.getX().add(dateX);
			graph.getY().add(sum);
			c.add(Calendar.MONTH, 1);
		}
		return graph;
	}
}
