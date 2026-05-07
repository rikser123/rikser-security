package rikser123.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
  ReactiveManagementWebSecurityAutoConfiguration.class
})
public class RikserSecurityApplication {
  public static void main(String[] args) {
    SpringApplication.run(RikserSecurityApplication.class, args);
  }
}
