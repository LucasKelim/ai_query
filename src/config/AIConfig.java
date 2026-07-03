package config;

import java.util.HashMap;
import java.util.Map;

/**
 * Armazena as configurações globais da biblioteca, em especial as chaves
 * de API dos provedores de IA suportados e o provedor padrão a ser
 * utilizado quando nenhum for explicitamente informado.
 * <p>
 * Esta classe representa o padrão de projeto <b>Singleton</b>: garante
 * que exista apenas uma única instância de configuração acessível em
 * qualquer ponto da aplicação, evitando a necessidade de passar a API
 * key manualmente entre as classes que compõem a biblioteca.
 * <p>
 * As chaves de API são armazenadas em um {@link Map}, indexadas pelo
 * nome do provedor (em minúsculas). Essa abordagem permite adicionar
 * novos provedores de IA no futuro sem exigir alterações nesta classe
 * (princípio Aberto/Fechado do SOLID).
 *
 * @author Lucas Kelim Thiel
 * @see ai.AIProviderFactory
 */
public class AIConfig {
	
	/** Única instância desta classe (Singleton). */
	private static AIConfig instance;

	/** Chaves de API armazenadas por nome do provedor (em minúsculas). */
    private final Map<String, String> apiKeys = new HashMap<>();
    
    /** Nome do provedor de IA utilizado por padrão quando nenhum é informado. */
    private String defaultProvider = "gemini";

    /**
     * Construtor privado, impedindo a criação de instâncias fora desta
     * classe e garantindo o comportamento de Singleton.
     */
    private AIConfig() {
    }

    /**
     * Retorna a única instância de {@code AIConfig} existente na
     * aplicação, criando-a na primeira chamada (inicialização
     * preguiçosa / lazy initialization).
     *
     * @return a instância única de {@code AIConfig}
     */
    public static AIConfig getInstance() {
        if (instance == null) {
            instance = new AIConfig();
        }
        return instance;
    }

    /**
     * Define a chave de API a ser utilizada para um provedor de IA
     * específico.
     *
     * @param providerName nome do provedor de IA (ex.: {@code "openai"},
     *                      {@code "gemini"}); não é sensível a maiúsculas
     *                      ou minúsculas, pois é convertido internamente
     * @param apiKey chave de API correspondente ao provedor informado
     */
    public void setApiKey(String providerName, String apiKey) {
        apiKeys.put(providerName.toLowerCase(), apiKey);
    }

    /**
     * Recupera a chave de API previamente configurada para um provedor
     * de IA específico.
     *
     * @param providerName nome do provedor de IA cuja chave será
     *                      recuperada; não é sensível a maiúsculas ou
     *                      minúsculas
     * @return a chave de API associada ao provedor informado, ou
     *         {@code null} caso nenhuma chave tenha sido configurada
     *         para ele
     */
    public String getApiKey(String providerName) {
        return apiKeys.get(providerName.toLowerCase());
    }

    /**
     * Retorna o nome do provedor de IA definido como padrão.
     *
     * @return o nome do provedor padrão atualmente configurado
     */
    public String getDefaultProvider() {
        return defaultProvider;
    }

    /**
     * Define qual provedor de IA deve ser utilizado como padrão.
     *
     * @param defaultProvider nome do provedor de IA a ser definido
     *                        como padrão
     */
    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }
}
