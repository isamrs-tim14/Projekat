package com.isamrs.tim14.rest;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.isamrs.tim14.dao.RegisteredUserDAO;
import com.isamrs.tim14.model.RegisteredUser;

@RestController
@RequestMapping("/api")
public class RegisteredUserRest {
	
	private RegisteredUserDAO registeredUserDAO;
	
	@Autowired
	public RegisteredUserRest(RegisteredUserDAO registeredUserDAO) {
		this.registeredUserDAO = registeredUserDAO;
	}

	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@GetMapping("/registeredUser/search/{input}")
	public ResponseEntity<List<RegisteredUser>> searchUsers(@PathVariable String input) {
		return registeredUserDAO.searchUsers(input);
	}
	
	@PreAuthorize("hasRole('ROLE_REGISTEREDUSER')")
	@GetMapping("/registeredUser/getFriendRequests")
	public ResponseEntity<Set<RegisteredUser>> getFriendRequests() {
		return registeredUserDAO.getFriendRequests();
	}
	
}
