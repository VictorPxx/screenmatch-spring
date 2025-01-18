package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.service.ConsumirApi;
import br.com.alura.screenmatch.service.ConverteDados;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    Dotenv dotenv = Dotenv.load();
    private Scanner scanner = new Scanner(System.in);
    private ConsumirApi consumo = new ConsumirApi();
    private ConverteDados conversor = new ConverteDados();
    private EscreveArquivo escreveArquivo = new EscreveArquivo();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = dotenv.get("API_KEY");

    public void exibeMenu() {
        System.out.println("Digite o nome da série: ");
        var nomeSerie = scanner.nextLine().replace(" ", "+").trim().toLowerCase();
        var json = consumo.obterDados(ENDERECO + nomeSerie + "&apikey=" + API_KEY);
        var dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas() ; i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie + "&season=" + i + "&apikey=" + API_KEY);
            var dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("\nTop 5 episódios");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        System.out.println();

        List<Episodio> episodios = temporadas.stream()
                        .flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numeroTemporada(), d)))
                                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("A partir de que ano você quer ver os episódios?");
        var ano = scanner.nextInt();
        scanner.nextLine();

        LocalDate dataBusca = LocalDate.of(ano,1,1);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada()
                        + " | Episódio: " + e.getTitulo()
                        + " | Data lançamento: " + e.getDataLancamento().format(formatador)
                ));

        Map<Integer, Double> avaliacaoPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        DoubleSummaryStatistics stats = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Média: " + stats.getAverage());
        System.out.println("Melhor episódio: " + stats.getMax());
        System.out.println("Pior episódio: " + stats.getMin());
        System.out.println("Quantidade: " + stats.getCount());

        var dadosConvertidosJson = conversor.converteParaJson(temporadas);
		escreveArquivo.escrever( nomeSerie.replace("+", "-") + ".json", dadosConvertidosJson);
    }
}
