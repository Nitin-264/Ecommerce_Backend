package com.trigon.config;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidator extends OncePerRequestFilter{

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
			String jwt = request.getHeader(JwtConstant.JWT_HEADER);
			if (jwt != null && jwt.startsWith("Bearer ")) {

				jwt = jwt.split(" ")[1].trim();
			     
			try {
				SecretKey key=Keys.hmacShaKeyFor(Base64.getDecoder().decode(JwtConstant.SECRET_KEY));
				Claims claims=Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
				String email=String.valueOf(claims.get("email"));
				String authorities=String.valueOf(claims.get("authorities"));
				List<GrantedAuthority> auth=AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
				Authentication authentication=new UsernamePasswordAuthenticationToken(email,null, auth);
//				System.out.println("FULL AUTH HEADER: " + request.getHeader("Authorization"));
//				System.out.println("TOKEN BEFORE PARSE: " + jwt);

				SecurityContextHolder.getContext().setAuthentication(authentication);
 			}catch(Exception e) {
 				e.printStackTrace();
 				throw new BadCredentialsException("Invalid token is there...");
			}
		}
		filterChain.doFilter(request, response);
	}

}
