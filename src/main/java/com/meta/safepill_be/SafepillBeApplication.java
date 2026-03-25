package com.meta.safepill_be;

import com.meta.safepill_be.medicine.service.MedicineService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SafepillBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafepillBeApplication.class, args);
    }
    @Bean
    public CommandLineRunner run(MedicineService medicineService) {
        return args -> {
            medicineService.fetchMedicineDataFromApi();
        };
    }
}