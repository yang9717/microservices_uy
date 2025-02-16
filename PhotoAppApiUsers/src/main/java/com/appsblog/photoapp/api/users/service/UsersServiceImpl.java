package com.appsblog.photoapp.api.users.service;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appsblog.photoapp.api.users.data.AlbumsServiceClient;
import com.appsblog.photoapp.api.users.data.UserEntity;
import com.appsblog.photoapp.api.users.data.UsersRepository;
import com.appsblog.photoapp.api.users.shared.UserDto;
import com.appsblog.photoapp.api.users.ui.model.AlbumResponseModel;


@Service
public class UsersServiceImpl implements UsersService {
	
	// Database access layer
	UsersRepository usersRepository;
	
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	AlbumsServiceClient albumsServiceClient;
	
	Environment env;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public UsersServiceImpl(
			UsersRepository usersRepository, 
			BCryptPasswordEncoder bCryptPasswordEncoder, 
			AlbumsServiceClient albumsServiceClient,
			Environment env
			) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.albumsServiceClient=albumsServiceClient;
		this.env=env;
	}

	@Override
	public UserDto createUser(UserDto userDetails) {

		userDetails.setUserId(UUID.randomUUID().toString());

		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

		ModelMapper modelMapper = new ModelMapper();

		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);
		
		usersRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

		return returnValue;
	}
		
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Find the user in the database
		UserEntity userEntity = usersRepository.findByEmail(username);
		
		if (userEntity == null) throw new UsernameNotFoundException(username);
		
		return new User(
				userEntity.getEmail(), 
				userEntity.getEncryptedPassword(), 
				true, 
				true, 
				true, 
				true, 
				new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		
		// Find the user in the database
		UserEntity userEntity = usersRepository.findByEmail(email);
		
		if (userEntity == null) throw new UsernameNotFoundException(email);
		return new ModelMapper().map(userEntity, UserDto.class);
		
	}

	@Override
	public UserDto getUserById(String userId) {
		UserEntity userEntity = usersRepository.findByUserId(userId);
		
		if (userEntity == null) throw new UsernameNotFoundException("User not found");
		
		ModelMapper modelMapper = new ModelMapper();
		
		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);
		
		List<AlbumResponseModel> albumsList = albumsServiceClient.getAlbums(userId);
		
		returnValue.setAlbums(albumsList);
		
		return returnValue;
	}

}
