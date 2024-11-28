package br.com.lumah;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/")
public class OMDBServlet extends HttpServlet {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OMDB_API_KEY");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String movieName = req.getParameter("name");
        String resultHtml = null;

        if (movieName != null && !movieName.isEmpty()) {
            // Codifica a string para uso seguro na URL
            String encodedMovieName = URLEncoder.encode(movieName, StandardCharsets.UTF_8);
            String omdbApiUrl = String.format("http://www.omdbapi.com/?s=%s&apikey=%s", encodedMovieName, API_KEY);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(omdbApiUrl);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        String jsonResponse = EntityUtils.toString(response.getEntity());
                        resultHtml = processJsonResponse(jsonResponse);
                    } else {
                        resultHtml = "<p>Erro ao consultar a API do OMDB. Verifique o nome do filme ou a chave de API.</p>";
                    }
                }
            } catch (Exception e) {
                resultHtml = "<p>Erro interno: " + e.getMessage() + "</p>";
            }
        }

        // Gerar página HTML
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(buildHtmlPage(movieName, resultHtml));
    }

    private String processJsonResponse(String jsonResponse) {
        StringBuilder html = new StringBuilder();
        JSONObject json = new JSONObject(jsonResponse);

        if (json.has("Search")) {
            JSONArray movies = json.getJSONArray("Search");
            html.append("<div class='movies-container'>");

            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                String title = movie.getString("Title");
                String poster = movie.getString("Poster");

                // Adiciona cada filme à grade
                html.append("<div class='movie-item'>");
                html.append("<a href='/movieglota/details?id=").append(movie.getString("imdbID")).append("'>");
                html.append("<img src='").append(poster).append("' alt='Poster' class='movie-poster'>");
                html.append("</a>");
                html.append("<a href='/movieglota/details?id=").append(movie.getString("imdbID")).append("'>");
                html.append("<h3 class='movie-title'>").append(title).append("</h3>");
                html.append("</a>");
                html.append("</div>");
            }
            html.append("</div>");
        } else {
            html.append("<p>Nenhum filme encontrado.</p>");
        }

        return html.toString();
    }

    private String buildHtmlPage(String movieName, String resultHtml) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>MovieGlota</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        html.append("form { margin-bottom: 20px; }");
        html.append(".movies-container { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; }");
        html.append(".movie-item { text-align: center; width: 150px; }");
        html.append(".movie-poster { width: 100%; height: auto; border-radius: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".movie-title { font-size: 14px; margin-top: 10px; color: #333; }");
        html.append(".history { margin-top: 20px; padding: 10px; border: 1px solid #ccc; background-color: #fff; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>MovieGlota - Busca de Filmes</h1>");
        html.append("<form method='get' id='search-form'>");
        html.append("<label for='name'>Digite o nome do filme:</label><br>");
        html.append("<input type='text' id='name' name='name' value='" + (movieName != null ? movieName : "") + "' required>");
        html.append("<button type='submit'>Buscar</button>");
        html.append("</form>");

        if (resultHtml != null) {
            html.append("<div class='result'>");
            html.append("<h2>Resultados:</h2>");
            html.append(resultHtml);
            html.append("</div>");
        }

        html.append("<div class='history'>");
        html.append("<h2>Histórico de Pesquisas:</h2>");
        html.append("<ul id='search-history'></ul>");
        html.append("</div>");

        html.append("<script>");
        html.append("document.getElementById('search-form').addEventListener('submit', function(e) {");
        html.append("  const nameInput = document.getElementById('name').value;");
        html.append("  let history = JSON.parse(localStorage.getItem('searchHistory')) || [];");
        html.append("  if (!history.includes(nameInput)) {");
        html.append("    history.push(nameInput);");
        html.append("    localStorage.setItem('searchHistory', JSON.stringify(history));");
        html.append("  }");
        html.append("});");

        html.append("window.addEventListener('DOMContentLoaded', function() {");
        html.append("  const history = JSON.parse(localStorage.getItem('searchHistory')) || [];");
        html.append("  const historyList = document.getElementById('search-history');");
        html.append("  history.reverse().forEach(function(item) {");
        html.append("    const li = document.createElement('li');");
        html.append("    li.textContent = item;");
        html.append("    li.style.cursor = 'pointer';;");
        html.append("    li.addEventListener('click', function() {");
        html.append("      document.getElementById('name').value = item;");
        html.append("      document.getElementById('search-form').submit();");
        html.append("  });");
        html.append("    historyList.appendChild(li);");
        html.append("  });");
        html.append("});");
        html.append("</script>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
