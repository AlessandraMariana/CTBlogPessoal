package com.generation.blogpessoal.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
	
	private static final String SECRET = "546A576E5A7234753778214125442A472D4B6150645367566B59703273357638";
	
	private Key getSignKey() {
		
		byte[] KeyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(KeyBytes);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey (getSignKey()).build()
				.parseClaimsJws(token).getBody();
	}
	
	public<T> T extractClaim (String token, Function <Claims,T> claimsResolver){
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
		
	}
	
	public String extractUsername (String token) {
		return extractClaim (token, Claims::getSubject);
	}
	
	public Date extractExpiration (String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Boolean isTokenExpired (String token) {
		return extractExpiration(token).before(new Date());
	}
	
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	private String createToken (Map <String, Object> claims, String userName) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(userName)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 *60 ))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
	}
	
	public String generateToken(String userName) {
		Map <String, Object> claims = new HashMap<>();
		return createToken(claims, userName);
	}
}