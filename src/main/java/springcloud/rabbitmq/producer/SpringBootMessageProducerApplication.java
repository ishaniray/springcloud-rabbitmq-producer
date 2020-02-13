package springcloud.rabbitmq.producer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import springcloud.rabbitmq.dto.Associate;

@SpringBootApplication
@RestController
@EnableBinding(Source.class)
public class SpringBootMessageProducerApplication {
	
	@Autowired
	Source source;
	
	private Map<Long, Associate> recordsSent = new ConcurrentHashMap<>();
	private final AtomicLong counter = new AtomicLong();
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMessageProducerApplication.class, args);
	}
	
	@RequestMapping("/sent")
	public Map<Long, Associate> getRecordsSent() {
		return recordsSent;
	}
	
	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public ResponseEntity<String> send(@RequestParam(value = "id") int id, @RequestParam(value = "name") String name) {
		Associate assoc = new Associate();
		assoc.setId(id);
		assoc.setName(name);
		
		recordsSent.put(counter.incrementAndGet(), assoc);
		if (counter.longValue() > 20_000) {
			recordsSent.clear();
		}
		
		source.output().send(MessageBuilder.withPayload(assoc).build());
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
