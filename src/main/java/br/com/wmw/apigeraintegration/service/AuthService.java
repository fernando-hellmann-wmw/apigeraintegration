package br.com.wmw.apigeraintegration.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class AuthService {
	
	private final WebClient webClient;
	
	public AuthService(WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<String> authenticate(String idDistribuidor) {
		return webClient.get()
	            .uri("/distributor/" + idDistribuidor + "/token")
	            .retrieve()
	            .bodyToMono(String.class);
	}

}
