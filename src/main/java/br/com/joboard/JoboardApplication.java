package br.com.joboard;

import br.com.joboard.configuracao.SupabaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SupabaseProperties.class)
public class JoboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(JoboardApplication.class, args);
	}

}
