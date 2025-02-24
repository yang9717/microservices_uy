package com.appsblog.photoapp.api.gateway;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.HttpHeaders;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
	
	@Autowired
	Environment env;
	
	public AuthorizationHeaderFilter() {
        super(Config.class);
    }
	
	public static class Config {
		// put configuration properties here
	}
	
	@Override
	public GatewayFilter apply(Config config) {

		return (exchange, chain) -> {
			
			ServerHttpRequest request = exchange.getRequest();
			
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION) ) {
				return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
			}
			
			String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			
			if (!authorizationHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }
			
			String jwt = authorizationHeader.substring(7);
			
			if (!isJwtValid(jwt)) {
				return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
			}
			
			return chain.filter(exchange);
		};
	}
	
	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		
		// Include error message in response body of Spring Cloud Gateway
		DataBufferFactory bufferFactory = response.bufferFactory();
		DataBuffer dataBuffer = bufferFactory.wrap(err.getBytes());
		
		return response.writeWith(Mono.just(dataBuffer));
	}
	
	private boolean isJwtValid (String jwt) {
		
		boolean isValid = true;
		String subject = null;
		
		try {
			String tokenSecret = env.getProperty("token.secret");
			byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
			
			SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);
			JwtParser parser = Jwts.parser().verifyWith(key).build();
			
			Claims claims = parser.parseSignedClaims(jwt).getPayload();
	        subject = claims.getSubject();

			
		} catch (Exception ex) {
            isValid = false;
        }
		
		if (subject == null || subject.isEmpty()) {
			isValid = false;
		}

		return isValid;
	}
}
