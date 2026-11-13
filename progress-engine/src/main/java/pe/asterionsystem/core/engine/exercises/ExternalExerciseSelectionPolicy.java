package pe.asterionsystem.core.engine.exercises;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Conservative adapter for a separately imported exercise catalogue.
 * Metadata from the upstream dataset has NO validated level or intensity.
 * Unless independently reviewed, entries remain browsing-only and never
 * become workout recommendations.
 */
public final class ExternalExerciseSelectionPolicy {
    private static final Set<String> CATEGORIES=Set.of(
            "tren_superior","tren_central","tren_inferior","movilidad","cardio");

    private ExternalExerciseSelectionPolicy(){ }

    public static final class Candidate {
        public final String id;
        public final String category;
        public final Set<String> requiredEquipment;
        public final int technicalLevel;
        public final boolean reviewed;
        public final boolean supervisionRecommended;

        public Candidate(String id,String category,Set<String> equipment,
                         int level,boolean reviewed,boolean supervision) {
            if(id==null||!id.matches("ext_[0-9]{4}"))
                throw new IllegalArgumentException("ID externo incorrecto");
            if(!CATEGORIES.contains(category))
                throw new IllegalArgumentException("Categoría inválida");
            if(level<0||level>5 || (reviewed&&level==0))
                throw new IllegalArgumentException("Nivel no validado");
            this.id=id;
            this.category=category;
            this.requiredEquipment=Collections.unmodifiableSet(
                    new HashSet<>(Objects.requireNonNull(equipment,"equipment")));
            this.technicalLevel=level;
            this.reviewed=reviewed;
            this.supervisionRecommended=supervision;
        }
    }

    /**
     * Returns only reviewed, equipment-compatible, unsupervised candidates.
     * Shuffling is stable for the same date/filter/rotation irrespective of
     * source array order. No implicit escalation of technical complexity.
     */
    public static List<Candidate> select(
            List<Candidate> source,String category,Set<String> available,
            int maximumLevel,int quantity,LocalDate date,int rotation) {
        Objects.requireNonNull(source,"source");
        Objects.requireNonNull(available,"available");
        Objects.requireNonNull(date,"date");
        if(!CATEGORIES.contains(category))throw new IllegalArgumentException("Categoría");
        if(maximumLevel<1||maximumLevel>3)
            throw new IllegalArgumentException("Solo niveles técnicos revisados 1..3");
        if(quantity<1||quantity>7)
            throw new IllegalArgumentException("Cantidad fuera del rango 1..7");
        if(rotation<0||rotation>999)
            throw new IllegalArgumentException("Variación fuera de rango");
        List<Candidate> eligible=new ArrayList<>();
        HashSet<String> seen=new HashSet<>();
        for(Candidate item:source) {
            if(item==null||!seen.add(item.id))
                throw new IllegalArgumentException("Duplicado o elemento vacío");
            if(!item.reviewed||item.supervisionRecommended
                    ||item.technicalLevel>maximumLevel
                    ||!item.category.equals(category)
                    ||!available.containsAll(item.requiredEquipment))continue;
            eligible.add(item);
        }
        eligible.sort(Comparator.comparing(a->a.id));
        long seed=seed(category+"|"+maximumLevel+"|"+quantity+"|"+date
                +"|"+rotation+"|"+new java.util.TreeSet<>(available));
        Collections.shuffle(eligible,new Random(seed));
        if(eligible.size()>quantity)eligible.subList(quantity,eligible.size()).clear();
        return Collections.unmodifiableList(eligible);
    }

    private static long seed(String input) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(bytes).getLong();
        }catch(NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
