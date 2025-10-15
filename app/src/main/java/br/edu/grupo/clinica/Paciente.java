package br.edu.grupo.clinica;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

public class Paciente extends SimProcess {
    private final boolean urgente;
    private TimeInstant inicioEspera;

    public Paciente(ClinicaModel owner, String name, boolean showInTrace, boolean urgente) {
        super(owner, name, showInTrace);
        this.urgente = urgente;
    }

    public boolean isUrgente() { return urgente; }

    @Override
    public void lifeCycle() throws SuspendExecution {
        ClinicaModel m = (ClinicaModel) getModel();

        // Escolhe a fila COM MENOR COMPRIMENTO (soma urgente+comum)
        int idx = 0;
        int len0 = m.filasUrg[0].length() + m.filasComum[0].length();
        for (int i = 1; i < m.filasUrg.length; i++) {
            int len = m.filasUrg[i].length() + m.filasComum[i].length();
            if (len < len0) { len0 = len; idx = i; }
        }

        // Entra na fila adequada e registra início de espera
        inicioEspera = presentTime();
        if (urgente) m.filasUrg[idx].insert(this);
        else         m.filasComum[idx].insert(this);

        // Se o consultório correspondente estiver dormindo, acorde-o
        if (!m.consultorios[idx].isScheduled()) {
            m.consultorios[idx].activate();
        }

        // Aguarda ser chamado pelo consultório
        passivate();

        // Quando for reativado pelo consultório, mede a espera
        double espera = presentTime().getTimeAsDouble() - inicioEspera.getTimeAsDouble();
        m.tempoEspera.update(espera);

        // Paciente “termina” aqui. O tempo de serviço foi simulado no consultório.
    }
}
