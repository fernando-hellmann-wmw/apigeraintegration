package br.com.wmw.apigeraintegration.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.wmw.apigeraintegration.mapper.DynamicJsonMapper;
import br.com.wmw.apigeraintegration.mapper.DynamicJsonMapper.CampoConfig;
import reactor.core.publisher.Mono;

@Service
public class PedidoService {

	private final WebClient webClient;
	private final DynamicJsonMapper dynamicJsonMapper;
	
	public PedidoService(WebClient webClient, DynamicJsonMapper dynamicJsonMapper) {
		this.webClient = webClient;
		this.dynamicJsonMapper = dynamicJsonMapper;
	}

	public Mono<String> getClassificacaoComercial(String idDistribuidor) {
		return webClient.post()
	            .uri("/distributor/" + idDistribuidor + "/industrysegmentation")
	            .bodyValue("""
	            		{
						    "industryDocuments":[ 
						        {
						        "type": 0, 
						        "document": "string" 
						       }
						    ],
						    "customerDocuments": [ 
						        {
						            "type": 0, 
						            "document": "string" 
						        }
						    ]
						}
	            		""")
	            .retrieve()
	            .bodyToMono(String.class);
	}

	public Mono<String> criaPedido(String idDistribuidor, JsonNode requestBody) {
		String body = getBodyCriacaoPedido(requestBody);
		return webClient.post()
	            .uri("/distributor/" + idDistribuidor + "/promotions/orders?includeOptions=requirements&includeOptions=targetTypeProducts&includeOptions=awards")
	            .bodyValue(body)
	            .retrieve()
	            .bodyToMono(String.class);
	}

	private String getBodyCriacaoPedido(JsonNode requestBody) {
		ObjectMapper mapper = new ObjectMapper();
	    List<CampoConfig> campoConfigList = mapper.convertValue(requestBody.get("configCampoIntegList"), new TypeReference<List<CampoConfig>>() {});
	    ((ObjectNode) requestBody).remove("configCampoIntegList");
		return dynamicJsonMapper.mapJsonToOutput(requestBody.get("pedido"), campoConfigList);
	}

	public Mono<String> getRecomendacaoComercial(String idDistribuidor) {
		return webClient.post()
         .uri("/distributor/" + idDistribuidor + "/personConsolidatedRecommendation")
         .bodyValue("""
         		    {
					    "personPortfolios": 
					        {
					            "document": "string",
					            "type": 0
					        }
					}
         		""")
         .retrieve()
         .bodyToMono(String.class);
	}

	public Mono<String> aplicaDesconto(String idDistribuidor) {
		return webClient.post()
		         .uri("/distributor/" + idDistribuidor + "/promotions/apply?includeOptions=requirements&includeOptions=awards&includeOptions=considerAllIndustries")
		         .bodyValue(getBodyAplicaDesconto())
		         .retrieve()
		         .bodyToMono(String.class);
	}

	private Object getBodyAplicaDesconto() {
		// TODO Auto-generated method stub
		return null;
	}

	public Mono<String> aprovaPedido(String idDistribuidor) {
		return webClient.put()
		         .uri("/distributor/" + idDistribuidor + "/orderStatus")
		         .bodyValue("""
		         		{
						  "creatorSystemOrderCode": "string",
						  "creatorSystemOrderVersion": "string", 
						  "status": 0 
						}
		         		""")
		         .retrieve()
		         .bodyToMono(String.class);
	}

	public Mono<String> processaAutorizacao(String idDistribuidor) {
		return webClient.put()
		         .uri("/distributor/" + idDistribuidor + "/orderAuthorization")
		         .bodyValue("""
		         		""")
		         .retrieve()
		         .bodyToMono(String.class);
	}

	public Mono<String> manterPedido(String idDistribuidor) {
		return webClient.put()
		         .uri("/distributor/" + idDistribuidor + "/order")
		         .bodyValue("""
		         		""")
		         .retrieve()
		         .bodyToMono(String.class);
	}
	
}
