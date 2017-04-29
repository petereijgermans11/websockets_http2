package nl.campanula.jee7showcase.websocket.server;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Person implements Serializable {
	private static final long serialVersionUID = 7697764707120016622L;

	private int age;
	private String familyName;
	private String email;
	private String firstName;

	public Person(final int age, final String voornaam, final String familyName, final String email) {
		this.age = age;
		this.firstName = voornaam;
		this.familyName = familyName;
		this.email = email;
	}

	public Person() {
		this.age = 19;
		this.firstName = "Voor";
		this.familyName = "Achter";
		this.email = "e@mail";
	}

	public int getAge() {
		return age;
	}

	public void setAge(final int age) {
		this.age = age;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(final String familyName) {
		this.familyName = familyName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

}
