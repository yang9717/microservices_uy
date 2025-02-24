package com.appsblog.photoapp.api.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.appsblog.photoapp.api.users.service.UsersService;

@EnableMethodSecurity(prePostEnabled=true)
@Configuration
@EnableWebSecurity
public class WebSecurity {

	private Environment env;
	private UsersService usersService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public WebSecurity(
			Environment env, 
			UsersService usersService, 
			BCryptPasswordEncoder bCryptPasswordEncoder
			) {
		this.env = env;
		this.usersService = usersService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}
	
	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
		
		// Get the authentication manager builder
		AuthenticationManagerBuilder authenticationManagerBuilder = 
				http.getSharedObject(AuthenticationManagerBuilder.class);
		
		// Configure the builder
		authenticationManagerBuilder.userDetailsService(usersService)
									.passwordEncoder(bCryptPasswordEncoder);
		
		// Build the manager
		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
		
		AuthenticationFilter authenticationFilter = 
				new AuthenticationFilter(usersService, env, authenticationManager);
		
		// Set the URL path for processing login requests
		authenticationFilter.setFilterProcessesUrl(env.getProperty("login.url.path"));
		
		// Configure rules
		http
			.csrf(csrf -> csrf.disable())	// Disable CSRF protection
			.authorizeHttpRequests((authz) -> authz
				.requestMatchers(new AntPathRequestMatcher("/users/**")).access(
							new WebExpressionAuthorizationManager("hasIpAddress('"+env.getProperty("gateway.ip")+"')"))
				.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
		        .requestMatchers(new AntPathRequestMatcher("/actuator/**", HttpMethod.GET.name())).permitAll())
		        .addFilter(new AuthorizationFilter(authenticationManager, env))
				.addFilter(authenticationFilter)	// For login ONLY
		        .authenticationManager(authenticationManager)
		        .sessionManagement((session) -> session
		                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		
		http.headers(headers -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));
		
		return http.build();
	}
	
}
