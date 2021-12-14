package tourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Autoconfigure and start the application.
*/
@SpringBootApplication
@EnableFeignClients("tourGuide")
public class Application {

    /** Application entry point.
     */
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

}
