package br.edu.grupo.clinica;

import desmoj.core.simulator.*;
import co.paralleluniverse.fibers.SuspendExecution;

public class Consultorio extends SimProcess {

    private final ClinicaModel m;
    private final int idx; // índice deste consultório

    public Consultorio(ClinicaModel owner, String name, boolean showInTrace, int idx) {
        super(owner, name, showInTrace);
        this.m = owner;
        this.idx = idx;
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        while (true) {
            // Se não há ninguém na fila, este consultório hiberna
            if (m.filasUrg[idx].isEmpty() && m.filasComum[idx].isEmpty()) {
                passivate();
                continue;
            }

            // Política de prioridade: atende urgente primeiro, senão comum
            Paciente prox;
            if (!m.filasUrg[idx].isEmpty()) {
                prox = m.filasUrg[idx].first();
                m.filasUrg[idx].remove(prox);
            } else {
                prox = m.filasComum[idx].first();
                m.filasComum[idx].remove(prox);
            }

            // Reativa o paciente para que ele calcule o tempo de espera
            // (ele NÃO fará o hold do atendimento; o consultório fará)
            if (!prox.isScheduled()) {
                prox.activate();
            }

            // Duração do atendimento é decidida pelo tipo do paciente
            double dur = prox.isUrgente()
                    ? m.distAtendimentoUrgente.sample()
                    : m.distAtendimentoNaoUrgente.sample();

            // Simula o atendimento neste consultório
            hold(new TimeSpan(dur));
            // loop continua buscando o próximo
        }
    }
}
