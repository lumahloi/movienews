
# MovieNews

Encontrar informações sobre filmes e notícias relacionadas é difícil, com dados espalhados em várias plataformas, o que torna a busca demorada e frustrante.

O MovieNews oferece uma plataforma única para pesquisar filmes e acessar notícias atualizadas, melhorando a experiência do usuário com uma interface intuitiva e centralizada.



## Funcionalidades


- Pesquisa de filmes
- Exibir detalhes do filme: exibir informações detalhadas detalhadas sobre os filmes, como sinopse, gênero, título, pôster
- Exibição de Notícias: apresentar notícias relacionadas ao filme
- Histórico de pesquisa de filmes
- Favoritos: adicionar filme aos favoritos
## Stack utilizada

**Front-end:** HTML e CSS

**Back-end:** Java

**IDE:** IntelliJ 

**Frameworks:** Maven, Jetty, Apache
## Demonstração

![](https://i.imgur.com/wkgqSHR.mp4)


## Variáveis de Ambiente

Para rodar esse projeto, você vai precisar adicionar as seguintes variáveis de ambiente no seu .env:

`OMDB_API_KEY = [SUA CHAVE]`

`NEWS_API_KEY= [SUA CHAVE]`

## Rodando localmente

Clone o projeto

```bash
  git clone https://github.com/lumahloi/movienews.git
```

Entre no diretório do projeto

```bash
  cd movieglota
```

Instale as dependências

```bash
  mvn install
```

Inicie o servidor

```bash
  mvn jetty:run
```


## Aprendizados

Foi minha primeira vez utilizando o Intellij e simplesmente adorei a IDE no que concerna Java! Recomendo demais, tem licença educacional.
Além disso, consolidei meus conhecimentos em Java para a Web, criando servlets e manipulando APIs.
Também aproveitei para implementar padrões de projeto, utilizando Singleton e Strategy. Uma lição valiosa para Java que adquiri neste projeto é: não ficar mexendo nas versões das importações e adaptar as dependências às versões!
## Feedback

Se você tiver algum feedback ou dúvida sobre o projeto, não hesite em entrar em contato! :D
