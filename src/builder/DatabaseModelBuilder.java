package builder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.model.ColumnModel;
import core.model.DatabaseModel;
import core.model.RelationshipModel;
import core.model.TableModel;
import exception.SchemaExtractionException;

/**
 * Responsável por construir um {@link DatabaseModel} a partir da estrutura
 * (schema) de um banco de dados relacional, utilizando os metadados
 * expostos pelo driver JDBC via {@link DatabaseMetaData}.
 * <p>
 * Esta classe representa o padrão de projeto <b>Builder</b>: monta um
 * objeto complexo ({@link DatabaseModel}, composto por tabelas, colunas
 * e relacionamentos) passo a passo, isolando a lógica de extração de
 * metadados do restante da biblioteca.
 * <p>
 * Importante: esta classe lê apenas a <b>estrutura</b> do banco (nomes de
 * tabelas, colunas, tipos, chaves primárias e estrangeiras) — nenhum dado
 * armazenado nas tabelas é lido ou exposto, preservando a privacidade das
 * informações do banco do usuário da biblioteca.
 *
 * @author Lucas Kelim Thiel
 * @see core.model.DatabaseModel
 */
public class DatabaseModelBuilder {
	
	/**
     * Extrai o schema completo do banco de dados acessível pela conexão
     * informada, incluindo todas as tabelas, suas colunas e os
     * relacionamentos (chaves estrangeiras) entre elas.
     *
     * @param connection conexão JDBC ativa com o banco de dados do qual
     *                    o schema será extraído
     * @return um {@link DatabaseModel} contendo a representação completa
     *         da estrutura do banco de dados
     * @throws SchemaExtractionException se ocorrer qualquer erro de
     *         acesso ao banco durante a leitura dos metadados
     */
	public DatabaseModel build(Connection connection) {
		try {
            DatabaseMetaData meta = connection.getMetaData();
            String catalog = connection.getCatalog();
            
            String schemaPattern = isPostgres(connection) ? "public" : null; // pode ajustar se usar PostgreSQL com schema específico

            List<TableModel> tables = new ArrayList<>();

            ResultSet tableRs = meta.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"});
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");

                List<ColumnModel> columns = extractColumns(meta, catalog, schemaPattern, tableName);
                List<RelationshipModel> relationships = extractRelationships(meta, catalog, schemaPattern, tableName);

                tables.add(new TableModel(tableName, columns, relationships));
            }
            tableRs.close();

            return new DatabaseModel(tables);

        } catch (SQLException e) {
            throw new SchemaExtractionException("Erro ao extrair schema do banco: " + e.getMessage(), e);
        }
	}
	
	private boolean isPostgres(Connection connection) throws SQLException {
	    return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
	}
	
	/**
     * Extrai todas as colunas de uma tabela específica, incluindo nome,
     * tipo, se é chave primária e se aceita valores nulos.
     *
     * @param meta metadados da conexão JDBC
     * @param catalog catálogo do banco de dados (pode ser {@code null})
     * @param schemaPattern padrão de schema a ser filtrado
     *                       (pode ser {@code null})
     * @param tableName nome da tabela cujas colunas serão extraídas
     * @return lista de {@link ColumnModel} representando as colunas
     *         da tabela informada
     * @throws SQLException se ocorrer erro ao consultar os metadados
     *         das colunas
     */
    private List<ColumnModel> extractColumns(DatabaseMetaData meta, String catalog, String schemaPattern, String tableName) throws SQLException {
    	List<ColumnModel> columns = new ArrayList<>();
    	Set<String> primaryKeys = extractPrimaryKeys(meta, catalog, schemaPattern, tableName);
    	
    	ResultSet colRs = meta.getColumns(catalog, schemaPattern, tableName, "%");
    	while (colRs.next()) {
    		String colName = colRs.getString("COLUMN_NAME");
    		String typeName = colRs.getString("TYPE_NAME");
    		boolean nullable = colRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
    		boolean isPk = primaryKeys.contains(colName);

    		columns.add(new ColumnModel(colName, typeName, isPk, nullable));
    	}
    	colRs.close();
    	return columns;
    }
    
    /**
     * Extrai os nomes das colunas que compõem a chave primária de uma
     * tabela específica.
     *
     * @param meta metadados da conexão JDBC
     * @param catalog catálogo do banco de dados (pode ser {@code null})
     * @param schemaPattern padrão de schema a ser filtrado
     *                       (pode ser {@code null})
     * @param tableName nome da tabela cuja chave primária será extraída
     * @return conjunto com os nomes das colunas que formam a chave
     *         primária da tabela
     * @throws SQLException se ocorrer erro ao consultar os metadados
     *         da chave primária
     */
    private Set<String> extractPrimaryKeys(DatabaseMetaData meta, String catalog, String schemaPattern, String tableName) throws SQLException {
    	Set<String> pks = new HashSet<>();
    	ResultSet pkRs = meta.getPrimaryKeys(catalog, schemaPattern, tableName);
    	while (pkRs.next()) {
    		pks.add(pkRs.getString("COLUMN_NAME"));
    	}
    	pkRs.close();
    	return pks;
    }
    
    /**
     * Extrai os relacionamentos (chaves estrangeiras) que a tabela
     * informada possui em direção a outras tabelas do banco de dados.
     * <p>
     * Utiliza {@link DatabaseMetaData#getImportedKeys}, que retorna
     * apenas as FKs que a própria tabela declara — relacionamentos em
     * que outras tabelas referenciam esta não são incluídos.
     *
     * @param meta metadados da conexão JDBC
     * @param catalog catálogo do banco de dados (pode ser {@code null})
     * @param schemaPattern padrão de schema a ser filtrado
     *                       (pode ser {@code null})
     * @param tableName nome da tabela cujos relacionamentos serão
     *                   extraídos
     * @return lista de {@link RelationshipModel} representando as
     *         chaves estrangeiras da tabela informada
     * @throws SQLException se ocorrer erro ao consultar os metadados
     *         de chaves estrangeiras
     */
    private List<RelationshipModel> extractRelationships(DatabaseMetaData meta, String catalog, String schemaPattern, String tableName) throws SQLException {
    	List<RelationshipModel> relationships = new ArrayList<>();

    	// getImportedKeys = as FKs que ESSA tabela tem apontando pra outras
    	ResultSet fkRs = meta.getImportedKeys(catalog, schemaPattern, tableName);
    	while (fkRs.next()) {
    		String fkColumn = fkRs.getString("FKCOLUMN_NAME");
    		String pkTable = fkRs.getString("PKTABLE_NAME");
    		String pkColumn = fkRs.getString("PKCOLUMN_NAME");

    		relationships.add(new RelationshipModel(fkColumn, pkTable, pkColumn));
    	}
    	fkRs.close();
    	return relationships;
    }
}
