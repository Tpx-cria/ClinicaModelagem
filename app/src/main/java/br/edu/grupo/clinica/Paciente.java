package br.edu.grupo.clinica;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

public class Paciente extends Entity {

    public final TipoPaciente tipo;

    public Paciente(Model owner, String name, boolean showInTrace, TipoPaciente tipo) {
        super(owner, name, showInTrace);
        this.tipo = tipo;
    }
}
