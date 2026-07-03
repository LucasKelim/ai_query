package core.model;

import java.util.List;

/**
 * Representa uma tabela do banco de dados, contendo seu nome, suas
 * colunas e os relacionamentos (chaves estrangeiras) que possui com
 * outras tabelas.
 * <p>
 * Esta classe faz parte do modelo de domínio ({@code core.model}) que
 * descreve a estrutura do banco de dados em memória. Um {@link DatabaseModel}
 * é composto por várias instâncias de {@code TableModel}, cada uma
 * composta, por sua vez, por instâncias de {@link ColumnModel} e
 * {@link RelationshipModel}.
 *
 * @author Seu Nome
 * @see ColumnModel
 * @see RelationshipModel
 * @see DatabaseModel
 */
public class TableModel {

    /** Nome da tabela. */
    private String name;

    /** Lista de colunas que compõem a tabela. */
    private List<ColumnModel> columns;

    /** Lista de relacionamentos (chaves estrangeiras) que a tabela possui com outras tabelas. */
    private List<RelationshipModel> relationships;

    /**
     * Cria uma nova representação de tabela com o nome, colunas e
     * relacionamentos informados.
     *
     * @param name nome da tabela
     * @param columns lista de colunas que compõem a tabela
     * @param relationships lista de relacionamentos (chaves estrangeiras)
     *                       que a tabela possui com outras tabelas
     */
    public TableModel(String name, List<ColumnModel> columns, List<RelationshipModel> relationships) {
        setName(name);
        setColumns(columns);
        setRelationships(relationships);
    }

    /**
     * Retorna o nome da tabela.
     *
     * @return o nome da tabela
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da tabela.
     *
     * @param name o nome da tabela
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna a lista de colunas que compõem a tabela.
     *
     * @return lista de {@link ColumnModel} representando as colunas
     *         da tabela
     */
    public List<ColumnModel> getColumns() {
        return columns;
    }

    /**
     * Define a lista de colunas que compõem a tabela.
     *
     * @param columns lista de {@link ColumnModel} representando as
     *                colunas da tabela
     */
    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    }

    /**
     * Retorna a lista de relacionamentos (chaves estrangeiras) que a
     * tabela possui com outras tabelas.
     *
     * @return lista de {@link RelationshipModel} representando os
     *         relacionamentos da tabela
     */
    public List<RelationshipModel> getRelationships() {
        return relationships;
    }

    /**
     * Define a lista de relacionamentos (chaves estrangeiras) que a
     * tabela possui com outras tabelas.
     *
     * @param relationships lista de {@link RelationshipModel} representando
     *                       os relacionamentos da tabela
     */
    public void setRelationships(List<RelationshipModel> relationships) {
        this.relationships = relationships;
    }

    /**
     * Retorna uma representação textual da tabela, incluindo seu nome,
     * a listagem de todas as suas colunas e, caso existam, os
     * relacionamentos que possui com outras tabelas.
     * <p>
     * Este formato é utilizado por {@link DatabaseModel#toString()} e,
     * consequentemente, por {@link builder.PromptBuilder}, para compor
     * a descrição completa do schema enviada à IA.
     *
     * @return representação textual da tabela, suas colunas e
     *         relacionamentos
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tabela: ").append(name).append("\n");
        sb.append("Colunas:\n");
        for (ColumnModel c : columns) {
            sb.append("  - ").append(c.toString()).append("\n");
        }
        if (!relationships.isEmpty()) {
            sb.append("Relacionamentos:\n");
            for (RelationshipModel r : relationships) {
                sb.append("  - ").append(r.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}