package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ai.AIProvider;
import ai.AIProviderFactory;
import builder.DatabaseModelBuilder;
import builder.PromptBuilder;
import cache.JdbcQueryCacheRepository;
import cache.QueryCacheRepository;
import config.AIConfig;
import core.model.DatabaseModel;
import db.DatabaseExecutor;
import db.JdbcDatabaseExecutor;
import exception.QueryExecutionException;
import exception.SchemaExtractionException;

/**
 * Ponto único de entrada da biblioteca, permitindo que o usuário gere
 * comandos SQL a partir de perguntas em linguagem natural e,
 * opcionalmente, execute esses comandos diretamente sobre seu banco de
 * dados.
 * <p>
 * Esta classe representa o padrão de projeto <b>Facade</b>: esconde toda
 * a complexidade da biblioteca (extração de schema, montagem de prompt,
 * chamada à API de IA, cache de perguntas e execução de SQL) atrás de
 * uma interface simples, com poucos métodos públicos. Internamente,
 * coordena diversas outras classes que implementam os demais padrões de
 * projeto da biblioteca:
 * <ul>
 *   <li>{@link builder.DatabaseModelBuilder} e {@link builder.PromptBuilder} (Builder)</li>
 *   <li>{@link ai.AIProvider} e {@link db.DatabaseExecutor} (Strategy)</li>
 *   <li>{@link ai.AIProviderFactory} (Factory Method)</li>
 *   <li>{@link config.AIConfig} (Singleton)</li>
 *   <li>{@link cache.QueryCacheRepository} (Repository)</li>
 * </ul>
 * <p>
 * Implementa {@link AutoCloseable}, permitindo seu uso com
 * <i>try-with-resources</i>, garantindo o fechamento automático da
 * conexão com o banco de dados ao final do uso.
 *
 * @author Seu Nome
 * @see ai.AIProvider
 * @see db.DatabaseExecutor
 * @see cache.QueryCacheRepository
 */
public class AIQuery implements AutoCloseable {

    /** Conexão JDBC com o banco de dados do usuário da biblioteca. */
    private final Connection connection;

    /** Schema do banco de dados, extraído automaticamente na criação da instância. */
    private final DatabaseModel schema;

    /** Provedor de IA utilizado para gerar os comandos SQL. */
    private final AIProvider aiProvider;

    /** Executor responsável por rodar os comandos SQL sobre o banco de dados. */
    private final DatabaseExecutor executor;

    /** Repositório de cache de perguntas e comandos SQL já gerados. */
    private final QueryCacheRepository cache;

    /**
     * Cria uma nova instância de {@code AIQuery}, extraindo automaticamente
     * o schema do banco de dados associado à conexão informada e
     * inicializando o executor de SQL e o repositório de cache.
     *
     * @param connection conexão JDBC ativa com o banco de dados do
     *                    usuário da biblioteca
     * @param aiProvider provedor de IA a ser utilizado para gerar
     *                   comandos SQL a partir de perguntas em linguagem
     *                   natural
     * @throws SchemaExtractionException se ocorrer erro ao extrair o
     *         schema do banco de dados ou ao inicializar a tabela de
     *         cache
     */
    public AIQuery(Connection connection, AIProvider aiProvider) {
        this.connection = connection;
        this.schema = new DatabaseModelBuilder().build(connection);
        this.aiProvider = aiProvider;
        this.executor = new JdbcDatabaseExecutor(connection);
        this.cache = new JdbcQueryCacheRepository(connection);
    }

    /**
     * Monta o prompt textual que seria enviado à API de IA para a
     * pergunta informada, combinando as instruções fixas de formatação
     * com o schema do banco de dados e a pergunta do usuário.
     * <p>
     * Útil para fins de depuração ou para inspecionar exatamente o
     * texto que será enviado à IA antes de efetivamente gerar o SQL.
     *
     * @param question pergunta em linguagem natural
     * @return o prompt completo que seria enviado à API de IA
     */
    public String prompt(String question) {
        return new PromptBuilder()
                 .withSchema(schema)
                 .withQuestion(question)
                 .build();
    }

    /**
     * Gera o comando SQL correspondente a uma pergunta em linguagem
     * natural, utilizando o provedor de IA configurado.
     * <p>
     * Antes de acionar a API de IA, verifica se a pergunta já foi
     * respondida anteriormente, consultando o {@link QueryCacheRepository}.
     * Caso já exista um SQL em cache para essa pergunta, ele é retornado
     * diretamente, evitando uma nova chamada à API. Caso contrário, o
     * SQL é gerado, armazenado em cache e então retornado.
     *
     * @param question pergunta em linguagem natural
     *                 (ex.: {@code "clientes com saldo maior que mil"})
     * @return o comando SQL correspondente à pergunta informada
     * @throws exception.QueryGenerationException se ocorrer falha na
     *         comunicação com a API de IA
     */
    public String sql(String question) {
        Optional<String> cached = cache.findSql(question);
        if (cached.isPresent()) {
            return cached.get();
        }

        String prompt = prompt(question);
        String generatedSql = aiProvider.generateSql(prompt);
        cache.save(question, generatedSql);
        return generatedSql;
    }

