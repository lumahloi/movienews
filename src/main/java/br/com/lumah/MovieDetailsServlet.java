package br.com.lumah;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/details")
public class MovieDetailsServlet extends HttpServlet {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OMDB_API_KEY");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String imdbID = req.getParameter("id");

        if (imdbID == null || imdbID.isEmpty()) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<p>Erro: ID do filme não fornecido.</p>");
            return;
        }

        String omdbApiUrl = String.format("http://www.omdbapi.com/?i=%s&apikey=%s", imdbID, API_KEY);

        String resultHtml;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(omdbApiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    resultHtml = processJsonResponse(jsonResponse);
                } else {
                    resultHtml = "<p>Erro ao consultar a API do OMDB.</p>";
                }
            }
        } catch (Exception e) {
            resultHtml = "<p>Erro interno: " + e.getMessage() + "</p>";
        }

        // Exibir a página com detalhes do filme
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(buildHtmlPage(resultHtml));
    }

    private String processJsonResponse(String jsonResponse) {
        JSONObject movie = new JSONObject(jsonResponse);
        StringBuilder html = new StringBuilder();

        if (movie.has("Title")) {
            html.append("<h1>").append(movie.getString("Title")).append("</h1>");
            html.append("<img src='").append(movie.getString("Poster")).append("' alt='Poster' style='max-width:200px;'><br>");
            html.append("<p><strong>Ano:</strong> ").append(movie.getString("Year")).append("</p>");
            html.append("<p><strong>Diretor:</strong> ").append(movie.optString("Director", "N/A")).append("</p>");
            html.append("<p><strong>Elenco:</strong> ").append(movie.optString("Actors", "N/A")).append("</p>");
            html.append("<p><strong>Gênero:</strong> ").append(movie.optString("Genre", "N/A")).append("</p>");
            html.append("<p><strong>Sinopse:</strong> ").append(movie.optString("Plot", "N/A")).append("</p>");
            html.append("<p><strong>IMDb Avaliação:</strong> ").append(movie.optString("imdbRating", "N/A")).append("</p>");
        } else {
            html.append("<p>Detalhes do filme não encontrados.</p>");
        }

        return html.toString();
    }

    private String buildHtmlPage(String content) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>Detalhes do Filme</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f9f9f9; }");
        html.append("h1 { color: #333; }");
        html.append("p { font-size: 14px; line-height: 1.6; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append(content);
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
