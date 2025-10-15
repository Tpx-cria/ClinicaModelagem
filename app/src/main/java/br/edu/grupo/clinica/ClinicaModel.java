package br.edu.grupo.clinica;

import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.Tally;

public class ClinicaModel extends Model {

    public int consultoriosAbertos = 1;

    public ContDistExponential distChegada;
    public ContDistNormal distAtendimentoNaoUrgente;
    public ContDistNormal distAtendimentoUrgente;
    public ContDistUniform u01;
    public long seedBase;

    // Duas filas por consultório: urgente e comum (prioridade simples)
    public ProcessQueue<Paciente>[] filasUrg;
    public ProcessQueue<Paciente>[] filasComum;

    public Tally tempoEspera;

    // Referência aos servidores
    public Consultorio[] consultorios;

    public ClinicaModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    @Override
    public String description() {
        return "Simulação de atendimentos na clínica com consultórios e filas.";
    }

    @Override
    public void doInitialSchedules() {
        // Escalonar os consultórios (servidores)
        for (int i = 0; i < consultoriosAbertos; i++) {
            consultorios[i] = new Consultorio(this, "Consultorio-" + i, true, i);
            consultorios[i].activate();
        }
        // Escalonar gerador de chegadas
        new GeradorChegadas(this, "GeradorChegadas", true).activate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        // Distribuições (minutos)
        distChegada = new ContDistExponential(this, "ChegadaPacientes", 15.0, true, true);
        distAtendimentoNaoUrgente = new ContDistNormal(this, "AtendimentoNaoUrgente", 20.0, 5.0, true, true);
        distAtendimentoUrgente    = new ContDistNormal(this, "AtendimentoUrgente",    10.0, 3.0, true, true);
        u01 = new ContDistUniform(this, "U[0,1]", 0.0, 1.0, true, true);

            // === Sementes explícitas (baseadas no índice de réplica) ===
            long base = seedBase; // passe isso via construtor do modelo
            distChegada.setSeed(base + 11);
            distAtendimentoNaoUrgente.setSeed(base + 22);
            distAtendimentoUrgente.setSeed(base + 33);
            u01.setSeed(base + 44);


        // Filas por consultório e por prioridade
        filasUrg   = new ProcessQueue[consultoriosAbertos];
        filasComum = new ProcessQueue[consultoriosAbertos];
        for (int i = 0; i < consultoriosAbertos; i++) {
            filasUrg[i]   = new ProcessQueue<>(this, "FilaUrgente-" + i, true, true);
            filasComum[i] = new ProcessQueue<>(this, "FilaComum-" + i, true, true);
        }

        // Servidores
        consultorios = new Consultorio[consultoriosAbertos];

        // Métrica de tempo de espera
        tempoEspera = new Tally(this, "TempoEspera", true, true);
    }
}
