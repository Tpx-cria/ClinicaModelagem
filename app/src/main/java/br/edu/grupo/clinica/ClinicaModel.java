package br.edu.grupo.clinica;


import desmoj.core.dist.*; // distribuições aleatórias (Exponential, Normal, Uniform)
import desmoj.core.simulator.*; // classes (Model, ProcessQueue, tipo o arena)
import desmoj.core.statistic.Tally; //  estatísticas (média, desvio padrão)

public class ClinicaModel extends Model {

    public int consultoriosAbertos = 4; // sei la da pra entender oque é
    public ContDistExponential distChegada; // chegadas Exponencial(15 min)
    public ContDistNormal distAtendimentoNaoUrgente; // Normal(20, 5)
    public ContDistNormal distAtendimentoUrgente;    // Normal(10, 3)
    public ContDistUniform u01; //30% de urgentes
    public ProcessQueue<Paciente>[] filas; // filas dos consultórios
    public Tally tempoEspera; // estatística do tempo de espera o report.html mostra média, desvio, min, max
 
    public ClinicaModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace); // mostra no relatório nome da clinica, distribuições/filas/tallies, eventos e ativações (estes booleanos vão na linha 39 mosrando todos os caminhos traces)
    }

    @Override
    public String description() {
        return "Simulação de atendimentos na clínica com consultórios e filas."; // texto que vai para o topo do relatório.
    }

    @Override
    public void doInitialSchedules() {
        new GeradorChegadas(this, "GeradorChegadas", true).activate(); // inicia o gerador de chegadas
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        // Distribuições (unidades: minutos)
        distChegada = new ContDistExponential(this, "ChegadaPacientes", 15.0, true, true); // média 15 min, true para aparecer no relatório e true para aparecer no trace
        distAtendimentoNaoUrgente = new ContDistNormal(this, "Atendimento nao Urgente", 20.0, 5.0, true, true); // média 20 min, desvio padrão 5 min, true para aparecer no relatório e true para aparecer no trace
        distAtendimentoUrgente    = new ContDistNormal(this, "Atendimento Urgente",    10.0, 3.0, true, true);// média 10 min, desvio padrão 3 min, true para aparecer no relatório e true para aparecer no trace
        u01 = new ContDistUniform(this, "Distribuição", 0.0, 1.0, true, true); // uniforme entre 0 e 1, true para aparecer no relatório e true para aparecer no trace
        // Filas (uma por consultório)
        filas = new ProcessQueue[consultoriosAbertos]; // array de filas
        for (int i = 0; i < consultoriosAbertos; i++) { // para cada consultório
            filas[i] = new ProcessQueue<>(this, "Fila Consultorio " + i, true, true); // cria a fila com nome "FilaConsultorio i" true para aparecer no relatório e true para aparecer no trace
        }

        tempoEspera = new Tally(this, "TempoEspera", true, true); //coletor de estatística 
    }
}