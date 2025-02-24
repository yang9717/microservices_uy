package com.appsblog.photoapp.api.albums.security;

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
    		AuthenticationManager authManager, 
    		Environment env) {
        super(authManager);
        this.env = env;
    }
    
    @Override
    protected void doFilterInternal(
    		HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(env.getProperty("authorization.token.header.name"));

        if (header == null || 
        		!header.startsWith(env.getProperty("authorization.token.header.prefix"))) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
    
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

        String authorizationHeader = request.getHeader(
        		env.getProperty("authorization.token.header.name"));

        if (authorizationHeader == null) return null;

        String token = authorizationHeader.substring(7);
		String tokenSecret = env.getProperty("token.secret");
		
		if (tokenSecret == null) return null;
		
		JwtClaimsParser jwtClaimsParser = new JwtClaimsParser(token, tokenSecret);
		
        String subject = jwtClaimsParser.getJwtSubject();
        Collection<? extends GrantedAuthority> userAuthotiries = jwtClaimsParser.getUserAuthorities();
        
        return new UsernamePasswordAuthenticationToken(subject, null, userAuthotiries);

    }
}
