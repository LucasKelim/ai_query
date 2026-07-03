package ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import exception.QueryGenerationException;

/**
 * Implementação de {@link AIProvider} que gera comandos SQL utilizando
 * a API de Chat Completions da OpenAI.
 * <p>
 * Esta classe representa uma das implementações concretas do padrão de
 * projeto <b>Strategy</b> definido por {@link AIProvider}, permitindo
 * que a biblioteca gere SQL através da OpenAI sem que o restante do
 * sistema precise conhecer os detalhes de sua API HTTP.
 * <p>
 * A temperatura da requisição é fixada em {@code 0}, tornando as
 * respostas do modelo mais determinísticas e reduzindo a chance de
 * variações criativas indesejadas na geração do SQL. A resposta bruta
 * retornada pela API passa por uma limpeza ({@link #cleanSQL(String)})
 * para remover eventuais blocos de código markdown, garantindo que o
 * SQL retornado seja puro, sem formatação adicional.
 *
 * @author Seu Nome
 * @see AIProvider
 * @see AIProviderFactory
 */
public class OpenAIProvider implements AIProvider {

    /** URL do endpoint de Chat Completions da API da OpenAI. */
    private static final String URL = "https://api.openai.com/v1/chat/completions";

    /** Chave de API utilizada para autenticação nas requisições à OpenAI. */
    private final String apiKey;

    /** Nome do modelo da OpenAI a ser utilizado (ex.: {@code "gpt-4o-mini"}). */
    private final String model;

    /** Cliente HTTP utilizado para realizar as requisições à API da OpenAI. */
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Cria uma nova instância do provedor OpenAI com a chave de API e o
     * modelo informados.
     *
     * @param apiKey chave de API utilizada para autenticação nas
     *               requisições à OpenAI
     * @param model nome do modelo da OpenAI a ser utilizado
     */
    public OpenAIProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Gera um comando SQL a partir do prompt informado, enviando uma
     * requisição HTTP à API de Chat Completions da OpenAI e extraindo
     * o conteúdo textual da resposta retornada pelo modelo.
     *
     * @param prompt texto completo enviado à IA, contendo as instruções,
     *               o schema do banco de dados e a pergunta do usuário
     * @return o comando SQL gerado pela OpenAI, já limpo de blocos de
     *         código markdown ou qualquer texto adicional
     * @throws QueryGenerationException se a API retornar um status HTTP
     *         diferente de 200 (ex.: chave inválida, sem créditos, cota
     *         excedida), ou se ocorrer qualquer outra falha na
     *         comunicação com a API (erro de rede, resposta em formato
     *         inesperado, etc.)
     */
    @Override
    public String generateSql(String prompt) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", 0);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        body.put("messages", messages);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new QueryGenerationException("OpenAI retornou erro (status " + response.statusCode() + "): " + response.body());
            }
            JSONObject json = new JSONObject(response.body());
            String raw = json.getJSONArray("choices")
                              .getJSONObject(0)
                              .getJSONObject("message")
                              .getString("content");
            return cleanSQL(raw);
        } catch (QueryGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryGenerationException("Erro ao chamar API da OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Remove eventuais blocos de código markdown (
     * {@code ```sql} / {@code ```}) e espaços em branco nas extremidades
     * do texto retornado pela IA, garantindo que apenas o SQL puro seja
     * retornado.
     *
     * @param raw texto bruto retornado pela API da OpenAI
     * @return o SQL limpo, sem marcações de markdown
     */
    private String cleanSQL(String raw) {
        return raw.replaceAll("```sql", "")
                   .replaceAll("```", "")
                   .trim();
    }
}