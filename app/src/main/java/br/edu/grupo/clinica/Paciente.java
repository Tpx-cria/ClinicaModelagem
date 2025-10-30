package br.edu.grupo.clinica;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

public class Paciente extends SimProcess {
    private final boolean urgente;
    private TimeInstant inicioEspera;
    private TimeInstant tChegada;

    public Paciente(ClinicaModel owner, String name, boolean showInTrace, boolean urgente) {
        super(owner, name, showInTrace);
        this.urgente = urgente;
    }

    public boolean isUrgente() { return urgente; }

    @Override
    public void lifeCycle() throws SuspendExecution {
        ClinicaModel m = (ClinicaModel) getModel();

        tChegada = presentTime();
        
        if (m.triagem) {
            m.filaTriagem.insert(this);
            if (!m.enfermeiro.isScheduled()) {
                m.enfermeiro.activate();
            }
            passivate();
        }

        // Entra na fila adequada e registra início de espera
        inicioEspera = presentTime();
        if (m.filaUnica) {
            m.filaUnicaQueue.insert(this);
            m.filaMudouUnica();
            for (int i = 0; i < m.consultorios.length; i++) {
                if (!m.consultorios[i].isScheduled()) {
                    m.consultorios[i].activate();
                }
            }
        } else {
            double muMix = m.probUrgentes * m.muUrgente + (1.0 - m.probUrgentes) * m.muNaoUrgente;
            double best = Double.POSITIVE_INFINITY;
            int bestIdx = 0;
            for (int i = 0; i < m.consultoriosAbertos; i++) {
                int len = m.filasPorConsultorio[i].length();
                double est = (m.servidorOcupado[i] ? muMix : 0.0) + len * muMix;
                if (est < best) { best = est; bestIdx = i; }
            }
            m.filasPorConsultorio[bestIdx].insert(this);
            if (!m.consultorios[bestIdx].isScheduled()) m.consultorios[bestIdx].activate();
        }

        // Aguarda ser chamado pelo consultório
        passivate();

        // Quando for reativado pelo consultório, mede a espera
        double espera = presentTime().getTimeAsDouble() - inicioEspera.getTimeAsDouble();
        m.tempoEspera.update(espera);

        // Paciente termina aqui. O tempo de serviço foi simulado no consultório.
    }

    public double getChegadaTime() {
        return tChegada != null ? tChegada.getTimeAsDouble() : 0.0;
    }
}
