package br.edu.grupo.clinica;

import desmoj.core.dist.*;
import desmoj.core.simulator.*;
import desmoj.core.statistic.Tally;
import desmoj.core.statistic.Count;
import co.paralleluniverse.fibers.SuspendExecution;

public class ClinicaModel extends Model {

    // Parâmetros principais
    public int consultoriosAbertos = 1;
    public double duracaoJornada = 600.0; // minutos

    // Distribuições e parâmetros
    public double chegadaMedia = 15.0;
    public double probUrgentes = 0.30; // mix
    public double muNaoUrgente = 20.0;
    public double sdNaoUrgente = 5.0;
    public double muUrgente = 10.0;
    public double sdUrgente = 3.0;

    // Distribuições
    public ContDistExponential distChegada;
    public ContDistNormal distAtendimentoNaoUrgente;
    public ContDistNormal distAtendimentoUrgente;
    public ContDistUniform u01;
    public long seedBase;


    public boolean filaUnica = false;
    public boolean prioridadeUrgente = false;
    public boolean triagem = false;
    public ProcessQueue<Paciente> filaUnicaQueue;
    public ProcessQueue<Paciente>[] filasPorConsultorio;
    public ProcessQueue<Paciente> filaTriagem;
    public Triagem enfermeiro;

    public Tally tempoEspera;
    public Tally tempoServico;
    public Tally tempoServicoUrgente;
    public Tally tempoServicoNaoUrgente;
    public Tally tempoSistema;
    public Tally tempoSistemaUrgente;
    public Tally tempoSistemaNaoUrgente;
    public Count pacientesAtendidos;
    public Count pacientesAtendidosUrgente;
    public Count pacientesAtendidosNaoUrgente;
    public Count[] atendidosPorConsultorio;
    public Tally[] tempoServicoPorConsultorio;

    public Consultorio[] consultorios;
    public boolean[] servidorOcupado;

    public Tally pLqMaior5FilaUnica;
    public double tempoAcima5FilaUnica;
    public double ultimoMarcadoFilaUnica;
    public boolean acima5FilaUnicaAtivo;

    public ClinicaModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    @Override
    public String description() {
        return "Simulacao de atendimentos ambulatoriais com fila única e dois perfis de pacientes (urgente/nao urgente).";
    }

    @Override
    public void doInitialSchedules() {
        // Escalonar triagem se ativada
        if (triagem) {
            enfermeiro = new Triagem(this, "Triagem", true);
            enfermeiro.activate();
        }
        
        // Escalonar os consultórios (servidores)
        for (int i = 0; i < consultoriosAbertos; i++) {
            consultorios[i] = new Consultorio(this, "Consultorio " + i, true, i);
            consultorios[i].activate();
        }
        // Escalonar gerador de chegadas
        new GeradorChegadas(this, "Gerador de chegadas", true).activate();
        new Finalizador(this, "Finalizador", true).activate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        distChegada = new ContDistExponential(this, "Chegada Pacientes", chegadaMedia, true, true);
        distAtendimentoNaoUrgente = new ContDistNormal(this, "Atendimento nao urgente", muNaoUrgente, sdNaoUrgente, true, true);
        distAtendimentoUrgente    = new ContDistNormal(this, "Atendimento urgente",    muUrgente, sdUrgente, true, true);
        u01 = new ContDistUniform(this, "U[0,1]", 0.0, 1.0, true, true);

        long base = seedBase;
        distChegada.setSeed(base + 11);
        distAtendimentoNaoUrgente.setSeed(base + 22);
        distAtendimentoUrgente.setSeed(base + 33);
        u01.setSeed(base + 44);


        // Fila de triagem
        if (triagem) {
            filaTriagem = new ProcessQueue<>(this, "FilaTriagem", true, true);
        }
        
        if (filaUnica) {
            filaUnicaQueue = new ProcessQueue<>(this, "FilaUnica", true, true);
        } else {
            filasPorConsultorio = new ProcessQueue[consultoriosAbertos];
            for (int i = 0; i < consultoriosAbertos; i++) {
                filasPorConsultorio[i] = new ProcessQueue<>(this, "FilaConsultorio-" + i, true, true);
            }
        }

        // Servidores
        consultorios = new Consultorio[consultoriosAbertos];
        servidorOcupado = new boolean[consultoriosAbertos];

        // Métricas de tempo de espera/serviço/sistema
        tempoEspera = new Tally(this, "TempoEspera", true, true);
        tempoServico = new Tally(this, "TempoServico", true, true);
        tempoServicoUrgente = new Tally(this, "TempoServico_Urgente", true, true);
        tempoServicoNaoUrgente = new Tally(this, "TempoServico_NaoUrgente", true, true);
        tempoSistema = new Tally(this, "TempoSistema", true, true);
        tempoSistemaUrgente = new Tally(this, "TempoSistema_Urgente", true, true);
        tempoSistemaNaoUrgente = new Tally(this, "TempoSistema_NaoUrgente", true, true);
        pacientesAtendidos = new Count(this, "PacientesAtendidos", true, true);
        pacientesAtendidosUrgente = new Count(this, "PacientesAtendidos_Urgente", true, true);
        pacientesAtendidosNaoUrgente = new Count(this, "PacientesAtendidos_NaoUrgente", true, true);

        // Métricas por consultório
        atendidosPorConsultorio = new Count[consultoriosAbertos];
        tempoServicoPorConsultorio = new Tally[consultoriosAbertos];
        for (int i = 0; i < consultoriosAbertos; i++) {
            atendidosPorConsultorio[i] = new Count(this, "Atendidos_Consultorio-" + i, true, true);
            tempoServicoPorConsultorio[i] = new Tally(this, "TempoServico_Consultorio-" + i, true, true);
        }
    }

    public void filaMudouUnica() {
        double now = presentTime().getTimeAsDouble();
        if (acima5FilaUnicaAtivo) {
            tempoAcima5FilaUnica += (now - ultimoMarcadoFilaUnica);
        }
        int l = (filaUnicaQueue != null ? filaUnicaQueue.length() : 0);
        acima5FilaUnicaAtivo = (l > 5);
        ultimoMarcadoFilaUnica = now;
    }
    
    public static class Finalizador extends SimProcess {
        public Finalizador(Model owner, String name, boolean showInTrace) { super(owner, name, showInTrace); }
        @Override
        public void lifeCycle() throws co.paralleluniverse.fibers.SuspendExecution {
            ClinicaModel m = (ClinicaModel) getModel();
            double eps = 1e-6;
            double esperar = Math.max(0.0, m.duracaoJornada - presentTime().getTimeAsDouble() - eps);
            hold(new TimeSpan(esperar));
            double end = m.duracaoJornada - eps;
            if (m.filaUnica) {
                if (m.acima5FilaUnicaAtivo) {
                    m.tempoAcima5FilaUnica += (end - m.ultimoMarcadoFilaUnica);
                }
                double frac = (end > 0.0) ? (m.tempoAcima5FilaUnica / end) : 0.0;
                if (m.pLqMaior5FilaUnica != null) m.pLqMaior5FilaUnica.update(frac);
            }
        }
    }
}
