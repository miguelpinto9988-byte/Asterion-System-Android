package pe.asterionsystem.core.engine.exercises;

import java.util.Map;
import java.util.Objects;

/**
 * Editorial, non-clinical body-focus labels for every v1 exercise family.
 * Primary/secondary describe broad functional emphasis, not exact muscle
 * recruitment, a medical assessment or an exercise prescription.
 *
 * Family IDs and exercise database schemas stay unchanged.
 */
public final class ExerciseMuscleFocus {
    public record Focus(String primary,String secondary) {
        public Focus {
            Objects.requireNonNull(primary,"primary");
            Objects.requireNonNull(secondary,"secondary");
        }
        public String key() { return primary+"|"+secondary; }
        public String description() {
            return primary+(secondary.isEmpty()?"":" · apoyo: "+secondary);
        }
    }

    private static final Map<String,Focus> FOCUS=Map.ofEntries(
            Map.entry("sentadillas",new Focus("muslos","cadera")),
            Map.entry("zancadas",new Focus("muslos","cadera")),
            Map.entry("bisagra_cadera",new Focus("cadera","muslos posteriores")),
            Map.entry("puentes_cadera",new Focus("cadera","tronco")),
            Map.entry("subida_escalon",new Focus("muslos","cadera")),
            Map.entry("pantorrillas",new Focus("pantorrillas","tobillos")),
            Map.entry("flexion_rodilla",new Focus("muslos posteriores","rodilla")),
            Map.entry("control_cadera",new Focus("cadera","muslos")),
            Map.entry("extension_rodilla",new Focus("muslos anteriores","rodilla")),
            Map.entry("empuje_horizontal",new Focus("pectoral","tríceps")),
            Map.entry("empuje_vertical",new Focus("hombros","tríceps")),
            Map.entry("aperturas_pecho",new Focus("pectoral","hombros")),
            Map.entry("extension_codo",new Focus("tríceps","hombros")),
            Map.entry("traccion_horizontal",new Focus("espalda","bíceps")),
            Map.entry("traccion_vertical",new Focus("espalda","bíceps")),
            Map.entry("flexion_codo",new Focus("bíceps","antebrazo")),
            Map.entry("elevaciones_hombro",new Focus("hombros","escápulas")),
            Map.entry("escapulas",new Focus("escápulas","espalda")),
            Map.entry("rotacion_hombro",new Focus("hombros","escápulas")),
            Map.entry("extension_espalda",new Focus("zona lumbar","tronco")),
            Map.entry("core_antextension",new Focus("abdomen","tronco")),
            Map.entry("core_anti_rotacion",new Focus("oblicuos","tronco")),
            Map.entry("core_flexion",new Focus("abdomen","cadera")),
            Map.entry("core_rotacion",new Focus("oblicuos","espalda")),
            Map.entry("equilibrio",new Focus("equilibrio","piernas")),
            Map.entry("transportes",new Focus("agarre","tronco")),
            Map.entry("caminata",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("carrera",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("bicicleta",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("remo_cardio",new Focus("cardiorrespiratorio","espalda")),
            Map.entry("eliptica",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("escalon_aerobico",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("coordinacion",new Focus("coordinación","cuerpo completo")),
            Map.entry("movilidad_cadera",new Focus("movilidad","cadera")),
            Map.entry("movilidad_columna",new Focus("movilidad","columna")),
            Map.entry("movilidad_hombro",new Focus("movilidad","hombros")),
            Map.entry("movilidad_tobillo",new Focus("movilidad","tobillos")),
            Map.entry("flexibilidad_piernas",new Focus("flexibilidad","piernas")),
            Map.entry("respiracion_postura",new Focus("respiración","tronco")),
            Map.entry("manos_agarre",new Focus("antebrazo","mano")),
            Map.entry("control_muneca_fuerza",new Focus("antebrazo","muñeca")),
            Map.entry("estabilidad_cuadrupedia",new Focus("tronco","hombros")),
            Map.entry("postura_tronco_sentado",new Focus("tronco","postura")),
            Map.entry("motricidad_pies",new Focus("pies","tobillos")),
            Map.entry("extension_cadera_pie",new Focus("cadera","piernas")),
            Map.entry("movilidad_cervical",new Focus("movilidad","cuello")),
            Map.entry("movilidad_dedos_manos",new Focus("movilidad","mano")),
            Map.entry("pasos_aerobicos_suaves",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("danza_bajo_impacto",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("serrato_escapular",new Focus("escápulas","hombros")),
            Map.entry("press_alternativos",new Focus("pectoral","tríceps")),
            Map.entry("remadas_soportadas",new Focus("espalda","bíceps")),
            Map.entry("antebrazo_rotacional",new Focus("antebrazo","muñeca")),
            Map.entry("coordinacion_brazos_pelota",new Focus("coordinación","hombros")),
            Map.entry("pilates_suelo_inicial",new Focus("tronco","cadera")),
            Map.entry("control_tronco_pie",new Focus("tronco","equilibrio")),
            Map.entry("rotacion_diagonal_tronco",new Focus("oblicuos","espalda")),
            Map.entry("yoga_estabilidad_general",new Focus("tronco","equilibrio")),
            Map.entry("apoyo_lateral_tronco",new Focus("oblicuos","cadera")),
            Map.entry("isometrias_piernas",new Focus("muslos","cadera")),
            Map.entry("abductores_acostado",new Focus("cadera","muslos")),
            Map.entry("deslizamientos_rodilla",new Focus("rodilla","muslos")),
            Map.entry("pasos_direcciones",new Focus("piernas","coordinación")),
            Map.entry("saltos_recreativos",new Focus("piernas","coordinación")),
            Map.entry("flexibilidad_antebrazos",new Focus("flexibilidad","antebrazo")),
            Map.entry("movilidad_torax_pie",new Focus("movilidad","columna torácica")),
            Map.entry("flexibilidad_cadera_suelo",new Focus("flexibilidad","cadera")),
            Map.entry("equilibrio_tandem",new Focus("equilibrio","piernas")),
            Map.entry("yoga_movilidad_restaurativa",new Focus("movilidad","cuerpo completo")),
            Map.entry("natacion_recreativa",new Focus("cardiorrespiratorio","cuerpo completo")),
            Map.entry("danza_recreativa_diversa",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("pelota_deportiva_recreativa",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("cuerda_saltar_recreativa",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("deportes_raqueta_recreativos",new Focus("cardiorrespiratorio","hombros")),
            Map.entry("recorridos_al_aire_libre",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("elevacion_brazos_funcional",new Focus("hombros","brazos")),
            Map.entry("alcances_pared_brazos",new Focus("hombros","escápulas")),
            Map.entry("control_brazos_cruzados",new Focus("hombros","pecho")),
            Map.entry("apoyos_superiores_estables",new Focus("hombros","tronco")),
            Map.entry("destreza_manos_brazos",new Focus("antebrazo","coordinación")),
            Map.entry("alineacion_tronco_suave",new Focus("tronco","postura")),
            Map.entry("alcances_tronco_asistidos",new Focus("tronco","hombros")),
            Map.entry("coordinacion_tronco_pie",new Focus("tronco","equilibrio")),
            Map.entry("respiracion_movimiento_tronco",new Focus("respiración","tronco")),
            Map.entry("control_pelvis_suelo",new Focus("cadera","tronco")),
            Map.entry("movilidad_rodilla_sentada",new Focus("muslos","rodilla")),
            Map.entry("pasos_cortos_funcionales",new Focus("piernas","coordinación")),
            Map.entry("tobillo_control_equilibrio",new Focus("tobillos","equilibrio")),
            Map.entry("cadera_lateral_pie",new Focus("cadera","equilibrio")),
            Map.entry("transiciones_silla_estable",new Focus("muslos","cadera")),
            Map.entry("movilidad_hombros_sentada",new Focus("movilidad","hombros")),
            Map.entry("movilidad_cadera_silla",new Focus("movilidad","cadera")),
            Map.entry("movilidad_rodilla_suave",new Focus("movilidad","rodilla")),
            Map.entry("movilidad_pies_suelo",new Focus("movilidad","pies")),
            Map.entry("movilidad_toracica_silla",new Focus("movilidad","columna torácica")),
            Map.entry("marcha_en_sitio_bajoimpacto",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("pasos_aerobicos_direcciones",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("ritmos_brazos_pasos",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("cardio_desde_silla",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("juegos_pasos_recreativos",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("deporte_empujes_superiores",new Focus("pecho","hombros")),
            Map.entry("deporte_tracciones_superiores",new Focus("espalda","brazos")),
            Map.entry("deporte_estabilidad_hombros",new Focus("hombros","escápulas")),
            Map.entry("deporte_gestos_implementos",new Focus("hombros","coordinación")),
            Map.entry("deporte_control_brazos_prono",new Focus("espalda alta","escápulas")),
            Map.entry("deporte_centro_estabilidad",new Focus("tronco","estabilidad")),
            Map.entry("deporte_centro_rotacion",new Focus("tronco","rotación")),
            Map.entry("deporte_centro_respiracion",new Focus("respiración","tronco")),
            Map.entry("deporte_centro_acuatico",new Focus("tronco","coordinación")),
            Map.entry("deporte_centro_transiciones",new Focus("tronco","cadera")),
            Map.entry("deporte_piernas_potencia",new Focus("piernas","coordinación")),
            Map.entry("deporte_pies_agilidad",new Focus("piernas","agilidad")),
            Map.entry("deporte_fuerza_piernas",new Focus("muslos","cadera")),
            Map.entry("deporte_piernas_pilates",new Focus("cadera","muslos")),
            Map.entry("deporte_piernas_danza",new Focus("piernas","equilibrio")),
            Map.entry("deporte_deslizamientos",new Focus("piernas","equilibrio")),
            Map.entry("deporte_mov_yoga",new Focus("movilidad","columna")),
            Map.entry("deporte_mov_articular",new Focus("movilidad","articulaciones")),
            Map.entry("deporte_mov_equilibrio",new Focus("equilibrio","movilidad")),
            Map.entry("deporte_mov_tecnica",new Focus("movilidad","coordinación")),
            Map.entry("deporte_cardio_carrera",new Focus("cardiorrespiratorio","piernas")),
            Map.entry("deporte_cardio_ritmo",new Focus("cardiorrespiratorio","ritmo")),
            Map.entry("deporte_cardio_agua",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("deporte_cardio_adaptado",new Focus("cardiorrespiratorio","brazos")),
            Map.entry("deporte_cardio_funcional",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("calistenia_suspension",new Focus("brazos","coordinación")),
            Map.entry("gimnasio_maquinas_superior",new Focus("brazos","coordinación")),
            Map.entry("voleibol_toques",new Focus("brazos","coordinación")),
            Map.entry("raqueta_golpes",new Focus("brazos","coordinación")),
            Map.entry("hockey_control_palo",new Focus("brazos","coordinación")),
            Map.entry("balonmano_pases",new Focus("brazos","coordinación")),
            Map.entry("pilates_control_respiracion",new Focus("tronco","coordinación")),
            Map.entry("tronco_antirotacion_deporte",new Focus("tronco","coordinación")),
            Map.entry("tronco_tecnica_remo",new Focus("tronco","coordinación")),
            Map.entry("postura_nadador_en_seco",new Focus("tronco","coordinación")),
            Map.entry("tronco_control_balon",new Focus("tronco","coordinación")),
            Map.entry("tronco_yoga_equilibrio",new Focus("tronco","coordinación")),
            Map.entry("futbol_pases_recepciones",new Focus("piernas","coordinación")),
            Map.entry("gimnasio_maquinas_inferior",new Focus("piernas","coordinación")),
            Map.entry("baloncesto_pivotes",new Focus("piernas","coordinación")),
            Map.entry("atletismo_despegue_control",new Focus("piernas","coordinación")),
            Map.entry("patinaje_apoyos_seco",new Focus("piernas","coordinación")),
            Map.entry("beisbol_pasos_bases",new Focus("piernas","coordinación")),
            Map.entry("movilidad_hombro_raqueta",new Focus("movilidad","coordinación")),
            Map.entry("movilidad_atletismo_pista",new Focus("movilidad","coordinación")),
            Map.entry("movilidad_nadador_hombros",new Focus("movilidad","coordinación")),
            Map.entry("movilidad_patinaje_cadera",new Focus("movilidad","coordinación")),
            Map.entry("movilidad_deportiva_manos",new Focus("movilidad","coordinación")),
            Map.entry("movilidad_danza_yoga",new Focus("movilidad","coordinación")),
            Map.entry("atletismo_cadencia",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("relevos_movimiento",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("futbol_conduccion",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("baloncesto_bote",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("hockey_desplazamientos",new Focus("cardiorrespiratorio","coordinación")),
            Map.entry("acondicionamiento_multideporte",new Focus("cardiorrespiratorio","coordinación"))
    );

    private ExerciseMuscleFocus() { }

    public static boolean knowsFamily(String familyId) {
        return FOCUS.containsKey(familyId);
    }

    public static int classifiedFamilies() { return FOCUS.size(); }

    public static Focus forFamily(String familyId) {
        return FOCUS.getOrDefault(familyId,new Focus("no clasificado",""));
    }

    public static Focus forVariant(ExerciseCatalog catalog,ExerciseVariant variant) {
        Objects.requireNonNull(catalog,"catalog");
        Objects.requireNonNull(variant,"variant");
        return forFamily(catalog.categoryIndex().familyForVariant(variant.id()));
    }
}
