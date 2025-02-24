package com.appsblog.JwtAuthorities;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtClaimsParser {
	
	Jwt<?, ?> jwtObject;
	
	public JwtClaimsParser(String jwt, String secretToken) {
		this.jwtObject = parseJwt(jwt, secretToken);
	}
	
	Jwt<?, ?> parseJwt(String jwtString, String secretToken) {
		
		byte[] secretKeyBytes = Base64.getEncoder().encode(secretToken.getBytes());
		
		SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);
		
		JwtParser parser = Jwts.parser().verifyWith(key).build();
		
		return parser.parse(jwtString);
	}
	
	public Collection<? extends GrantedAuthority> getUserAuthorities() {
		
		// Get the decoded content of the JWT
		Claims decoded = (Claims) jwtObject.getPayload();
		
		// Look for scope field in the JWT and expects it to be a list
		Collection<Map<String, String>> scopes = decoded.get("scope", List.class);
		
		return scopes.stream()
				.map(scopeMap -> new SimpleGrantedAuthority(scopeMap.get("authority")))
				.collect(Collectors.toList());
	}
	
	public String getJwtSubject() {
		return ((Claims)jwtObject.getPayload()).getSubject();
	}
}
