package br.edu.grupo.clinica;

import desmoj.core.simulator.*;

public class PacienteEvento extends ExternalEvent {
    
    private final boolean urgente;
    private final String acao;
    private TimeInstant tChegada;
    private TimeInstant inicioEspera;
    
    public PacienteEvento(Model owner, String name, boolean showInTrace, boolean urgente, String acao) {
        super(owner, name, showInTrace);
        this.urgente = urgente;
        this.acao = acao;
    }
    
    @Override
    public void eventRoutine() {
        ClinicaModel m = (ClinicaModel) getModel();
        
        switch(acao) {
            case "CHEGADA":
                tChegada = presentTime();
                sendTraceNote("Paciente " + (urgente ? "URGENTE" : "comum") + " chegou");
                
                // Agenda próxima ação
                PacienteEvento entradaFila = new PacienteEvento(m, getName(), true, urgente, "ENTRADA_FILA");
                entradaFila.tChegada = this.tChegada;
                entradaFila.schedule(new TimeInstant(presentTime().getTimeAsDouble() + 0.01));
                break;
                
            case "ENTRADA_FILA":
                inicioEspera = presentTime();
                sendTraceNote("Entrando na fila");
                
                // Agenda atendimento
                PacienteEvento inicioAtend = new PacienteEvento(m, getName(), true, urgente, "INICIO_ATENDIMENTO");
                inicioAtend.tChegada = this.tChegada;
                inicioAtend.inicioEspera = this.inicioEspera;
                inicioAtend.schedule(new TimeInstant(presentTime().getTimeAsDouble() + 0.01));
                break;
                
            case "INICIO_ATENDIMENTO":
                double espera = presentTime().getTimeAsDouble() - inicioEspera.getTimeAsDouble();
                sendTraceNote(String.format("Iniciando atendimento (esperou %.1fmin)", espera));
                
                // Agenda finalização
                double duracao = urgente ? 10.0 : 20.0; // Tempo fixo para exemplo
                PacienteEvento fim = new PacienteEvento(m, getName(), true, urgente, "FIM_ATENDIMENTO");
                fim.tChegada = this.tChegada;
                fim.schedule(new TimeInstant(presentTime().getTimeAsDouble() + duracao));
                break;
                
            case "FIM_ATENDIMENTO":
                double tempoTotal = presentTime().getTimeAsDouble() - tChegada.getTimeAsDouble();
                sendTraceNote(String.format("Paciente %s finalizado (total: %.1fmin)", 
                             urgente ? "URGENTE" : "comum", tempoTotal));
                break;
        }
    }
    
    public boolean isUrgente() { return urgente; }
}