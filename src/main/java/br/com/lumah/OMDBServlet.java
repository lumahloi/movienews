package br.com.lumah;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/")
public class OMDBServlet extends HttpServlet {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OMDB_API_KEY");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String movieName = req.getParameter("name");
        String result = null;

        if (movieName != null && !movieName.isEmpty()) {
            String omdbApiUrl = String.format("http://www.omdbapi.com/?t=%s&apikey=%s", movieName, API_KEY);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(omdbApiUrl);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        result = EntityUtils.toString(response.getEntity());
                    } else {
                        result = "Erro ao consultar a API do OMDB. Verifique o nome do filme ou a chave de API.";
                    }
                }
            } catch (Exception e) {
                result = "Erro interno: " + e.getMessage();
            }
        }

        // Gerar página HTML
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(buildHtmlPage(movieName, result));
    }

    private String buildHtmlPage(String movieName, String result) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<title>MovieGlota</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }");
        html.append("form { margin-bottom: 20px; }");
        html.append(".result { margin-top: 20px; padding: 10px; border: 1px solid #ccc; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>MovieGlota - Busca de Filmes</h1>");
        html.append("<form method='get'>");
        html.append("<label for='name'>Digite o nome do filme:</label><br>");
        html.append("<input type='text' id='name' name='name' value='" + (movieName != null ? movieName : "") + "' required>");
        html.append("<button type='submit'>Buscar</button>");
        html.append("</form>");

        if (result != null) {
            html.append("<div class='result'>");
            html.append("<h2>Resultado:</h2>");
            html.append("<pre>").append(result).append("</pre>");
            html.append("</div>");
        }

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
