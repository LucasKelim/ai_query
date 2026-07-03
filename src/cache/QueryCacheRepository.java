package cache;

import java.util.Optional;

/**
 * Define o contrato para repositórios responsáveis por armazenar e
 * consultar o cache de perguntas em linguagem natural e seus respectivos
 * comandos SQL já gerados anteriormente.
 * <p>
 * Esta interface representa o padrão de projeto <b>Repository</b>: isola
 * o restante da biblioteca (em especial a classe {@code AIQuery}) dos
 * detalhes de como e onde o cache é efetivamente armazenado, permitindo
 * trocar a estratégia de persistência (banco relacional, arquivo, Redis,
 * etc.) sem alterar quem consome essa interface.
 * <p>
 * O uso do cache evita chamadas repetidas à API de IA para perguntas já
 * respondidas anteriormente, reduzindo custo e tempo de resposta.
 *
 * @author Lucas Kelim Thiel
 * @see cache.JdbcQueryCacheRepository
 */
public interface QueryCacheRepository {
	
	/**
     * Busca o comando SQL previamente armazenado para uma pergunta em
     * linguagem natural já realizada anteriormente.
     *
     * @param question pergunta em linguagem natural a ser buscada no cache
     * @return um {@link Optional} contendo o SQL correspondente, caso a
     *         pergunta já tenha sido processada antes; ou
     *         {@link Optional#empty()} caso não haja registro em cache
     *         para essa pergunta
     */
	Optional<String> findSql(String question);
	
	/**
     * Armazena no cache a associação entre uma pergunta em linguagem
     * natural e o comando SQL gerado pela IA para ela.
     *
     * @param question pergunta em linguagem natural que originou o SQL
     * @param sql comando SQL gerado pela IA para a pergunta informada
     */
	void save(String question, String sql);
}
