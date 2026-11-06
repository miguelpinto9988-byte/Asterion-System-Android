package pe.asterionsystem.app;

/**
 * Single shared catalog for all onboarding icons. Each icon is packaged only
 * once, referenced by resource ID throughout the entire app, and is never
 * generated, downloaded or recolored at launch.
 */
public final class AsterionRegistrationIcons {
    public enum Icon { PROFILE, NAME, AGE, HEIGHT, WEIGHT, GENDER, PHOTO, NEXT }
    private AsterionRegistrationIcons() { }
    public static int drawable(Icon icon) {
        switch (icon) {
            case PROFILE: return R.drawable.asterion_reg_profile;
            case NAME: return R.drawable.asterion_reg_name;
            case AGE: return R.drawable.asterion_reg_age;
            case HEIGHT: return R.drawable.asterion_reg_height;
            case WEIGHT: return R.drawable.asterion_reg_weight;
            case GENDER: return R.drawable.asterion_reg_gender;
            case PHOTO: return R.drawable.asterion_reg_photo;
            case NEXT: return R.drawable.asterion_reg_next;
            default: throw new IllegalArgumentException("Unknown registration icon");
        }
    }
}
