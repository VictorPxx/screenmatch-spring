package br.com.alura.screenmatch.model;

import java.io.FileWriter;
import java.io.IOException;

public class EscreveArquivo {
    public void escrever (String nomeArquivo, String conteudo) {
        try (FileWriter escreve = new FileWriter(nomeArquivo)) {
            escreve.write(conteudo);
            System.out.println("Arquivo escrito com sucesso");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo");
        }
    }
}
