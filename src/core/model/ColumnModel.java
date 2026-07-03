package core.model;

/**
 * Representa uma coluna de uma tabela do banco de dados, contendo suas
 * informações estruturais: nome, tipo, se é chave primária e se aceita
 * valores nulos.
 * <p>
 * Esta classe faz parte do modelo de domínio ({@code core.model}) que
 * representa o schema do banco de dados em memória, sendo utilizada por
 * {@link builder.DatabaseModelBuilder} durante a extração da estrutura
 * do banco e por {@link builder.PromptBuilder} para descrever o schema
 * no prompt enviado à IA.
 *
 * @author Lucas Kelim Thiel
 * @see TableModel
 * @see DatabaseModel
 */
public class ColumnModel {
	
	/** Nome da coluna. */
	private String name;
	
	/** Tipo de dado da coluna, conforme reportado pelo driver JDBC (ex.: {@code VARCHAR}, {@code INT}). */
	private String type;
	
	/** Indica se a coluna faz parte da chave primária da tabela. */
	private boolean primaryKey;
	
	/** Indica se a coluna aceita valores nulos. */
	private boolean nullable;
	
	/**
     * Cria uma nova representação de coluna com as informações
     * estruturais informadas.
     *
     * @param name nome da coluna
     * @param type tipo de dado da coluna
     * @param primaryKey {@code true} se a coluna for parte da chave
     *                   primária da tabela; {@code false} caso contrário
     * @param nullable {@code true} se a coluna aceitar valores nulos;
     *                 {@code false} caso contrário
     */
	public ColumnModel(String name, String type, boolean primaryKey, boolean nullable) {
		setName(name);
		setType(type);
		setPrimaryKey(primaryKey);
		setNullable(nullable);
	}

	/**
     * Retorna o nome da coluna.
     *
     * @return o nome da coluna
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da coluna.
     *
     * @param name o nome da coluna
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna o tipo de dado da coluna.
     *
     * @return o tipo de dado da coluna (ex.: {@code VARCHAR}, {@code INT})
     */
    public String getType() {
        return type;
    }

    /**
     * Define o tipo de dado da coluna.
     *
     * @param type o tipo de dado da coluna
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Indica se a coluna faz parte da chave primária da tabela.
     *
     * @return {@code true} se for chave primária; {@code false} caso
     *         contrário
     */
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	/**
     * Define se a coluna faz parte da chave primária da tabela.
     *
     * @param primaryKey {@code true} se a coluna for chave primária;
     *                   {@code false} caso contrário
     */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
     * Indica se a coluna aceita valores nulos.
     *
     * @return {@code true} se a coluna aceitar valores nulos;
     *         {@code false} caso contrário
     */
	public boolean isNullable() {
		return nullable;
	}

	/**
     * Define se a coluna aceita valores nulos.
     *
     * @param nullable {@code true} se a coluna aceitar valores nulos;
     *                 {@code false} caso contrário
     */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
     * Retorna uma representação textual da coluna, incluindo nome, tipo
     * e marcadores {@code [PK]} e {@code [NOT NULL]} quando aplicável.
     * <p>
     * Este formato é utilizado por {@link TableModel#toString()} e,
     * consequentemente, por {@link builder.PromptBuilder}, para compor
     * a descrição do schema enviada à IA.
     *
     * @return representação textual da coluna
     */
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" ").append(type);
        if (primaryKey) sb.append(" [PK]");
        if (!nullable) sb.append(" [NOT NULL]");
        return sb.toString();
    }
}
