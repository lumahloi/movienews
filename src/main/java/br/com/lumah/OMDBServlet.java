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

@WebServlet("/omdb")
public class OMDBServlet extends HttpServlet {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("OMDB_API_KEY");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            resp.getWriter().write("A chave da API não foi configurada. Verifique o arquivo .env.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String movieName = req.getParameter("name");

        if (movieName == null || movieName.isEmpty()) {
            resp.getWriter().write("Por favor, forneça o nome de um filme como parâmetro 'name'.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String omdbApiUrl = String.format("http://www.omdbapi.com/?t=%s&apikey=%s", movieName, API_KEY);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(omdbApiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());
                    resp.setContentType("application/json");
                    resp.getWriter().write(jsonResponse);
                } else {
                    resp.getWriter().write("Erro ao consultar a API do OMDB. Verifique o nome do filme ou a chave de API.");
                    resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                }
            }
        } catch (Exception e) {
            resp.getWriter().write("Erro interno: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
