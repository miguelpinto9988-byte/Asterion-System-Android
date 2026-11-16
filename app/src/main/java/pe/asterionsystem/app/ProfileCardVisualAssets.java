package pe.asterionsystem.app;

import android.content.Context;

/** Non-destructive mapping from stable profile values to approved local WebP resources. */
final class ProfileCardVisualAssets {
    private ProfileCardVisualAssets() { }

    static String hunterDrawable(String id) {
        if ("sung_jinwoo".equals(id) || "cazador_azul".equals(id)) return "asterion_hunter_blue";
        if ("cha_haein".equals(id) || "cazadora_dorada".equals(id)) return "asterion_hunter_gold";
        if ("yoo_joonghyuk".equals(id) || "guerrero_oscuro".equals(id)) return "asterion_hunter_dark";
        if ("kim_dokja".equals(id) || "estratega_blanco".equals(id)) return "asterion_hunter_white";
        // Preserve any unknown or legacy stored value: never reset the saved profile.
        return "asterion_hunter_blue";
    }

    static String backgroundDrawable(String id) {
        if ("ruinas".equals(id) || "templo_oriental".equals(id)) return "asterion_card_temple";
        if ("niebla".equals(id) || "playa_tropical".equals(id)) return "asterion_card_beach";
        if ("obsidiana".equals(id) || "ciudad_occidental".equals(id)) return "asterion_card_city";
        if ("aurora".equals(id) || "desierto".equals(id)) return "asterion_card_desert";
        if ("bosque".equals(id) || "bosque_oriental".equals(id)) return "asterion_card_forest";
        return "asterion_card_temple";
    }

    /** Stable companion IDs already persisted by the original profile wizard. */
    static String companionDrawable(String id) {
        if ("igris".equals(id)) return "asterion_companion_igris";
        if ("beru".equals(id)) return "asterion_companion_beru";
        if ("tank".equals(id)) return "asterion_companion_tank";
        if ("tusk".equals(id)) return "asterion_companion_tusk";
        if ("iron".equals(id)) return "asterion_companion_iron";
        return "";
    }

    static int resourceId(Context context, String drawableName) {
        return context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
    }
}
