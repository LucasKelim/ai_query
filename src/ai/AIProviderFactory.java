package ai;

import java.util.Map;

import config.AIConfig;

/**
 * Fábrica responsável por instanciar implementações concretas de
 * {@link AIProvider} com base no nome do provedor informado.
 * <p>
 * Esta classe representa o padrão de projeto <b>Factory Method</b>:
 * centraliza a lógica de criação dos providers, isolando o restante do
 * sistema de precisar conhecer as classes concretas ({@link OpenAIProvider},
 * {@link GeminiProvider}) ou como elas são construídas.
 * <p>
 * A chave de API de cada provedor é obtida automaticamente do
 * {@link AIConfig} (Singleton), evitando que o chamador precise
 * passá-la manualmente a cada criação.
 * <p>
 * Para adicionar um novo provedor de IA, basta criar uma nova classe que
 * implemente {@link AIProvider} e adicionar um novo {@code case} no
 * método {@link #create(String, String)} — nenhuma outra classe do
 * sistema precisa ser alterada (princípio Aberto/Fechado do SOLID).
 *
 * @author Lucas Kelim Thiel
 * @see AIProvider
 * @see AIConfig
 */
public class AIProviderFactory {
	
	/**
     * Modelos padrão utilizados quando o chamador não especifica
     * explicitamente qual modelo deseja usar para um provedor.
     */
    private static final Map<String, String> DEFAULT_MODELS = Map.of(
            "openai", "gpt-4o-mini",
            "gemini", "gemini-2.5-flash"
    );
    
    /**
     * Cria uma instância de {@link AIProvider} utilizando o modelo
     * padrão definido para o provedor informado.
     * <p>
     * Sobrecarga de {@link #create(String, String)} para os casos em
     * que o usuário da biblioteca não deseja escolher um modelo
     * específico.
     *
     * @param providerName nome do provedor de IA desejado
     *                      (ex.: {@code "openai"}, {@code "gemini"})
     * @return uma instância concreta de {@link AIProvider} correspondente
     *         ao provedor informado, configurada com o modelo padrão
     * @throws IllegalStateException se não houver API key configurada
     *         em {@link AIConfig} para o provedor informado
     * @throws IllegalArgumentException se o nome do provedor for
     *         desconhecido
     */
    public static AIProvider create(String providerName) {
        return create(providerName, DEFAULT_MODELS.get(providerName.toLowerCase()));
    }

    /**
     * Cria uma instância de {@link AIProvider} utilizando o modelo
     * especificado, recuperando a respectiva API key a partir do
     * {@link AIConfig}.
     *
     * @param providerName nome do provedor de IA desejado
     *                      (ex.: {@code "openai"}, {@code "gemini"})
     * @param model nome do modelo a ser utilizado pelo provedor
     *              (ex.: {@code "gpt-4o-mini"}, {@code "gemini-2.5-flash"});
     *              se {@code null}, utiliza o modelo padrão definido em
     *              {@link #DEFAULT_MODELS}
     * @return uma instância concreta de {@link AIProvider} correspondente
     *         ao provedor informado
     * @throws IllegalStateException se não houver API key configurada
     *         em {@link AIConfig} para o provedor informado
     * @throws IllegalArgumentException se o nome do provedor for
     *         desconhecido (não corresponde a nenhum {@code case}
     *         implementado)
     */
	public static AIProvider create(String providerName, String model) {
        AIConfig config = AIConfig.getInstance();
        String apiKey = config.getApiKey(providerName);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key não configurada para o provider: " + providerName);
        }
        
        String modelToUse = (model != null) ? model : DEFAULT_MODELS.get(providerName);

        switch (providerName.toLowerCase()) {
            case "openai":
                return new OpenAIProvider(apiKey, modelToUse);

            case "gemini":
                return new GeminiProvider(apiKey, modelToUse);

            default:
                throw new IllegalArgumentException("Provider de IA desconhecido: " + providerName);
        }
    }
}
