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

@WebServlet("/details")
public class MovieDetailsServlet extends HttpServlet {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String OMDB_API_KEY = dotenv.get("OMDB_API_KEY");
    private static final String NEWS_API_KEY = dotenv.get("NEWS_API_KEY");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String imdbID = req.getParameter("id");

        if (imdbID == null || imdbID.isEmpty()) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<p>Erro: ID do filme não fornecido.</p>");
            return;
        }

        String omdbApiUrl = String.format("http://www.omdbapi.com/?i=%s&apikey=%s", imdbID, OMDB_API_KEY);
        String movieHtml = null;
        String newsHtml = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Busca os detalhes do filme
            HttpGet movieRequest = new HttpGet(omdbApiUrl);
            try (CloseableHttpResponse movieResponse = httpClient.execute(movieRequest)) {
                if (movieResponse.getStatusLine().getStatusCode() == 200) {
                    String movieResponseJson = EntityUtils.toString(movieResponse.getEntity());
                    movieHtml = processMovieJson(movieResponseJson);
                    String movieTitle = new JSONObject(movieResponseJson).getString("Title");

                    // Busca notícias relacionadas ao título do filme
                    newsHtml = fetchNewsForMovie(movieTitle);
                } else {
                    movieHtml = "<p>Erro ao consultar a API do OMDB.</p>";
                }
            }
        } catch (Exception e) {
            movieHtml = "<p>Erro interno: " + e.getMessage() + "</p>";
        }

        // Exibir a página com detalhes do filme e as notícias
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(buildHtmlPage(movieHtml, newsHtml));
    }

    private String buildHeader() {
        StringBuilder html = new StringBuilder();

        html.append("<header style='background: #333; color: #fff; padding: 10px 20px; display: flex; align-items: center;'>");
        html.append("<h1 style='margin: 0; flex: 1;'><a href='/movienews/' style='color: #fff; text-decoration: none;'>MovieGlota</a></h1>");
        html.append("<form id='search-form' method='get' style='flex: 2; position: relative;'>");
        html.append("<input type='text' id='name' name='name' placeholder='Digite o nome do filme...' style='width: 100%; padding: 10px; border-radius: 5px; border: 1px solid #ccc;'>");
        html.append("<div id='search-history' style='display: none; position: absolute; top: 100%; left: 0; right: 0; background: #fff; border: 1px solid #ccc; max-height: 200px; overflow-y: auto; z-index: 10;'></div>");
        html.append("</form>");
        html.append("</header>");

        // Adicionando script JavaScript para comportamento do histórico
        html.append("<script>");
        html.append("document.getElementById('name').addEventListener('focus', function() {");
        html.append("  const history = JSON.parse(localStorage.getItem('searchHistory')) || [];");
        html.append("  const historyDiv = document.getElementById('search-history');");
        html.append("  historyDiv.innerHTML = history.map(item => `<div style='padding: 5px; cursor: pointer;'>${item}</div>`).join('');");
        html.append("  historyDiv.style.display = history.length ? 'block' : 'none';");
        html.append("  Array.from(historyDiv.children).forEach(child => {");
        html.append("    child.addEventListener('click', function() {");
        html.append("      document.getElementById('name').value = this.textContent;");
        html.append("      document.getElementById('search-form').submit();");
        html.append("    });");
        html.append("  });");
        html.append("});");
        html.append("document.getElementById('name').addEventListener('blur', function() {");
        html.append("  setTimeout(() => { document.getElementById('search-history').style.display = 'none'; }, 200);");
        html.append("});");
        html.append("</script>");

        return html.toString();
    }


    private String fetchNewsForMovie(String movieTitle) {
        String newsApiUrl = String.format("https://newsapi.org/v2/everything?q=%s&apiKey=%s",
                movieTitle.replace(" ", "%20"), NEWS_API_KEY);
        StringBuilder newsHtml = new StringBuilder();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet newsRequest = new HttpGet(newsApiUrl);
            try (CloseableHttpResponse newsResponse = httpClient.execute(newsRequest)) {
                if (newsResponse.getStatusLine().getStatusCode() == 200) {
                    String newsResponseJson = EntityUtils.toString(newsResponse.getEntity());
                    newsHtml.append(processNewsJson(newsResponseJson));
                } else {
                    newsHtml.append("<p>Erro ao buscar notícias.</p>");
                }
            }
        } catch (Exception e) {
            newsHtml.append("<p>Erro interno ao buscar notícias: ").append(e.getMessage()).append("</p>");
        }

        return newsHtml.toString();
    }

    private String processMovieJson(String jsonResponse) {
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

    private String processNewsJson(String jsonResponse) {
        JSONObject newsJson = new JSONObject(jsonResponse);
        StringBuilder html = new StringBuilder();

        if (newsJson.has("articles")) {
            JSONArray articles = newsJson.getJSONArray("articles");
            html.append("<h2>Notícias Relacionadas:</h2>");
            html.append("<div class='news-container'>"); // Contêiner para a grade de cards

            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                String title = article.getString("title");
                String url = article.getString("url");
                String source = article.getJSONObject("source").getString("name");
                String description = article.optString("description", "Sem descrição disponível.");
                String publishedAt = article.optString("publishedAt", "Data desconhecida").substring(0, 10); // Apenas a data

                html.append("<div class='news-card'>");
                html.append("<h3 class='news-title'><a href='").append(url).append("' target='_blank'>").append(title).append("</a></h3>");
                html.append("<p class='news-source'>Fonte: ").append(source).append("</p>");
                html.append("<p class='news-date'>Publicado em: ").append(publishedAt).append("</p>");
                html.append("<p class='news-description'>").append(description).append("</p>");
                html.append("</div>");
            }

            html.append("</div>"); // Fechar contêiner
        } else {
            html.append("<p>Nenhuma notícia encontrada.</p>");
        }

        return html.toString();
    }

    private String buildHtmlPage(String movieHtml, String newsHtml) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>Detalhes do Filme</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; padding: 20px; background-color: #f9f9f9; }");
        html.append("h1, h2 { color: #333; }");
        html.append("p { font-size: 14px; line-height: 1.6; }");
        html.append(".news-container { display: flex; flex-wrap: wrap; gap: 20px; margin-top: 20px; }");
        html.append(".news-card { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 15px; width: 300px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }");
        html.append(".news-card h3 { margin-top: 0; font-size: 16px; color: #0073e6; }");
        html.append(".news-card p { margin: 5px 0; color: #555; }");
        html.append(".news-card .news-title a { text-decoration: none; color: #0073e6; }");
        html.append(".news-card .news-title a:hover { text-decoration: underline; }");
        html.append(".news-card .news-source { font-weight: bold; }");
        html.append(".news-card .news-date { font-style: italic; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append(buildHeader());
        html.append(movieHtml);
        html.append(newsHtml);
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
