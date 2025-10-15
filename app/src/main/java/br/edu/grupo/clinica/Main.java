package br.edu.grupo.clinica;


import desmoj.core.simulator.Experiment;

public class Main {
    public static void main(String[] args) {
        ClinicaModel model = new ClinicaModel(null, "Clinica", true, true); // os dois booleanos é tudo que for reportável dentro do modelo (filas, distribuições, tallies) aparecem no *_report.html e o showInTrace = true: os eventos/atividades aparecem no *_trace.html tudo isso se quiser fazer uma analise foda no excel ou outro software que esqueci o nome
        Experiment exp = new Experiment("Clinica"); // nome do experimento controla tudo tempo de simulação, relatórios, traces, debug

        model.connectToExperiment(exp); // conecta o modelo ao experimento o DESMO-J chama: model.init() cria distribuições, filas, tallies, model.doInitialSchedules()  agenda processos iniciais o GeradorChegadas, sem essa linha o experimento não vê o modelo

        exp.stop(new desmoj.core.simulator.TimeInstant(600*30)); // tempo total de simulação: 10 horas (600 minutos)
        exp.tracePeriod(new desmoj.core.simulator.TimeInstant(0), new desmoj.core.simulator.TimeInstant(600*30)); //liga o Trace do inicio 0 até 600 fim.
        exp.debugPeriod(new desmoj.core.simulator.TimeInstant(0), new desmoj.core.simulator.TimeInstant(60*30)); // mesma coisa só q debug 

        exp.start(); // inicia a simulação
        exp.report(); // gera os relatórios HTML
        exp.finish(); // finaliza o experimento
    }
}