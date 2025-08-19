package br.com.wmw.apigeraintegration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.wmw.apigeraintegration.service.AuthService;
import br.com.wmw.apigeraintegration.service.PedidoService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/apigera")
public class ApiGeraIntegrationController {

	private final AuthService authService;
	private final PedidoService pedidoService;
	
	public ApiGeraIntegrationController(AuthService authService, PedidoService pedidoService) {
		this.authService = authService;
		this.pedidoService = pedidoService;
	}
	
	@GetMapping("/auth/{idDistribuidor}")
    public Mono<String> autenticar(@PathVariable String idDistribuidor) {
		return authService.authenticate(idDistribuidor);
	}
	
	
	@GetMapping("/classifcomercial/{idDistribuidor}")
    public Mono<String> getClassificacaoComercia(@PathVariable String idDistribuidor) {
        return pedidoService.getClassificacaoComercial(idDistribuidor);
	}
	
	@PostMapping("/criapedido/{idDistribuidor}")
    public Mono<String> criaPedido(@PathVariable String idDistribuidor, @RequestBody JsonNode body) {
        return pedidoService.criaPedido(idDistribuidor, body);
	}
	
	@GetMapping("/recomendcomercial/{idDistribuidor}")
    public Mono<String> getRecomendacaoComercial(@PathVariable String idDistribuidor) {
        return pedidoService.getRecomendacaoComercial(idDistribuidor);
	}
	
	@GetMapping("/aplicadesconto/{idDistribuidor}")
    public Mono<String> aplicaDesconto(@PathVariable String idDistribuidor) {
        return pedidoService.aplicaDesconto(idDistribuidor);
	}
	
	@GetMapping("/aprovapedido/{idDistribuidor}")
    public Mono<String> aprovaPedido(@PathVariable String idDistribuidor) {
        return pedidoService.aprovaPedido(idDistribuidor);
	}
	
	@GetMapping("/geraautorizacao/{idDistribuidor}")
    public Mono<String> geraAutorizacao(@PathVariable String idDistribuidor) {
        return pedidoService.aprovaPedido(idDistribuidor);
	}
	
	@GetMapping("/processaautorizacao/{idDistribuidor}")
    public Mono<String> processaAutorizacao(@PathVariable String idDistribuidor) {
        return pedidoService.processaAutorizacao(idDistribuidor);
	}
	
	@GetMapping("/manterpedido/{idDistribuidor}")
    public Mono<String> manterPedido(@PathVariable String idDistribuidor) {
        return pedidoService.manterPedido(idDistribuidor);
	}
	
//	@GetMapping("/classiccomercial/{idDistribuidor}")
//    public Mono<String> getClassificacaoComercia(@RequestHeader("Authorization") String token, @PathVariable String idDistribuidor) {
//        return pedidoService.getClassificacaoComercia(token, idDistribuidor);
//	}
}
