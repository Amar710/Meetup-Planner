package com.project.meetupplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@EntityScan(
	basePackageClasses = { MeetupplannerApplication.class, Jsr310JpaConverters.class }
)

@SpringBootApplication
public class MeetupplannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetupplannerApplication.class, args);
	}

}
