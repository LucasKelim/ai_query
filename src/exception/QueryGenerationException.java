package exception;

/**
 * Exceção lançada quando ocorre um erro durante a geração de um comando
 * SQL a partir de uma pergunta em linguagem natural, através da API de
 * um provedor de IA.
 * <p>
 * Trata-se de uma exceção não verificada ({@code RuntimeException}),
 * utilizada para encapsular falhas na comunicação com a API de IA — como
 * erros de rede, autenticação inválida, limite de requisições excedido
 * ou respostas em formato inesperado — sem exigir que classes que
 * consomem a biblioteca declarem {@code throws} explícitos para lidar
 * com essas falhas.
 *
 * @author Lucas Kelim Thiel
 * @see ai.AIProvider
 * @see ai.OpenAIProvider
 * @see ai.GeminiProvider
 */
public class QueryGenerationException extends RuntimeException {

    /**
     * Cria uma nova exceção de geração de query com uma mensagem
     * descritiva e a causa original do erro.
     *
     * @param message mensagem descritiva do erro ocorrido
     * @param cause exceção original que causou este erro
     *              (ex.: falha de rede ou erro retornado pela API de IA)
     */
    public QueryGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria uma nova exceção de geração de query com uma mensagem
     * descritiva, sem uma causa associada.
     *
     * @param message mensagem descritiva do erro ocorrido
     */
    public QueryGenerationException(String message) {
        super(message);
    }
}