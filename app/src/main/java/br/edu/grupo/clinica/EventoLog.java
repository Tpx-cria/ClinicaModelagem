package br.edu.grupo.clinica;

import desmoj.core.simulator.*;

public class EventoLog extends ExternalEvent {
    
    private final String mensagem;
    
    public EventoLog(Model owner, String name, boolean showInTrace, String mensagem) {
        super(owner, name, showInTrace);
        this.mensagem = mensagem;
    }
    
    @Override
    public void eventRoutine() {
        sendTraceNote(mensagem);
    }
}