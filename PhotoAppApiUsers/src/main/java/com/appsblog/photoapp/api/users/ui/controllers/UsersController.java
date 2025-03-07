package com.appsblog.photoapp.api.users.ui.controllers;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appsblog.photoapp.api.users.service.UsersService;
import com.appsblog.photoapp.api.users.shared.UserDto;
import com.appsblog.photoapp.api.users.ui.model.CreateUserRequestModel;
import com.appsblog.photoapp.api.users.ui.model.CreateUserResponseModel;
import com.appsblog.photoapp.api.users.ui.model.UserResponseModel;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	UsersService usersService;
	
	@GetMapping("/status/check")
	public String status() {
		return String.format("working on port %s with token = %s", 
				env.getProperty("local.server.port"), 
				env.getProperty("token.secret"));
	}
	
	@PostMapping(
			consumes = { MediaType.APPLICATION_XML_VALUE, 
					MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_XML_VALUE, 
					MediaType.APPLICATION_JSON_VALUE }
			)
	public ResponseEntity<CreateUserResponseModel> createUser(
			@Valid @RequestBody CreateUserRequestModel userDetails
			) {
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		UserDto createdUser = usersService.createUser(userDto);
		
		CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
	}
	
	@GetMapping(
			value="/{userId}", 
			produces = { MediaType.APPLICATION_XML_VALUE, 
					MediaType.APPLICATION_JSON_VALUE }
			)
	@PreAuthorize("hasRole('ADMIN') or principal == #userId")
	public ResponseEntity<UserResponseModel> getUser(
			@PathVariable String userId, 
			@RequestHeader("Authorization") String authorization
			) {
		ModelMapper modelMapper = new ModelMapper();
		
		UserDto userDto = usersService.getUserById(userId, authorization);
		
		UserResponseModel returnValue = modelMapper.map(userDto, UserResponseModel.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(returnValue);
	}
	
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('DELETE') or principal == #userId")
	@DeleteMapping("/{userId}")
	public String deleteUser(@PathVariable String userId) {
		
		return "Deleting user with id " + userId;
	}

}
