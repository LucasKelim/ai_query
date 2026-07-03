package exception;

/**
 * Exceção lançada quando ocorre um erro durante a extração do schema
 * (estrutura de tabelas, colunas e relacionamentos) do banco de dados
 * do usuário da biblioteca, ou durante a tentativa de estabelecer
 * conexão com esse banco.
 * <p>
 * Trata-se de uma exceção não verificada ({@code RuntimeException}),
 * utilizada para encapsular erros de baixo nível (como {@link java.sql.SQLException})
 * originados pelo driver JDBC durante a leitura de metadados ou a
 * abertura da conexão, evitando que classes que consomem a biblioteca
 * precisem declarar {@code throws} explícitos para lidar com essas
 * falhas.
 *
 * @author Lucas Kelim Thiel
 * @see builder.DatabaseModelBuilder
 * @see service.AIQuery
 */
public class SchemaExtractionException extends RuntimeException {

    /**
     * Cria uma nova exceção de extração de schema com uma mensagem
     * descritiva e a causa original do erro.
     *
     * @param message mensagem descritiva do erro ocorrido
     * @param cause exceção original que causou este erro
     *              (geralmente uma {@link java.sql.SQLException})
     */
    public SchemaExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria uma nova exceção de extração de schema com uma mensagem
     * descritiva, sem uma causa associada.
     *
     * @param message mensagem descritiva do erro ocorrido
     */
    public SchemaExtractionException(String message) {
        super(message);
    }
}