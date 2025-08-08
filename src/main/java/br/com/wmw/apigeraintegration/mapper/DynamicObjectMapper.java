package br.com.wmw.apigeraintegration.mapper;

import java.lang.reflect.Field;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DynamicObjectMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectNode mapPedidoToJson(Object pedido, List<CampoConfig> campos) {
        ObjectNode rootNode = objectMapper.createObjectNode();

        for (CampoConfig campo : campos) {
            try {
                Object valor = resolveValor(pedido, campo.getNmCampoSistema());
                if (valor != null) {
                    setJsonValue(rootNode, campo.getNmCampoErp(), valor);
                }
            } catch (Exception e) {
                e.printStackTrace(); // ou logue
            }
        }

        return rootNode;
    }

    private Object resolveValor(Object origem, String caminho) throws Exception {
        String[] partes = caminho.split("\\.");
        Object atual = origem;
        for (String parte : partes) {
            if (atual == null) return null;

            Field field = getField(atual.getClass(), parte);
            field.setAccessible(true);
            atual = field.get(atual);
        }
        return atual;
    }

    private void setJsonValue(ObjectNode root, String caminhoJson, Object valor) {
        String[] partes = caminhoJson.split("\\.");
        ObjectNode atual = root;
        for (int i = 0; i < partes.length - 1; i++) {
            String parte = partes[i];
            if (!atual.has(parte) || !atual.get(parte).isObject()) {
                atual.set(parte, objectMapper.createObjectNode());
            }
            atual = (ObjectNode) atual.get(parte);
        }
        atual.putPOJO(partes[partes.length - 1], valor);
    }

    private Field getField(Class<?> clazz, String nome) throws NoSuchFieldException {
        Class<?> atual = clazz;
        while (atual != null) {
            try {
                return atual.getDeclaredField(nome);
            } catch (NoSuchFieldException e) {
                atual = atual.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Campo " + nome + " não encontrado na classe " + clazz.getName());
    }

    public static class CampoConfig {
        private String nmCampoSistema; // Ex: pedido.cliente.cdCliente
        private String nmCampoErp;     // Ex: paymentInformation.paymentPlanCode

        public CampoConfig(String sistema, String erp) {
            this.nmCampoSistema = sistema;
            this.nmCampoErp = erp;
        }

        public String getNmCampoSistema() { return nmCampoSistema; }
        public String getNmCampoErp() { return nmCampoErp; }
    }
}
