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
            Paciente prox = null;

            if (m.filaUnica) {
                if (m.filaUnicaQueue.isEmpty()) {
                    passivate();
                    continue;
                }
                prox = m.filaUnicaQueue.first();
                m.filaUnicaQueue.remove(prox);
                m.filaMudouUnica();
            } else {
                if (m.filasPorConsultorio[idx].isEmpty()) {
                    passivate();
                    continue;
                }
                prox = m.filasPorConsultorio[idx].first();
                m.filasPorConsultorio[idx].remove(prox);
            }

            // Reativa o paciente para que ele calcule o tempo de espera
            // (ele não fará o hold do atendimento; o consultório fará)
            if (!prox.isScheduled()) {
                prox.activate();
            }

            double dur;
            if (prox.isUrgente()) {
                dur = m.distAtendimentoUrgente.sample();
            } else {
                dur = m.distAtendimentoNaoUrgente.sample();
            }

            m.servidorOcupado[idx] = true;
            hold(new TimeSpan(dur));
            m.servidorOcupado[idx] = false;
            m.tempoServico.update(dur);
            if (prox.isUrgente()) m.tempoServicoUrgente.update(dur); else m.tempoServicoNaoUrgente.update(dur);
            m.tempoServicoPorConsultorio[idx].update(dur);
            double tempoSistema = presentTime().getTimeAsDouble() - prox.getChegadaTime();
            m.tempoSistema.update(tempoSistema);
            if (prox.isUrgente()) m.tempoSistemaUrgente.update(tempoSistema); else m.tempoSistemaNaoUrgente.update(tempoSistema);
            if (prox.isUrgente()) m.pacientesAtendidosUrgente.update(); else m.pacientesAtendidosNaoUrgente.update();
            m.pacientesAtendidos.update();
            m.atendidosPorConsultorio[idx].update();
            // loop continua buscando o próximo
        }
    }
}
