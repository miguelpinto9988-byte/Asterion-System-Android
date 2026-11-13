package pe.asterionsystem.portable;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;

/** Immutable startup preferences for a different app. No Android dependency. */
public final class PortableTrainingSettings {
    public final List<String> categories;
    public final String experience;
    public final int dailyActivities;
    public final boolean volumeSuggestionsEnabled;
    public final ZoneId zoneId;
    public final String language;

    private PortableTrainingSettings(List<String> categories, String experience,
            int dailyActivities, boolean enabled, ZoneId zone, String language) {
        this.categories=Collections.unmodifiableList(new ArrayList<>(categories));
        this.experience=experience;
        this.dailyActivities=dailyActivities;
        this.volumeSuggestionsEnabled=enabled;
        this.zoneId=zone;
        this.language=language;
    }

    public static PortableTrainingSettings defaults() {return from(new Properties());}

    public static PortableTrainingSettings from(Properties props) {
        Objects.requireNonNull(props,"props");
        String categoryCodes=props.getProperty("categorias_habilitadas",
                String.join(",",ExerciseCategoryMapper.weeklyCodes()));
        LinkedHashSet<String> allowed=new LinkedHashSet<>();
        for(String token:categoryCodes.split(",",-1)) {
            String code=token.trim();
            if(!ExerciseCategoryMapper.weeklyCodes().contains(code)||!allowed.add(code))
                throw new IllegalArgumentException("Categoría inválida/repetida: "+code);
        }
        if(allowed.isEmpty())throw new IllegalArgumentException("Se requiere una categoría");
        String experience=props.getProperty("nivel_experiencia","principiante").trim();
        if(!List.of("principiante","intermedio","avanzado").contains(experience))
            throw new IllegalArgumentException("Experiencia inválida");
        int count=Integer.parseInt(props.getProperty("actividades_diarias","3").trim());
        if(count<1||count>7)throw new IllegalArgumentException("Actividades fuera de 1..7");
        String raw=props.getProperty("sugerencias_volumen","false").trim();
        if(!raw.equals("true")&&!raw.equals("false"))
            throw new IllegalArgumentException("sugerencias_volumen debe ser true o false");
        String zone=props.getProperty("zona_horaria","system").trim();
        ZoneId zoneId=zone.equals("system")?ZoneId.systemDefault():ZoneId.of(zone);
        String lang=props.getProperty("idioma","es").trim();
        if(lang.isEmpty()||lang.length()>20)throw new IllegalArgumentException("Idioma inválido");
        return new PortableTrainingSettings(new ArrayList<>(allowed),experience,count,
                Boolean.parseBoolean(raw),zoneId,lang);
    }
}
