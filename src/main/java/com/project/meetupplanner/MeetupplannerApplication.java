package com.project.meetupplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@EntityScan(
	basePackageClasses = { MeetupplannerApplication.class, Jsr310JpaConverters.class }
)

@SpringBootApplication
public class MeetupplannerApplication {

	public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
