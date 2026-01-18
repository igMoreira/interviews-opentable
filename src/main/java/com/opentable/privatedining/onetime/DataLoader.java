package com.opentable.privatedining.onetime;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Component for loading initial data into the database on application startup.
 */
@Component
public class DataLoader implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs a new DataLoader with the required MongoTemplate.
     *
     * @param mongoTemplate the MongoDB template for database operations
     */
    public DataLoader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Runs on application startup to load initial data from init-db.yml file.
     * Skips loading if restaurant or reservation collections already exist.
     *
     * @param args application arguments
     * @throws Exception if there is an error loading the data
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        var yaml = new ClassPathResource("init-db.yml");

        if(mongoTemplate.collectionExists(Restaurant.class)
                || mongoTemplate.collectionExists(Reservation.class)) {
            return;
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(df));

        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.registerModule(module);
        try (var inputStream = yaml.getInputStream()) {
            var data = yamlMapper.readValue(inputStream, Data.class);

            mongoTemplate.dropCollection(Restaurant.class);
            mongoTemplate.dropCollection(Reservation.class);

            mongoTemplate.insertAll(data.getRestaurants());
            mongoTemplate.insertAll(data.getReservations());
        }
    }
}
