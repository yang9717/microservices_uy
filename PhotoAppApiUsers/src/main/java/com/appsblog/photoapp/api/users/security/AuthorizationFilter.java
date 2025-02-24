package com.appsblog.photoapp.api.users.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.appsblog.JwtAuthorities.JwtClaimsParser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {
	
	private Environment env;
	
	public AuthorizationFilter(
			AuthenticationManager authenticationManager,
			Environment env) {
		super(authenticationManager);
		this.env = env;
	}
	
	//	runs for every incoming request
	@Override
	protected void doFilterInternal(
			HttpServletRequest req,
			HttpServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		String authorizationHeader = req.getHeader(env.getProperty("authorization.token.header.name"));
		if (authorizationHeader == null 
				|| !authorizationHeader.startsWith(
						env.getProperty("authorization.token.header.name.prefix")
						)
				) {
			chain.doFilter(req, res);
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		chain.doFilter(req, res);
	}
	
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest req) {
		
		String authorizationHeader = req.getHeader(env.getProperty("authorization.token.header.name"));
		
		if (authorizationHeader == null) {
			return null;
		}
		
		String token = authorizationHeader.substring(7);
		String tokenSecret = env.getProperty("token.secret");
		
		if (tokenSecret == null) return null;
		
		JwtClaimsParser jwtClaimsParser = new JwtClaimsParser(token, tokenSecret);
		
		String userId = jwtClaimsParser.getJwtSubject();
		
		if (userId == null) return null;
		
		Collection<? extends GrantedAuthority> userAuthotiries = jwtClaimsParser.getUserAuthorities();
		
		return new UsernamePasswordAuthenticationToken(userId, null, userAuthotiries);
	}
}