    /**
     * Gera o comando SQL correspondente à pergunta informada e o
     * executa imediatamente sobre o banco de dados, retornando o
     * resultado.
     * <p>
     * Combina, em uma única chamada, os métodos {@link #sql(String)}
     * e {@link #execute(String)}.
     *
     * @param question pergunta em linguagem natural
     * @return lista de mapas representando o resultado da execução do
     *         SQL gerado para a pergunta informada
     * @throws exception.QueryGenerationException se ocorrer falha na
     *         geração do SQL pela API de IA
     * @throws QueryExecutionException se ocorrer erro ao executar o
     *         SQL gerado sobre o banco de dados
     */
    public List<Map<String, Object>> ask(String question) {
        String sql = sql(question);
        return execute(sql);
    }

    /**
     * Executa um comando SQL diretamente sobre o banco de dados,
     * retornando o resultado da execução.
     * <p>
     * Útil quando o usuário já possui um comando SQL (gerado
     * previamente por {@link #sql(String)} ou escrito manualmente) e
     * deseja apenas executá-lo, sem passar novamente pela geração via IA.
     *
     * @param sql comando SQL a ser executado
     * @return lista de mapas representando o resultado da execução do
     *         comando SQL
     * @throws QueryExecutionException se ocorrer erro de acesso ao
     *         banco de dados durante a execução do comando
     */
    public List<Map<String, Object>> execute(String sql) {
        try {
            return executor.execute(sql);
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao executar sql: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna o schema do banco de dados extraído na criação desta
     * instância.
     *
     * @return o {@link DatabaseModel} representando a estrutura do
     *         banco de dados
     */
    public DatabaseModel getSchema() {
        return schema;
    }

    /**
     * Cria uma instância de {@code AIQuery} conectada ao banco de dados
     * informado, utilizando o modelo padrão do provedor de IA escolhido.
     * <p>
     * Sobrecarga de {@link #connect(String, String, String, String, String, String)}
     * para os casos em que o usuário não deseja especificar um modelo
     * de IA específico.
     *
     * @param jdbcUrl URL de conexão JDBC com o banco de dados
     * @param user usuário do banco de dados
     * @param password senha do banco de dados
     * @param providerName nome do provedor de IA a ser utilizado
     *                      (ex.: {@code "openai"}, {@code "gemini"})
     * @param apiKey chave de API do provedor de IA informado
     * @return uma nova instância de {@code AIQuery} pronta para uso
     * @throws SchemaExtractionException se ocorrer erro ao conectar
     *         ao banco de dados ou ao extrair seu schema
     */
    public static AIQuery connect(String jdbcUrl, String user, String password, String providerName, String apiKey) {
        return connect(jdbcUrl, user, password, providerName, apiKey, null);
    }

    /**
     * Cria uma instância de {@code AIQuery} conectada ao banco de dados
     * informado, configurando a API key do provedor de IA e o modelo
     * a ser utilizado.
     * <p>
     * Este método representa a forma simplificada de inicialização da
     * biblioteca, encapsulando em uma única chamada: a configuração da
     * chave de API em {@link AIConfig}, a criação do {@link AIProvider}
     * via {@link AIProviderFactory}, a abertura da conexão JDBC e a
     * construção do {@code AIQuery}.
     *
     * @param jdbcUrl URL de conexão JDBC com o banco de dados
     * @param user usuário do banco de dados
     * @param password senha do banco de dados
     * @param providerName nome do provedor de IA a ser utilizado
     *                      (ex.: {@code "openai"}, {@code "gemini"})
     * @param apiKey chave de API do provedor de IA informado
     * @param model nome do modelo de IA a ser utilizado; se
     *              {@code null}, utiliza o modelo padrão definido em
     *              {@link AIProviderFactory}
     * @return uma nova instância de {@code AIQuery} pronta para uso
     * @throws SchemaExtractionException se ocorrer erro ao conectar
     *         ao banco de dados ou ao extrair seu schema
     * @throws IllegalStateException se não houver API key configurada
     *         para o provedor informado
     * @throws IllegalArgumentException se o nome do provedor for
     *         desconhecido
     */
    public static AIQuery connect(String jdbcUrl, String user, String password, String providerName, String apiKey, String model) {
        try {
            AIConfig.getInstance().setApiKey(providerName, apiKey);
            AIProvider provider = AIProviderFactory.create(providerName, model);
            Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
            return new AIQuery(connection, provider);
        } catch (SQLException e) {
            throw new SchemaExtractionException("Erro ao conectar no banco: " + e.getMessage(), e);
        }
    }

    /**
     * Fecha a conexão com o banco de dados associada a esta instância,
     * caso ainda esteja aberta.
     * <p>
     * Permite o uso de {@code AIQuery} em blocos <i>try-with-resources</i>,
     * garantindo o fechamento automático e seguro da conexão ao final
     * do uso, mesmo em caso de exceção.
     *
     * @throws QueryExecutionException se ocorrer erro ao fechar a
     *         conexão com o banco de dados
     */
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao fechar conexão: " + e.getMessage(), e);
        }
    }
}