package org.example.cardcollectorproject.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

// Mark the class as serializable by Jackson
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @JsonProperty("id") // Explicitly tell Jackson to serialize/deserialize this field
    private String id;

    @JsonProperty("username") // Same for all fields
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("passwordHash")
    private String passwordHash; // Hashed password stored in the database

    @JsonProperty("salt")
    private String salt; // Salt for hashing the password

    @JsonProperty("userId")
    private String userId;

    // Constructor
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.salt = generateSalt();
        this.passwordHash = hashPassword(password, this.salt);
    }

    // Empty constructor is necessary for deserialization
    public User() {}

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean verifyPassword(String password) {
        String hashedInput = hashPassword(password, this.salt);
        return hashedInput.equals(this.passwordHash);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}