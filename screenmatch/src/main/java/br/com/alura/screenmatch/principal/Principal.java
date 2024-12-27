package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.EscreveArquivo;
import br.com.alura.screenmatch.service.ConsumirApi;
import br.com.alura.screenmatch.service.ConverteDados;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Principal {

    Dotenv dotenv = Dotenv.load();
    private Scanner scanner = new Scanner(System.in);
    private ConsumirApi consumo = new ConsumirApi();
    private ConverteDados conversor = new ConverteDados();
    private EscreveArquivo escreveArquivo = new EscreveArquivo();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String apiKey = dotenv.get("API_KEY");

    public void exibeMenu() {
        System.out.println("Digite o nome da série: ");
        var nomeSerie = scanner.nextLine().replace(" ", "+").trim().toLowerCase();
        var json = consumo.obterDados(ENDERECO + nomeSerie + "&apikey=" + apiKey);
        var dadosSerie = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas() ; i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie + "&season=" + i + "&apikey=" + apiKey );
            var dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        var dadosConvertidosJson = conversor.converteParaJson(temporadas);
		escreveArquivo.escrever( nomeSerie.replace("+", "-") + ".json", dadosConvertidosJson);
    }
}
