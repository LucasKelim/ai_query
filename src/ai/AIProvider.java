package ai;

/**
 * Define o contrato para provedores de IA capazes de gerar comandos SQL
 * a partir de um prompt em linguagem natural.
 * <p>
 * Esta interface representa o papel de <b>Strategy</b> no padrão de projeto
 * Strategy: permite que a biblioteca troque a implementação concreta do
 * provedor de IA (OpenAI, Gemini, etc.) sem que o restante do sistema
 * (como {@code AIQuery}) precise conhecer os detalhes de cada API específica.
 * <p>
 * Novas integrações com outros serviços de IA devem apenas implementar
 * esta interface, sem exigir alterações nas classes que a consomem
 * (princípio Aberto/Fechado do SOLID).
 *
 * @author Lucas Kelim Thiel
 * @see ai.AIProviderFactory
 */
public interface AIProvider {
	
    /**
     * Gera um comando SQL a partir de um prompt em linguagem natural,
     * enviando a requisição para a API de IA correspondente.
     *
     * @param prompt texto completo enviado à IA, contendo as instruções,
     *               o schema do banco de dados e a pergunta do usuário
     * @return o comando SQL gerado pela IA, já limpo de blocos de código
     *         markdown ou qualquer texto adicional
     * @throws exception.QueryGenerationException se ocorrer falha na
     *         comunicação com a API (erro de rede, autenticação, limite
     *         de requisições excedido, ou resposta em formato inesperado)
     */
	String generateSql(String prompt);
}
