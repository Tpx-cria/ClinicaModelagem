package br.edu.grupo.clinica;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

public class Triagem extends SimProcess {
    
    private final ClinicaModel m;
    
    public Triagem(ClinicaModel owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        this.m = owner;
    }
    
    @Override
    public void lifeCycle() throws SuspendExecution {
        while (true) {
            if (m.filaTriagem.isEmpty()) {
                passivate();
                continue;
            }
            
            Paciente paciente = m.filaTriagem.first();
            m.filaTriagem.remove(paciente);
            hold(new TimeSpan(4.0));
            
            // Reativa o paciente
            if (!paciente.isScheduled()) {
                paciente.activate();
            }
        }
    }
}