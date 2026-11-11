package pe.asterionsystem.app;

/** Immutable paint tokens; appearance is independent of movement data. */
public final class AvatarPalette {
    public static final AvatarPalette BLUE=new AvatarPalette(
            false,0xFF26334C,0xFFF0B486,0xFFDE986B,0xFF49352D,0xFF79564A,
            0xFFE6EBF5,0xFF657287,0xFF24559D,0xFF5584C8,0xFF33384E,
            0xFF353A51,0xFF8F95A8,0xFF5D6A83);
    public static final AvatarPalette PURPLE=new AvatarPalette(
            true,0xFF26334C,0xFFF0B486,0xFFDE986B,0xFF49352D,0xFF79564A,
            0xFFE6EBF5,0xFF657287,0xFF7850B1,0xFFB194E2,0xFF403B61,
            0xFF45405F,0xFF4D486A,0xFF74668C);

    public final boolean female;
    public final int outline,skin,skinShade,hair,hairHighlight,shoe,shoeDark;
    public final int top,accent,shorts,legColor,farLegColor,garmentShade;

    private AvatarPalette(boolean female,int outline,int skin,int skinShade,
                          int hair,int hairHighlight,int shoe,int shoeDark,
                          int top,int accent,int shorts,int legColor,
                          int farLegColor,int garmentShade) {
        this.female=female;this.outline=outline;this.skin=skin;
        this.skinShade=skinShade;this.hair=hair;
        this.hairHighlight=hairHighlight;this.shoe=shoe;
        this.shoeDark=shoeDark;this.top=top;this.accent=accent;
        this.shorts=shorts;this.legColor=legColor;
        this.farLegColor=farLegColor;this.garmentShade=garmentShade;
    }
}
