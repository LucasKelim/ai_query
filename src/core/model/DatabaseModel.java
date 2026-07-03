package core.model;

import java.util.List;

/**
 * Representa o schema completo de um banco de dados, contendo a lista
 * de todas as suas tabelas.
 * <p>
 * Esta classe é o objeto raiz do modelo de domínio ({@code core.model})
 * que descreve a estrutura do banco de dados em memória. É construída por
 * {@link builder.DatabaseModelBuilder} a partir dos metadados obtidos via
 * JDBC, e consumida por {@link builder.PromptBuilder} para descrever o
 * schema no prompt enviado à IA.
 * <p>
 * Importante: esta classe representa apenas a <b>estrutura</b> do banco
 * de dados (tabelas, colunas e relacionamentos) — nenhum dado armazenado
 * nas tabelas é mantido aqui.
 *
 * @author Lucas Keim Thiel
 * @see TableModel
 * @see builder.DatabaseModelBuilder
 */
public class DatabaseModel {

    /** Lista de todas as tabelas que compõem o schema do banco de dados. */
    private List<TableModel> tables;

    /**
     * Cria uma nova representação do schema do banco de dados com a
     * lista de tabelas informada.
     *
     * @param tables lista de tabelas que compõem o schema do banco
     *               de dados
     */
    public DatabaseModel(List<TableModel> tables) {
        setTables(tables);
    }

    /**
     * Retorna a lista de tabelas que compõem o schema do banco de dados.
     *
     * @return lista de {@link TableModel} representando as tabelas
     *         do banco
     */
    public List<TableModel> getTables() {
        return tables;
    }

    /**
     * Define a lista de tabelas que compõem o schema do banco de dados.
     *
     * @param tables lista de {@link TableModel} representando as
     *               tabelas do banco
     */
    public void setTables(List<TableModel> tables) {
        this.tables = tables;
    }

    /**
     * Retorna uma representação textual do schema completo do banco de
     * dados, concatenando a descrição de todas as suas tabelas.
     * <p>
     * Este é o formato utilizado por {@link builder.PromptBuilder} para
     * incluir o schema dentro do prompt enviado à API de IA.
     *
     * @return representação textual de todas as tabelas do schema,
     *         uma por linha
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TableModel t : tables) {
            sb.append(t.toString()).append("\n");
        }
        return sb.toString();
    }
}