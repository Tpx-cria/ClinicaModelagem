package br.edu.grupo.clinica;


import desmoj.core.simulator.*; // classes (Model, ProcessQueue, tipo o arena)
import co.paralleluniverse.fibers.SuspendExecution; // necessário para usar SimProcess por exemplo hold e passivate


public class Paciente extends SimProcess { // paciente é um processo que se ativo executa o script do lifeCycle que o mesmo é executado ao longo do tempo de simulação
    private final boolean urgente; // se é urgente ou não

    public Paciente(ClinicaModel owner, String name, boolean showInTrace, boolean urgente) { // construtor
        super(owner, name, showInTrace); // chama o construtor da superclasse SimProcess
        this.urgente = urgente; // define se é urgente ou não
    }

    @Override
    public void lifeCycle() throws SuspendExecution  { // o que o paciente faz ao longo do tempo de simulação
        ClinicaModel m = (ClinicaModel) getModel(); // pega o clinicaModel para usar as distribuições e variáveis do modelo

        int idx = 0; // Escolhe a fila mais curta como ta descrito no projeto
        for (int i = 1; i < m.filas.length; i++) { //retorna o numero de processos esperando na fila   
            if (m.filas[i].length() < m.filas[idx].length()) idx = i; // se a fila i for menor que a fila idx atualiza idx para i
        }
        // isso aqui tem que explicar melhor, supunhetamos 4 filas com comprimentos [3, 5, 2, 4] o loop escolhe idx = 2 (a de tamanho 2) assim tende a balancear as filas e reduzir a espera

        TimeInstant inicioEspera = presentTime(); // marca o tempo de início da espera
        m.filas[idx].insert(this); // entra na fila escolhida
        passivate(); // aguarda ser atendido se suspende e não roda mais até alguém reativá-lo com activate() ou reactivate()
        //como não tem ninguem puxando ele fica parado para semprepor isso que o report.html vai mostrar insufficient data em TempoEspera e avg.Wait = 0

        double espera = presentTime().getTimeAsDouble() - inicioEspera.getTimeAsDouble(); // calcula o tempo de espera quando for reativado a A espera é tempo_atual - tempo_de_entrada_na_fila
        m.tempoEspera.update(espera); // atualiza a estatística do tempo de espera no relatorio

        // duração de atendimento
        double dur = urgente ? m.distAtendimentoUrgente.sample() : m.distAtendimentoNaoUrgente.sample(); // se for urgente usa a distribuição de urgentes senão a de não urgentes aqui ta só segurando o paciente por um tempo para simular ele sendo atendido
        hold(new TimeSpan(dur)); // espera o tempo de atendimento
    }
}