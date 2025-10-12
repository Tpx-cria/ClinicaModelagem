package br.edu.grupo.clinica;

import desmoj.core.simulator.*; // classes (Model, ProcessQueue, tipo o arena)
import co.paralleluniverse.fibers.SuspendExecution; // necessário para usar SimProcess por exemplo hold e passivate

public class GeradorChegadas extends SimProcess { // gerador de chegadas um processo que se ativo executa o script do lifeCycle que o mesmo é executado ao longo do tempo de simulação 
    public GeradorChegadas(Model owner, String name, boolean showInTrace) { // construtor
        super(owner, name, showInTrace); // chama o construtor da superclasse SimProcess
    }

    @Override
    public void lifeCycle() throws SuspendExecution  { // o que o gerador faz ao longo do tempo de simulação
        ClinicaModel m = (ClinicaModel) getModel(); // pega o modelo clinicaModel para usar as distribuições e variáveis do modelo

        while (true) { // o gerador nunca termina por si só quem para a simulação é o Experiment ent por isso o while true
            hold(new TimeSpan(m.distChegada.sample())); //m.distChegada.sample() aleatoriza o intervalo entre chegadas (Exponencial com media 15 min) e o hold(new TimeSpan) faz o gerador esperar esse tempo entre uma chegada e outra se a amostra der 0.0 o hold não espera nada e gera um monte de pacientes de uma vez só

            boolean urgente = (m.u01.sample() < 0.30); // 30% de urgentes u01 é uniforme entre 0 e 1 se der <0.3 é urgente senão não é urgente entende?
            Paciente p = new Paciente(m, urgente ? "PacienteUrgente" : "PacienteComum", true, urgente); // cria o paciente com o nome dependendo se é urgente ou não o true é para aparecer no trace e o ultimo parâmetro diz se é urgente ou não
            p.activate(); // ativa o paciente ou seja coloca ele na fila de eventos futuros para ser executado quando chegar a hora do evento
        }
    }
}
