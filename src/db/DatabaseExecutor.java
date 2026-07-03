package db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Define o contrato para executores de comandos SQL sobre o banco de
 * dados do usuário da biblioteca.
 * <p>
 * Esta interface representa o papel de <b>Strategy</b> no padrão de
 * projeto Strategy: permite que a biblioteca troque a forma de execução
 * do SQL (hoje, via JDBC) sem que o restante do sistema (como
 * {@code AIQuery}) precise conhecer os detalhes da implementação
 * utilizada.
 *
 * @author Seu Nome
 * @see db.JdbcDatabaseExecutor
 */
public interface DatabaseExecutor {

    /**
     * Executa um comando SQL sobre o banco de dados e retorna seu
     * resultado.
     * <p>
     * Para comandos de consulta (ex.: {@code SELECT}), cada elemento da
     * lista retornada representa uma linha do resultado, mapeando o
     * nome de cada coluna ao seu respectivo valor. Para comandos de
     * atualização (ex.: {@code INSERT}, {@code UPDATE}, {@code DELETE}),
     * a implementação pode retornar informações sobre o número de linhas
     * afetadas.
     *
     * @param sql comando SQL a ser executado
     * @return lista de mapas representando o resultado da execução do
     *         comando SQL
     * @throws SQLException se ocorrer erro de acesso ao banco de dados
     *         durante a execução do comando
     */
    List<Map<String, Object>> execute(String sql) throws SQLException;

}