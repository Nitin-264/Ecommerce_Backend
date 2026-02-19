package com.trigon.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class User {

	
	public User(Long id ,String password, String firstName, String lastName, String email, String role, String phone,
			List<Address> address, List<paymentInformation> paymentinfo, List<Rating> rating, List<Review> review) {
		super();
		this.id = id;
		FirstName = firstName;
		LastName = lastName;
		this.email = email;
		this.password=password;
		this.role = role;
		this.phone = phone;
		this.address = address;
		this.paymentinfo = paymentinfo;
		this.rating = rating;
		this.review = review;
	}

	public User() {
		// TODO Auto-generated constructor stub
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String FirstName;
	private String LastName;
	@Column(name="email")
	private String email;
	private String password;
	private String role;
	private String phone;
	
	@OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
	private List<Address> address=new ArrayList();
	
	@Embedded
	@ElementCollection
	@CollectionTable(name="payment_Information",joinColumns = @JoinColumn(name="user_id"))
	private List<paymentInformation> paymentinfo=new ArrayList();
	
	@OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
	@JsonIgnore
	private List<Rating> rating=new ArrayList();
	
	@OneToMany(mappedBy = "user" , cascade=CascadeType.ALL)
	@JsonIgnore
	private List<Review> review=new ArrayList();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return FirstName;
	}

	public void setFirstName(String firstName) {
		FirstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<Address> getAddress() {
		return address;
	}

	public void setAddress(List<Address> address) {
		this.address = address;
	}

	public List<paymentInformation> getPaymentinfo() {
		return paymentinfo;
	}

	public void setPaymentinfo(List<paymentInformation> paymentinfo) {
		this.paymentinfo = paymentinfo;
	}

	public List<Rating> getRating() {
		return rating;
	}

	public void setRating(List<Rating> rating) {
		this.rating = rating;
	}

	public List<Review> getReview() {
		return review;
	}

	public void setReview(List<Review> review) {
		this.review = review;
	}

	

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
