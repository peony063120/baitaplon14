package com.auction.common.exception;

public class AuthenticationException extends Exception {
    public AuthenticationException(String message){
        super(message);
    }
    public AuthenticationException(String message, Throwable cause){
        super(message, cause);
    }
    public static AuthenticationException invalidCredentials(){
        return new AuthenticationException("Invalid username or password");
    }
    public static AuthenticationException userAlreadyExists(String username) {
        return new AuthenticationException("User already exists: " +  username);
    }
    public static AuthenticationException sessionExpired(){
        return new AuthenticationException("Session has expired. Please login again");
    }
    public static AuthenticationException unauthorizedRole(String requiredRole) {
        return new AuthenticationException("Access denied. Required role: " + requiredRole);
    }
}
