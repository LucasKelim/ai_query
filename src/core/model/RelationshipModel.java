package core.model;

/**
 * Representa um relacionamento (chave estrangeira) entre uma coluna de
 * uma tabela e a coluna referenciada em outra tabela do banco de dados.
 * <p>
 * Esta classe faz parte do modelo de domínio ({@code core.model}) que
 * descreve a estrutura do banco de dados em memória, sendo utilizada por
 * {@link builder.DatabaseModelBuilder} durante a extração das chaves
 * estrangeiras e por {@link builder.PromptBuilder} para descrever os
 * relacionamentos entre tabelas no prompt enviado à IA — informação
 * essencial para que a IA consiga gerar comandos SQL com {@code JOIN}
 * corretos.
 *
 * @author Seu Nome
 * @see TableModel
 * @see DatabaseModel
 */
public class RelationshipModel {

    /** Nome da coluna, na tabela atual, que representa a chave estrangeira. */
    private String columnName;

    /** Nome da tabela referenciada pela chave estrangeira. */
    private String referencedTable;

    /** Nome da coluna referenciada na tabela de destino (geralmente a chave primária). */
    private String referencedColumn;

    /**
     * Cria uma nova representação de relacionamento entre uma coluna da
     * tabela atual e a coluna referenciada em outra tabela.
     *
     * @param columnName nome da coluna, na tabela atual, que representa
     *                    a chave estrangeira
     * @param referencedTable nome da tabela referenciada
     * @param referencedColumn nome da coluna referenciada na tabela de
     *                         destino
     */
    public RelationshipModel(String columnName, String referencedTable, String referencedColumn) {
        setColumnName(columnName);
        setReferencedTable(referencedTable);
        setReferencedColumn(referencedColumn);
    }

    /**
     * Retorna o nome da coluna, na tabela atual, que representa a chave
     * estrangeira.
     *
     * @return o nome da coluna de chave estrangeira
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Define o nome da coluna, na tabela atual, que representa a chave
     * estrangeira.
     *
     * @param columnName o nome da coluna de chave estrangeira
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Retorna o nome da tabela referenciada pela chave estrangeira.
     *
     * @return o nome da tabela referenciada
     */
    public String getReferencedTable() {
        return referencedTable;
    }

    /**
     * Define o nome da tabela referenciada pela chave estrangeira.
     *
     * @param referencedTable o nome da tabela referenciada
     */
    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    /**
     * Retorna o nome da coluna referenciada na tabela de destino.
     *
     * @return o nome da coluna referenciada
     */
    public String getReferencedColumn() {
        return referencedColumn;
    }

    /**
     * Define o nome da coluna referenciada na tabela de destino.
     *
     * @param referencedColumn o nome da coluna referenciada
     */
    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
    }

    /**
     * Retorna uma representação textual do relacionamento, no formato
     * {@code coluna -> tabela_referenciada.coluna_referenciada}.
     * <p>
     * Este formato é utilizado por {@link TableModel#toString()} e,
     * consequentemente, por {@link builder.PromptBuilder}, para
     * descrever os relacionamentos entre tabelas no prompt enviado
     * à IA.
     *
     * @return representação textual do relacionamento
     */
    @Override
    public String toString() {
        return columnName + " -> " + referencedTable + "." + referencedColumn;
    }
}