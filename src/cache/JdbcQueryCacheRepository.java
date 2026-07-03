package cache;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import exception.QueryExecutionException;

/**
 * Implementação de {@link QueryCacheRepository} que persiste o cache de
 * perguntas e comandos SQL diretamente no banco de dados do usuário da
 * biblioteca, utilizando JDBC.
 * <p>
 * Esta classe representa o padrão de projeto <b>Repository</b>: isola a
 * lógica de armazenamento e consulta do cache do restante da aplicação,
 * permitindo que a estratégia de persistência (hoje, uma tabela SQL) seja
 * substituída no futuro (ex.: por Redis ou arquivo) sem impactar quem
 * consome a interface {@link QueryCacheRepository}.
 * <p>
 * A tabela {@code ai_query_cache} é criada automaticamente na primeira
 * utilização, caso ainda não exista no banco do usuário. Cada pergunta é
 * identificada por um hash SHA-256 do seu texto normalizado (sem espaços
 * nas extremidades e em minúsculas), evitando duplicidade por diferenças
 * triviais de digitação.
 *
 * @author Lucas Kelim Thiel
 * @see QueryCacheRepository
 */
public class JdbcQueryCacheRepository implements QueryCacheRepository {
	
	/** Conexão JDBC com o banco de dados onde o cache será armazenado. */
	private final Connection connection;

	/**
     * Cria o repositório de cache associado à conexão informada e garante
     * que a tabela de cache exista no banco, criando-a automaticamente
     * caso necessário.
     *
     * @param connection conexão JDBC ativa com o banco de dados do
     *                    usuário da biblioteca
     * @throws QueryExecutionException se ocorrer erro ao verificar ou
     *         criar a tabela {@code ai_query_cache}
     */
	public JdbcQueryCacheRepository(Connection connection) {
		this.connection = connection;
		ensureTableExists();
	}
	
	/**
     * Garante a existência da tabela {@code ai_query_cache} no banco de
     * dados, criando-a caso ainda não exista.
     *
     * @throws QueryExecutionException se ocorrer erro de acesso ao banco
     *         durante a criação da tabela
     */
	private void ensureTableExists() {
		String sql;
        try {
            if (isPostgres()) {
                sql = "CREATE TABLE IF NOT EXISTS ai_query_cache (" +
                        "id SERIAL PRIMARY KEY, " +
                        "question_hash VARCHAR(64) NOT NULL UNIQUE, " +
                        "question TEXT NOT NULL, " +
                        "sql_generated TEXT NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            } else {
                sql = "CREATE TABLE IF NOT EXISTS ai_query_cache (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "question_hash VARCHAR(64) NOT NULL UNIQUE, " +
                        "question TEXT NOT NULL, " +
                        "sql_generated TEXT NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            }
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao identificar o banco de dados: " + e.getMessage(), e);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao criar tabela de cache: " + e.getMessage(), e);
        }
    }
	
    private boolean isPostgres() throws SQLException {
        return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
    }
	
	/**
     * Busca no cache o comando SQL previamente gerado para uma pergunta
     * já realizada anteriormente.
     *
     * @param question pergunta em linguagem natural a ser buscada no cache
     * @return um {@link Optional} contendo o SQL correspondente, caso a
     *         pergunta já tenha sido feita antes; ou {@link Optional#empty()}
     *         caso não haja registro para essa pergunta
     * @throws QueryExecutionException se ocorrer erro de acesso ao banco
     *         durante a consulta
     */
	@Override
	public Optional<String> findSql(String question) {
		String hash = hash(question);
        String sql = "SELECT sql_generated FROM ai_query_cache WHERE question_hash = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("sql_generated"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao consultar cache: " + e.getMessage(), e);
        }
	}

	/**
     * Salva no cache a associação entre uma pergunta em linguagem natural
     * e o comando SQL gerado pela IA para ela, evitando chamadas futuras
     * à API para a mesma pergunta.
     * <p>
     * Caso a pergunta já exista no cache (violação de chave única,
     * podendo ocorrer em cenários de concorrência), a inserção é
     * silenciosamente ignorada.
     *
     * @param question pergunta em linguagem natural que originou o SQL
     * @param sql comando SQL gerado pela IA para a pergunta informada
     * @throws QueryExecutionException se ocorrer erro de acesso ao banco
     *         durante a inserção, exceto em caso de chave duplicada
     */
	@Override
	public void save(String question, String sql) {
		String hash = hash(question);
        String insert = "INSERT INTO ai_query_cache (question_hash, question, sql_generated) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, hash);
            ps.setString(2, question);
            ps.setString(3, sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            // se já existir (corrida entre threads, por exemplo), ignora silenciosamente
            if (!isDuplicateKeyError(e)) {
                throw new QueryExecutionException("Erro ao salvar no cache: " + e.getMessage(), e);
            }
        }
	}
	
	/**
     * Verifica se a exceção SQL informada representa uma violação de
     * restrição de integridade (ex.: chave única duplicada).
     *
     * @param e exceção SQL a ser verificada
     * @return {@code true} se a exceção corresponder a uma violação de
     *         integridade (SQLState iniciado em {@code "23"});
     *         {@code false} caso contrário
     */
    private boolean isDuplicateKeyError(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
	
    /**
     * Gera um hash SHA-256 do texto informado, normalizado (sem espaços
     * nas extremidades e em minúsculas), utilizado como chave única de
     * identificação da pergunta no cache.
     *
     * @param text texto a ser transformado em hash (a pergunta em
     *             linguagem natural)
     * @return representação hexadecimal do hash SHA-256 do texto
     * @throws QueryExecutionException se o algoritmo de hash
     *         {@code SHA-256} não estiver disponível no ambiente de
     *         execução
     */
	private String hash(String text) {
		try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.trim().toLowerCase().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new QueryExecutionException("Erro ao gerar hash: " + e.getMessage(), e);
        }
	}

}
