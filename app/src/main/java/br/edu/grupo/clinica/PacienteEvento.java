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
                
                // Cria paciente real para interagir com consultórios
                Paciente pacienteReal = new Paciente(m, getName(), true, urgente);
                pacienteReal.activate();
                break;
                
            // Casos removidos - agora o Paciente real cuida do resto
        }
    }
    
    public boolean isUrgente() { return urgente; }
}