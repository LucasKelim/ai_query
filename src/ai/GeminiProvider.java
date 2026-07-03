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
 * a API do Gemini, da Google.
 * <p>
 * Esta classe representa uma das implementações concretas do padrão de
 * projeto <b>Strategy</b> definido por {@link AIProvider}, permitindo
 * que a biblioteca gere SQL através do Gemini sem que o restante do
 * sistema precise conhecer os detalhes de sua API HTTP.
 * <p>
 * A resposta bruta retornada pela API passa por uma limpeza
 * ({@link #cleanSQL(String)}) para remover eventuais blocos de código
 * markdown, garantindo que o SQL retornado seja puro, sem formatação
 * adicional.
 *
 * @author Seu Nome
 * @see AIProvider
 * @see AIProviderFactory
 */
public class GeminiProvider implements AIProvider {

    /** Chave de API utilizada para autenticação nas requisições ao Gemini. */
    private final String apiKey;

    /** Nome do modelo do Gemini a ser utilizado (ex.: {@code "gemini-2.5-flash"}). */
    private final String model;

    /** Cliente HTTP utilizado para realizar as requisições à API do Gemini. */
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Cria uma nova instância do provedor Gemini com a chave de API e o
     * modelo informados.
     *
     * @param apiKey chave de API utilizada para autenticação nas
     *               requisições ao Gemini
     * @param model nome do modelo do Gemini a ser utilizado
     */
    public GeminiProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Gera um comando SQL a partir do prompt informado, enviando uma
     * requisição HTTP à API do Gemini e extraindo o texto de resposta
     * retornado pelo modelo.
     *
     * @param prompt texto completo enviado à IA, contendo as instruções,
     *               o schema do banco de dados e a pergunta do usuário
     * @return o comando SQL gerado pelo Gemini, já limpo de blocos de
     *         código markdown ou qualquer texto adicional
     * @throws QueryGenerationException se a API retornar um status HTTP
     *         diferente de 200 (ex.: chave inválida, cota excedida), ou
     *         se ocorrer qualquer outra falha na comunicação com a API
     *         (erro de rede, resposta em formato inesperado, etc.)
     */
    @Override
    public String generateSql(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"+ model +":generateContent?key=" + apiKey;
        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new QueryGenerationException("Gemini retornou erro (status " + response.statusCode() + "): " + response.body());
            }
            JSONObject json = new JSONObject(response.body());
            String raw = json.getJSONArray("candidates")
                              .getJSONObject(0)
                              .getJSONObject("content")
                              .getJSONArray("parts")
                              .getJSONObject(0)
                              .getString("text");
            return cleanSQL(raw);
        } catch (QueryGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryGenerationException("Erro ao chamar API do Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Remove eventuais blocos de código markdown (
     * {@code ```sql} / {@code ```}) e espaços em branco nas extremidades
     * do texto retornado pela IA, garantindo que apenas o SQL puro seja
     * retornado.
     *
     * @param raw texto bruto retornado pela API do Gemini
     * @return o SQL limpo, sem marcações de markdown
     */
    private String cleanSQL(String raw) {
        return raw.replaceAll("```sql", "")
                   .replaceAll("```", "")
                   .trim();
    }
}