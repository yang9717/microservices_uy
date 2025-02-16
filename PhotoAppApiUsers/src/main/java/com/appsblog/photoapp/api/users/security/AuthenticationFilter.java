package com.appsblog.photoapp.api.users.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.appsblog.photoapp.api.users.service.UsersService;
import com.appsblog.photoapp.api.users.shared.UserDto;
import com.appsblog.photoapp.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private UsersService usersService;
	private Environment env;
	
	public AuthenticationFilter(
			UsersService usersService, 
			Environment env, 
			AuthenticationManager authenticationManager
			) {
		super(authenticationManager);
		this.usersService = usersService;
		this.env = env;
	}

	@Override
	public Authentication attemptAuthentication(
			HttpServletRequest req, 
			HttpServletResponse res
			) throws AuthenticationException {
		try {
			
			// Read credentials
			LoginRequestModel creds = new ObjectMapper().readValue(req.getInputStream(), LoginRequestModel.class);
			
			UsernamePasswordAuthenticationToken userToken = 
					new UsernamePasswordAuthenticationToken(
							creds.getEmail(), 
							creds.getPassword(), 
							new ArrayList<>()
							);
			
			// Return the result of credentials verification
			return getAuthenticationManager().authenticate(userToken);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	// If authenticated successfully, generate Access Token
	@Override
	protected void successfulAuthentication(
			HttpServletRequest req, 
			HttpServletResponse res, 
			FilterChain chain, 
			Authentication auth
			)
			throws IOException, ServletException {
		// Cast Principal to User class to call getUsername method
		String userName = ((User)auth.getPrincipal()).getUsername();
		
		// Fetch full user details
		UserDto userDetails = usersService.getUserDetailsByEmail(userName);
		
		String tokenSecret = env.getProperty("token.secret");
		
		// Convert to raw bytes, then encode in Base64
		byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
		
		// Create a cryptographic key for JWT signing
		SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
		
		String expTime = env.getProperty("token.expiration_time");
		Instant now = Instant.now();

		String token = Jwts.builder()
			.subject(userDetails.getUserId())
			.expiration(Date.from(now.plusMillis(
					Long.parseLong(expTime))))
			.issuedAt(Date.from(now))
			.signWith(secretKey)
			.compact();
		
		res.addHeader("token", token);
		res.addHeader("userId", userDetails.getUserId());
		
	}

}
