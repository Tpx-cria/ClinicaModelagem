package br.edu.grupo.clinica;

import desmoj.core.simulator.Experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static String getArgConfigPath(String[] args) {
        if (args == null) return null;
        for (String a : args) {
            if (a != null && a.startsWith("--config=")) {
                return a.substring("--config=".length());
            }
        }
        return null;
    }

    private static boolean getBool(Properties p, String k, boolean d) {
        String v = p.getProperty(k);
        return (v == null) ? d : Boolean.parseBoolean(v.trim());
    }

    private static int getInt(Properties p, String k, int d) {
        String v = p.getProperty(k);
        return (v == null) ? d : Integer.parseInt(v.trim());
    }

    private static long getLong(Properties p, String k, long d) {
        String v = p.getProperty(k);
        return (v == null) ? d : Long.parseLong(v.trim());
    }

    private static double getDouble(Properties p, String k, double d) {
        String v = p.getProperty(k);
        return (v == null) ? d : Double.parseDouble(v.trim());
    }

    private static Properties loadConfig(String[] args) {
        Properties props = new Properties();
        String path = getArgConfigPath(args);
        try {
            if (path == null) {
                // tenta config.properties no CWD, senão app/config.properties
                File f1 = new File("config.properties");
                File f2 = new File("app/config.properties");
                if (f1.exists()) path = f1.getPath();
                else if (f2.exists()) path = f2.getPath();
            }
            if (path != null) {
                try (InputStream in = new FileInputStream(path)) {
                    props.load(in);
                    System.out.println("Config carregada de: " + path);
                }
            } else {
                System.out.println("Nenhum arquivo de config encontrado. Usando defaults embutidos.");
            }
        } catch (Exception e) {
            System.out.println("Falha ao ler config: " + e.getMessage());
            System.out.println("Usando defaults embutidos.");
        }
        return props;
    }

    public static void main(String[] args) {
        Properties cfg = loadConfig(args);

        int R = getInt(cfg, "sim.replicacoes", 30);
        double duracao = getDouble(cfg, "sim.duracaoMin", 600.0);
        long seedBase = getLong(cfg, "seed.base", 12345789L);
        boolean showReport = getBool(cfg, "relatorio.showReport", true);
        boolean showTrace = getBool(cfg, "relatorio.showTrace", true);

        for (int r = 1; r <= R; r++) {
            long seedReplica = seedBase + 1000L * r; // muda por réplica

            ClinicaModel model = new ClinicaModel(null, "Clinica", showReport, showTrace);

            model.seedBase = seedReplica;
            model.duracaoJornada = duracao;

            model.consultoriosAbertos = getInt(cfg, "modelo.consultorios", 1);

            model.chegadaMedia = getDouble(cfg, "chegada.media", 15.0);
            model.probUrgentes = getDouble(cfg, "mix.urgente", 0.30);
            model.muNaoUrgente = getDouble(cfg, "servico.nurg.media", 20.0);
            model.sdNaoUrgente = getDouble(cfg, "servico.nurg.desvio", 5.0);
            model.muUrgente = getDouble(cfg, "servico.urg.media", 10.0);
            model.sdUrgente = getDouble(cfg, "servico.urg.desvio", 3.0);

            model.filaUnica = getBool(cfg, "cenario.filaUnica", false);
            model.prioridadeUrgente = getBool(cfg, "cenario.prioridadeUrgente", false);
            model.triagem = getBool(cfg, "cenario.triagem", false);

            Experiment exp = new Experiment("Clinica_rep" + r);
            model.connectToExperiment(exp);

            exp.stop(new desmoj.core.simulator.TimeInstant(duracao));
            if (showTrace) {
                exp.tracePeriod(new desmoj.core.simulator.TimeInstant(0), new desmoj.core.simulator.TimeInstant(duracao));
                exp.debugPeriod(new desmoj.core.simulator.TimeInstant(0), new desmoj.core.simulator.TimeInstant(Math.min(60.0, duracao)));
            }

            exp.start();
            exp.report();
            exp.finish();
        }
    }
}
