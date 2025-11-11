package br.edu.grupo.clinica;

import desmoj.core.simulator.*; // classes (Model, ProcessQueue)
import co.paralleluniverse.fibers.SuspendExecution; // necessário para SimProcess

public class GeradorChegadas extends SimProcess { // gerador de chegadas
    public GeradorChegadas(Model owner, String name, boolean showInTrace) { // construtor
        super(owner, name, showInTrace);
    }

    @Override
    public void lifeCycle() throws SuspendExecution  {
        ClinicaModel m = (ClinicaModel) getModel();

        while (true) {
            double inter = m.distChegada.sample();
            hold(new TimeSpan(inter));

            boolean urgente = (m.u01.sample() < m.probUrgentes);
            
            PacienteEvento pacienteEvento = new PacienteEvento(m, urgente ? "PacienteUrgente" : "PacienteComum", true, urgente, "CHEGADA");
            pacienteEvento.schedule(new TimeInstant(presentTime().getTimeAsDouble() + 0.01));
        }
    }
}
