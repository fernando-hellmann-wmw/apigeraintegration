package br.com.wmw.apigeraintegration.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.wmw.apigeraintegration.mapper.DynamicObjectMapper.CampoConfig;

public class DynamicJsonMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public ObjectNode mapJsonToOutput(JsonNode inputJson, List<CampoConfig> campos) {
        ObjectNode root = mapper.createObjectNode();
        Map<String, List<CampoConfig>> agrupados = campos.stream()
            .collect(Collectors.groupingBy(this::getPrefixoRaiz));
        for (Map.Entry<String, List<CampoConfig>> entry : agrupados.entrySet()) {
            String prefixoSaida = entry.getKey();
            List<CampoConfig> grupo = entry.getValue();
            String prefixoEntrada = grupo.get(0).getNmCampoSistema().split("\\.")[0];
            JsonNode listaEntrada = inputJson.get(prefixoEntrada);
            if (listaEntrada != null && listaEntrada.isArray()) {
                ArrayNode lista = processarItensLista(listaEntrada, grupo, prefixoEntrada, prefixoSaida);
                root.set(prefixoSaida, lista);
            } else {
                for (CampoConfig campo : grupo) {
                    JsonNode valor = resolvePath(inputJson, campo.getNmCampoSistema());
                    if (valor != null && !valor.isMissingNode()) {
                        setPath(root, campo.getNmCampoErp(), valor);
                    }
                }
            }
        }
        return root;
    }

    private ArrayNode processarItensLista(JsonNode listaEntrada, List<CampoConfig> campos, String prefixoEntrada, String prefixoSaida) {
        ArrayNode resultado = mapper.createArrayNode();
        for (JsonNode itemOrigem : listaEntrada) {
            ObjectNode itemDestino = mapper.createObjectNode();
            for (CampoConfig campo : campos) {
                String pathDentroItem = removerPrefixo(campo.getNmCampoSistema(), prefixoEntrada);
                String pathDestino = removerPrefixo(campo.getNmCampoErp(), prefixoSaida);
                JsonNode valor = resolvePath(itemOrigem, pathDentroItem);
                if (valor != null && !valor.isMissingNode()) {
                    setPath(itemDestino, pathDestino, valor);
                }
            }
            resultado.add(itemDestino);
        }
        return resultado;
    }

    private String removerPrefixo(String caminho, String prefixo) {
        if (caminho.startsWith(prefixo + ".")) {
            return caminho.substring(prefixo.length() + 1);
        }
        return caminho;
    }

    private String getPrefixoRaiz(CampoConfig campo) {
        return campo.getNmCampoErp().split("\\.")[0];
    }

    private JsonNode resolvePath(JsonNode node, String path) {
        String[] partes = path.split("\\.");
        JsonNode atual = node;
        for (String parte : partes) {
            if (atual == null || atual.isMissingNode()) {
            	return null;
            }
            atual = atual.get(parte);
        }
        return atual;
    }

    private void setPath(ObjectNode root, String path, JsonNode valor) {
        if (path.isEmpty()) {
        	return;
        }
        String[] partes = path.split("\\.");
        ObjectNode atual = root;
        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.has(parte) || !atual.get(parte).isObject()) {
                atual.set(parte, mapper.createObjectNode());
            }
            atual = (ObjectNode) atual.get(parte);
        }
        atual.set(partes[partes.length - 1], valor);
    }
}
