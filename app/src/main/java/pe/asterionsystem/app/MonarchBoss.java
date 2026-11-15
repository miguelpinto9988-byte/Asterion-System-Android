package pe.asterionsystem.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Presentation metadata for the four optional Monarca encounters. */
final class MonarchBoss {
    final String id,name,difficulty,focus,description;
    final int art;
    private MonarchBoss(String id,String name,String difficulty,String focus,
                        String description,int art) {
        this.id=id;this.name=name;this.difficulty=difficulty;this.focus=focus;
        this.description=description;this.art=art;
    }
    static final List<MonarchBoss> ALL=Collections.unmodifiableList(Arrays.asList(
            new MonarchBoss("igris","Igris","INICIACIÓN","Fuerza y control",
                    "Un duelo técnico con movimientos estables y controlados.",R.drawable.monarch_igris),
            new MonarchBoss("beru","Beru","INTERMEDIA","Agilidad y cardio",
                    "Una misión dinámica centrada en ritmo, movilidad y reacción.",R.drawable.monarch_beru),
            new MonarchBoss("baran","Baran","AVANZADA","Tren central y resistencia",
                    "Un ciclo sostenido que prioriza estabilidad y resistencia.",R.drawable.monarch_baran),
            new MonarchBoss("antares","Antares","MONARCA","Cuerpo completo",
                    "El encuentro más variado, adaptado a tu nivel y equipamiento.",R.drawable.monarch_antares)
    ));
    static MonarchBoss byId(String id) {
        for(MonarchBoss boss:ALL)if(boss.id.equals(id))return boss;
        return ALL.get(0);
    }
}
