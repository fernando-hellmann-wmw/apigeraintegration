package br.com.wmw.apigeraintegration.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class DynamicJsonMapper {
	
    private final ObjectMapper mapper = new ObjectMapper();

    public String mapJsonToOutput(JsonNode jSonPedido, List<CampoConfig> configList) {
        ObjectNode jSonSaida = mapper.createObjectNode();
        Map<String, List<CampoConfig>> prefixGroupMap = configList.stream().collect(Collectors.groupingBy(this::getPrefixoRaiz));
        for (Map.Entry<String, List<CampoConfig>> prefixo : prefixGroupMap.entrySet()) {
            String prefixoSaida = prefixo.getKey();
            List<CampoConfig> campoConfigList = prefixo.getValue();
            String prefixoEntrada = campoConfigList.get(0).getNmCampoSistema().split("\\.")[0];
            JsonNode listaEntrada = jSonPedido.get(prefixoEntrada);
            if (listaEntrada != null && listaEntrada.isArray()) {
                ArrayNode lista = processarItensLista(listaEntrada, campoConfigList, prefixoEntrada, prefixoSaida);
                jSonSaida.set(prefixoSaida, lista);
            } else {
                for (CampoConfig campoConfig : campoConfigList) {
                	JsonNode valor;
                    if (campoConfig.getDsValorFixo() != null && !campoConfig.getDsValorFixo().isEmpty()) {
                        valor = mapper.valueToTree(campoConfig.getDsValorFixo());
                    } else {
                        valor = getValueFromJson(jSonPedido, campoConfig.getNmCampoSistema());
                    }
                    if (valor != null && !valor.isMissingNode()) {
                        setValue(jSonSaida, campoConfig.getNmCampoErp(), valor);
                    }
                }
            }
        }
        return jSonSaida.toPrettyString();
    }

    private ArrayNode processarItensLista(JsonNode listaEntrada, List<CampoConfig> campos, String prefixoEntrada, String prefixoSaida) {
        ArrayNode resultado = mapper.createArrayNode();
        for (JsonNode itemOrigem : listaEntrada) {
            ObjectNode itemDestino = mapper.createObjectNode();
            for (CampoConfig campo : campos) {
                String pathDentroItem = removePrefixo(campo.getNmCampoSistema(), prefixoEntrada);
                String pathDestino = removePrefixo(campo.getNmCampoErp(), prefixoSaida);
                JsonNode valor;
                if (campo.getDsValorFixo() != null && !campo.getDsValorFixo().isEmpty()) {
                    valor = mapper.valueToTree(campo.getDsValorFixo());
                } else {
                    valor = getValueFromJson(itemOrigem, pathDentroItem);
                }
                if (valor != null && !valor.isMissingNode()) {
                    setValue(itemDestino, pathDestino, valor);
                }
            }
            resultado.add(itemDestino);
        }
        return resultado;
    }

    private String removePrefixo(String caminho, String prefixo) {
        if (caminho.startsWith(prefixo + ".")) {
            return caminho.substring(prefixo.length() + 1);
        }
        return caminho;
    }

    private String getPrefixoRaiz(CampoConfig configCampo) {
        return configCampo.getNmCampoErp().split("\\.")[0];
    }

    private JsonNode getValueFromJson(JsonNode jSonPedido, String nmCampoSistema) {
        String[] nivel = nmCampoSistema.split("\\.");
        JsonNode atual = jSonPedido;
        for (String atributo : nivel) {
            if (atual == null || atual.isMissingNode()) {
            	return null;
            }
            atual = atual.get(atributo);
        }
        return atual;
    }

    private void setValue(ObjectNode jSonSaida, String nmCampoJson, JsonNode valor) {
	    if (nmCampoJson == null || nmCampoJson.isEmpty()) {
	        return;
	    }
	    String[] niveisPropriedades = nmCampoJson.split("\\.");
	    JsonNode jSonAtual = jSonSaida;
	    for (int i = 0; i < niveisPropriedades.length; i++) {
	        String propriedade = niveisPropriedades[i];
	        // Caso seja array
	        if (propriedade.matches(".+\\[\\d+\\]")) {
	            String nomeArray = propriedade.substring(0, propriedade.indexOf("["));
	            int index = Integer.parseInt(propriedade.substring(propriedade.indexOf("[") + 1, propriedade.indexOf("]")));
	            ArrayNode arrayNode;
	            if (!((ObjectNode) jSonAtual).has(nomeArray) || !((ObjectNode) jSonAtual).get(nomeArray).isArray()) {
	                arrayNode = mapper.createArrayNode();
	                ((ObjectNode) jSonAtual).set(nomeArray, arrayNode);
	            } else {
	                arrayNode = (ArrayNode) ((ObjectNode) jSonAtual).get(nomeArray);
	            }
	            // Expande o array até o índice necessário
	            while (arrayNode.size() <= index) {
	                arrayNode.add(mapper.createObjectNode());
	            }
	            // Último nível → seta valor direto
	            if (i == niveisPropriedades.length - 1) {
	                arrayNode.set(index, valor);
	            } else {
	                jSonAtual = arrayNode.get(index);
	            }
	        } 
	        else { // Caso seja objeto
	            if (i == niveisPropriedades.length - 1) {
	                ((ObjectNode) jSonAtual).set(propriedade, valor);
	            } else {
	                if (!((ObjectNode) jSonAtual).has(propriedade) || !((ObjectNode) jSonAtual).get(propriedade).isObject()) {
	                    ((ObjectNode) jSonAtual).set(propriedade, mapper.createObjectNode());
	                }
	                jSonAtual = ((ObjectNode) jSonAtual).get(propriedade);
	            }
	        }
	    }
    }

    
    public static class CampoConfig {
    	
        private String nmCampoSistema; 
        private String nmCampoErp;     
        private String dsValorFixo;     

        public CampoConfig(@JsonProperty("nmCampoSistema") String nmCampoSistema, @JsonProperty("nmCampoErp") String nmCampoErp, @JsonProperty("dsValorFixo")String dsValorFixo) {
        	this.nmCampoSistema = nmCampoSistema;
        	this.nmCampoErp = nmCampoErp;
        	this.dsValorFixo = dsValorFixo;
        }
        
		public String getNmCampoSistema() {
			return nmCampoSistema;
		}
		public void setNmCampoSistema(String nmCampoSistema) {
			this.nmCampoSistema = nmCampoSistema;
		}
		public String getNmCampoErp() {
			return nmCampoErp;
		}
		public void setNmCampoErp(String nmCampoErp) {
			this.nmCampoErp = nmCampoErp;
		}
		public String getDsValorFixo() {
			return dsValorFixo;
		}
		public void setDsValorFixo(String dsValorFixo) {
			this.dsValorFixo = dsValorFixo;
		}
    }
}
