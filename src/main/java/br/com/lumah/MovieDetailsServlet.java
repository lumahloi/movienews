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
            html.append("<ul>");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);
                String title = article.getString("title");
                String url = article.getString("url");
                html.append("<li><a href='").append(url).append("' target='_blank'>").append(title).append("</a></li>");
            }
            html.append("</ul>");
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
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append(movieHtml);
        html.append(newsHtml);
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
