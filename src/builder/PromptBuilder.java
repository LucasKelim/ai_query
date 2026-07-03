package builder;

import core.model.DatabaseModel;

/**
 * Responsável por montar o prompt textual enviado à API de IA para
 * gerar comandos SQL a partir de perguntas em linguagem natural.
 * <p>
 * Esta classe representa o padrão de projeto <b>Builder</b>: constrói
 * o prompt final passo a passo, através de chamadas encadeadas
 * ({@code withSchema(...).withQuestion(...).build()}), combinando o
 * schema do banco de dados com a pergunta do usuário e um conjunto de
 * instruções fixas que orientam a IA a responder apenas com SQL puro,
 * sem markdown ou texto explicativo.
 *
 * @author Lucas Kelim Thiel
 * @see core.model.DatabaseModel
 */
public class PromptBuilder {

	/** Schema do banco de dados a ser incluído no prompt. */
    private DatabaseModel schema;
    
    /** Pergunta em linguagem natural feita pelo usuário. */
    private String question;

    /**
     * Define o schema do banco de dados que será incluído no prompt,
     * fornecendo à IA o contexto necessário (tabelas, colunas e
     * relacionamentos) para gerar um SQL válido.
     *
     * @param schema modelo do banco de dados extraído previamente
     *               por {@link DatabaseModelBuilder}
     * @return a própria instância de {@code PromptBuilder}, permitindo
     *         encadeamento de chamadas (fluent interface)
     */
    public PromptBuilder withSchema(DatabaseModel schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Define a pergunta em linguagem natural que será traduzida em
     * SQL pela IA.
     *
     * @param question pergunta do usuário, em linguagem natural
     *                 (ex.: {@code "clientes com saldo maior que mil"})
     * @return a própria instância de {@code PromptBuilder}, permitindo
     *         encadeamento de chamadas (fluent interface)
     */
    public PromptBuilder withQuestion(String question) {
        this.question = question;
        return this;
    }

    /**
     * Monta o prompt final, combinando as instruções fixas de formatação,
     * o schema do banco de dados e a pergunta do usuário em um único
     * texto pronto para ser enviado à API de IA.
     *
     * @return o prompt completo, contendo as regras de resposta, a
     *         descrição do schema e a pergunta formulada
     * @throws IllegalStateException se o schema ou a pergunta não
     *         tiverem sido definidos previamente através de
     *         {@link #withSchema(DatabaseModel)} ou
     *         {@link #withQuestion(String)}
     */
    public String build() {
        if (schema == null || question == null) {
            throw new IllegalStateException("Schema e pergunta são obrigatórios para montar o prompt.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Você é um gerador de SQL. Sua ÚNICA tarefa é converter ")
          .append("uma pergunta em linguagem natural para uma query SQL válida.\n\n");
        sb.append("REGRAS OBRIGATÓRIAS:\n");
        sb.append("- Responda APENAS com o código SQL puro.\n");
        sb.append("- NÃO use markdown, NÃO use ```sql, NÃO use ```.\n");
        sb.append("- NÃO escreva explicações, comentários ou texto antes/depois.\n");
        sb.append("- A resposta deve começar diretamente com SELECT, INSERT, UPDATE ou DELETE.\n\n");
        sb.append("Schema do banco de dados:\n");
        sb.append(schema.toString());
        sb.append("\nPergunta: ").append(question);
        sb.append("\nSQL:");

        return sb.toString();
    }
	
}
