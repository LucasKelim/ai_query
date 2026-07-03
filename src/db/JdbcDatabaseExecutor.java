package db;

import java.sql.*;
import java.util.*;

import exception.QueryExecutionException;

/**
 * Implementação de {@link DatabaseExecutor} que executa comandos SQL
 * diretamente sobre o banco de dados através de JDBC.
 * <p>
 * Esta classe representa a implementação concreta do padrão de projeto
 * <b>Strategy</b> definido por {@link DatabaseExecutor}, sendo utilizada
 * como estratégia padrão de execução de SQL dentro da biblioteca.
 * <p>
 * Suporta tanto comandos de consulta ({@code SELECT}), retornando o
 * conjunto de resultados como uma lista de mapas, quanto comandos de
 * atualização ({@code INSERT}, {@code UPDATE}, {@code DELETE}), retornando
 * o número de linhas afetadas.
 *
 * @author Lucas Kelim Thiel
 * @see DatabaseExecutor
 */
public class JdbcDatabaseExecutor implements DatabaseExecutor {

    /** Conexão JDBC com o banco de dados sobre o qual os comandos serão executados. */
    private final Connection connection;

    /**
     * Cria um novo executor associado à conexão JDBC informada.
     *
     * @param connection conexão JDBC ativa com o banco de dados do
     *                    usuário da biblioteca
     */
    public JdbcDatabaseExecutor(Connection connection) {
        this.connection = connection;
    }

    /**
     * Executa o comando SQL informado sobre o banco de dados.
     * <p>
     * Caso o comando seja uma consulta (ex.: {@code SELECT}), cada
     * elemento da lista retornada representa uma linha do resultado,
     * mapeando o nome de cada coluna ao seu respectivo valor, na ordem
     * em que aparecem no {@code ResultSet}. Caso seja um comando de
     * atualização (ex.: {@code INSERT}, {@code UPDATE}, {@code DELETE}),
     * a lista retornada conterá um único mapa com a chave
     * {@code "linhas_afetadas"}, indicando quantas linhas foram
     * modificadas.
     *
     * @param sql comando SQL a ser executado
     * @return lista de mapas representando o resultado da execução do
     *         comando SQL
     * @throws QueryExecutionException se ocorrer erro de acesso ao banco
     *         de dados durante a execução do comando
     */
    @Override
    public List<Map<String, Object>> execute(String sql) throws SQLException {
        List<Map<String, Object>> resultado = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            boolean isQuery = stmt.execute(sql);
            if (isQuery) {
                // SELECT: monta lista de mapas coluna -> valor
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();
                    while (rs.next()) {
                        Map<String, Object> linha = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            linha.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        resultado.add(linha);
                    }
                }
            } else {
                // INSERT/UPDATE/DELETE: informa quantas linhas foram afetadas
                int affected = stmt.getUpdateCount();
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("linhas_afetadas", affected);
                resultado.add(info);
            }
        } catch (SQLException e) {
            throw new QueryExecutionException("Erro ao executar SQL: " + e.getMessage(), e);
        }
        return resultado;
    }
}