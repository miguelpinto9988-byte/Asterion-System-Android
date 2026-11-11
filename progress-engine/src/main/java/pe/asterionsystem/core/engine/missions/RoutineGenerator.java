package pe.asterionsystem.core.engine.missions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseIntensity;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Six reproducible, optional routine choices from the real, filtered catalog.
 * Time is an estimated available window, not an imposed exercise duration.
 * Even an "avanzado" request never auto-selects technically advanced level 4-5
 * exercises, supervised moves, or high/variable-intensity movements.
 */
public final class RoutineGenerator {
    public static final int OFFER_COUNT=6;
    private final ExerciseCatalog catalog;

    public RoutineGenerator(ExerciseCatalog catalog) {
        this.catalog=Objects.requireNonNull(catalog,"catalog");
    }

    public static final class Request {
        public final String category,level;
        public final int minutes,variation;
        public final LocalDate date;

        public Request(String category,int minutes,String level,LocalDate date,int variation) {
            if(!ExerciseCategoryMapper.weeklyCodes().contains(category))
                throw new IllegalArgumentException("Una de las cinco categorías es obligatoria");
            if(minutes!=10 && minutes!=15 && minutes!=20
                    && minutes!=30 && minutes!=45 && minutes!=60)
                throw new IllegalArgumentException("Duración no admitida");
            if(!"basico".equals(level) && !"intermedio".equals(level)
                    && !"avanzado".equals(level))
                throw new IllegalArgumentException("Nivel de rutina desconocido");
            if(variation<0 || variation>999)
                throw new IllegalArgumentException("Revisión de sorteo fuera de rango");
            this.category=category;
            this.minutes=minutes;
            this.level=level;
            this.date=Objects.requireNonNull(date,"date");
            this.variation=variation;
        }
        public int stepCount() {
            if(minutes<=15)return 3;
            if(minutes<=20)return 4;
            if(minutes<=30)return 5;
            return 6;
        }
        public int maxTechnicalLevel() {
            return "basico".equals(level)?1:"intermedio".equals(level)?2:3;
        }
    }

    public static final class Offer {
        private final String id;
        private final List<ExerciseVariant> exercises;
        private final int minutes;
        private final String category,level;

        Offer(String id,List<ExerciseVariant> exercises,Request request) {
            this.id=id;
            this.exercises=Collections.unmodifiableList(new ArrayList<>(exercises));
            this.minutes=request.minutes;
            this.category=request.category;
            this.level=request.level;
        }
        public String id() { return id; }
        public List<ExerciseVariant> exercises() { return exercises; }
        public int minutes() { return minutes; }
        public String category() { return category; }
        public String level() { return level; }
    }

    public List<Offer> generate(Request request,MissionPlanner.Profile profile) {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(profile,"profile");
        ArrayList<ExerciseVariant> allowed=new ArrayList<>();
        for(ExerciseVariant variant:catalog.categoryIndex().variants(request.category)) {
            ExerciseIntensity intensity=catalog.intensity(variant.id());
            if(!catalog.eligibleForAutomaticSuggestion(variant)
                    ||variant.difficulty()>request.maxTechnicalLevel()
                    ||!profile.equipment.containsAll(variant.equipment())
                    ||(intensity!=ExerciseIntensity.LOW
                       && intensity!=ExerciseIntensity.MODERATE)
                    ||("baja".equals(profile.intensityPreference)
                       && intensity!=ExerciseIntensity.LOW))continue;
            allowed.add(variant);
        }
        allowed.sort(Comparator.comparing(ExerciseVariant::id));
        if(allowed.size()<request.stepCount())return Collections.emptyList();

        long seed=seed("asterion.routines.v1|"+profile.missionSignature()
                +"|"+request.date+"|"+request.category+"|"+request.level
                +"|"+request.minutes+"|"+request.variation);
        ArrayList<Offer> result=new ArrayList<>();
        Set<String> seenAcross=new HashSet<>();
        Set<String> fingerprints=new HashSet<>();
        for(int i=0;i<OFFER_COUNT;i++) {
            ArrayList<ExerciseVariant> pool=new ArrayList<>(allowed);
            Collections.shuffle(pool,new Random(seed+0x9E3779B97F4A7C15L*i));
            ArrayList<ExerciseVariant> selected=new ArrayList<>();
            Set<String> usedIds=new HashSet<>();
            Set<String> usedTypes=new HashSet<>();
            Set<String> usedFamilies=new HashSet<>();
            // First vary families and types across all proposals; relax only
            // diversity, never safety or equipment, when options are scarce.
            for(int pass=0;pass<4 && selected.size()<request.stepCount();pass++) {
                for(ExerciseVariant variant:pool) {
                    if(selected.size()>=request.stepCount())break;
                    if(usedIds.contains(variant.id()))continue;
                    if(pass<2 && seenAcross.contains(variant.id()))continue;
                    String family=catalog.categoryIndex().familyForVariant(variant.id());
                    if(pass==0 && usedFamilies.contains(family))continue;
                    if(pass<=1 && usedTypes.contains(variant.typeId()))continue;
                    if(usedIds.add(variant.id())) {
                        selected.add(variant);
                        usedTypes.add(variant.typeId());
                        usedFamilies.add(family);
                    }
                }
            }
            if(selected.size()<request.stepCount())continue;
            // Avoid exact duplicate offers when compatible pool is tiny.
            ArrayList<String> ids=new ArrayList<>();
            for(ExerciseVariant variant:selected)ids.add(variant.id());
            ArrayList<String> normalized=new ArrayList<>(ids);
            Collections.sort(normalized);
            if(!fingerprints.add(String.join(",",normalized)))continue;
            seenAcross.addAll(ids);
            String id=Long.toUnsignedString(seed("routine|"+request.date
                    +"|"+profile.missionSignature()+"|"+request.category
                    +"|"+request.level+"|"+request.minutes+"|"
                    +request.variation+"|"+String.join(",",ids)),16);
            result.add(new Offer(id,selected,request));
        }
        return Collections.unmodifiableList(result);
    }

    private static long seed(String input) {
        try {
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(
                    input.getBytes(StandardCharsets.UTF_8));
            long value=0L;
            for(int i=0;i<8;i++)value=(value<<8)|(hash[i]&255L);
            return value;
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible",e);
        }
    }
}
