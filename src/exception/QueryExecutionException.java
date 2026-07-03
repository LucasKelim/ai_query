package exception;

/**
 * Exceção lançada quando ocorre um erro durante a execução de um
 * comando SQL sobre o banco de dados, ou durante operações relacionadas
 * ao cache de perguntas e comandos SQL.
 * <p>
 * Trata-se de uma exceção não verificada ({@code RuntimeException}),
 * utilizada para encapsular erros de baixo nível (como {@link java.sql.SQLException})
 * originados pelo driver JDBC, evitando que classes que consomem a
 * biblioteca precisem declarar {@code throws} explícitos para lidar
 * com falhas internas de execução.
 *
 * @author Seu Nome
 * @see db.JdbcDatabaseExecutor
 * @see cache.JdbcQueryCacheRepository
 */
public class QueryExecutionException extends RuntimeException {

    /**
     * Cria uma nova exceção de execução de query com uma mensagem
     * descritiva e a causa original do erro.
     *
     * @param message mensagem descritiva do erro ocorrido
     * @param cause exceção original que causou este erro
     *              (geralmente uma {@link java.sql.SQLException})
     */
    public QueryExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria uma nova exceção de execução de query com uma mensagem
     * descritiva, sem uma causa associada.
     *
     * @param message mensagem descritiva do erro ocorrido
     */
    public QueryExecutionException(String message) {
        super(message);
    }
}