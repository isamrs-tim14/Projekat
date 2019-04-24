package com.isamrs.tim14.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "hotel")
public class Hotel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	
    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;
	
	@Column(name = "description")
	private String description;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hotel")
	private Set<HotelService> services;
	
	@ElementCollection(targetClass = Room.class)
	private Set<Room> rooms;
	
	@OneToMany(cascade = CascadeType.ALL, fetch= FetchType.LAZY)
	private Set<Grade> grades;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "hotel_id")
	private Set<RoomReservation> reservations;
	
	@ElementCollection(targetClass = HotelAdmin.class)
	private Set<HotelAdmin> admins;

	public Hotel() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<HotelService> getServices() {
		return services;
	}

	public void setServices(Set<HotelService> services) {
		this.services = services;
	}

	public Set<Room> getRooms() {
		return rooms;
	}

	public void setRooms(Set<Room> rooms) {
		this.rooms = rooms;
	}

	public Set<Grade> getGrades() {
		return grades;
	}

	public void setGrades(Set<Grade> grades) {
		this.grades = grades;
	}

	public Set<RoomReservation> getReservations() {
		return reservations;
	}

	public void setReservations(Set<RoomReservation> reservations) {
		this.reservations = reservations;
	}

	public Set<HotelAdmin> getAdmins() {
		return admins;
	}

	public void setAdmins(Set<HotelAdmin> admins) {
		this.admins = admins;
	}

}
