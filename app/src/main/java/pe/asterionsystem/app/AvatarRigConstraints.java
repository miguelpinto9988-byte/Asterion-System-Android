package pe.asterionsystem.app;

/**
 * Fixed-length rig mathematics in virtual Canvas units.
 *
 * Original lightweight Java implementation informed by the FABRIK method and
 * two-link geometric IK. No third-party runtime, library source or JNI is used.
 * The limits below are illustrative animation parameters, not clinical ranges.
 */
public final class AvatarRigConstraints {
    private static final float EPS=.0001f;
    private AvatarRigConstraints(){}

    public static final class TwoBone {
        public final AvatarKinematics.Point root,joint,end,requestedEnd;
        public final float flexDegrees;
        public final boolean targetLimited;
        private TwoBone(AvatarKinematics.Point root,AvatarKinematics.Point joint,
                        AvatarKinematics.Point end,AvatarKinematics.Point requested,
                        float flex,boolean limited) {
            this.root=root;this.joint=joint;this.end=end;this.requestedEnd=requested;
            this.flexDegrees=flex;this.targetLimited=limited;
        }
    }
    public static final class Chain {
        public final AvatarKinematics.Point[] joints;
        public final boolean targetLimited;
        public final float targetError;
        private Chain(AvatarKinematics.Point[] joints,boolean limited,float error){
            this.joints=joints.clone();
            this.targetLimited=limited;this.targetError=error;
        }
    }

    /**
     * Analytical 2-link IK: root, hinge and returned end share an exact chain.
     * If a requested target is unreachable or outside the selected bend range,
     * it is moved to the nearest admissible radius, never stretching a bone.
     * bendSign -1 bends to screen right for a downward-facing planted leg;
     * +1 selects the opposite branch. Both link lengths remain invariant.
     */
    public static TwoBone twoBone(AvatarKinematics.Point root,
                                  AvatarKinematics.Point requestedEnd,
                                  float proximal,float distal,int bendSign,
                                  float minFlexDegrees,float maxFlexDegrees) {
        validPoint(root);validPoint(requestedEnd);
        positive(proximal);positive(distal);
        if(bendSign!=-1&&bendSign!=1)
            throw new IllegalArgumentException("Bend sign must be -1 or 1");
        if(!Float.isFinite(minFlexDegrees)||!Float.isFinite(maxFlexDegrees)
                ||minFlexDegrees<0f||maxFlexDegrees>179.9f
                ||minFlexDegrees>maxFlexDegrees)
            throw new IllegalArgumentException("Invalid 2D flexion interval");
        double minRad=Math.toRadians(minFlexDegrees);
        double maxRad=Math.toRadians(maxFlexDegrees);
        double farRadius=distanceForFlex(proximal,distal,minRad);
        double nearRadius=distanceForFlex(proximal,distal,maxRad);
        double dx=requestedEnd.x-root.x,dy=requestedEnd.y-root.y;
        double raw=Math.hypot(dx,dy);
        double ux=raw<EPS?0:dx/raw;
        double uy=raw<EPS?1:dy/raw;
        double d=Math.max(Math.max(EPS,nearRadius),Math.min(farRadius,raw));
        // The side plane is chosen consistently throughout the animation.
        double a=(proximal*proximal-distal*distal+d*d)/(2*d);
        double h=Math.sqrt(Math.max(0d,proximal*proximal-a*a));
        AvatarKinematics.Point hinge=new AvatarKinematics.Point(
                (float)(root.x+ux*a-bendSign*uy*h),
                (float)(root.y+uy*a+bendSign*ux*h));
        AvatarKinematics.Point endpoint=new AvatarKinematics.Point(
                (float)(root.x+ux*d),(float)(root.y+uy*d));
        double cos=(d*d-proximal*proximal-distal*distal)/(2d*proximal*distal);
        float flex=(float)Math.toDegrees(Math.acos(clamp(cos,-1d,1d)));
        return new TwoBone(root,hinge,endpoint,requestedEnd,flex,
                Math.abs(d-raw)>.001d);
    }

    /**
     * The opposite constraint when an end is planted: adjust the moving root
     * before solving. This preserves the world-space contact point exactly.
     */
    public static AvatarKinematics.Point rootForPinnedEnd(
            AvatarKinematics.Point desiredRoot,
            AvatarKinematics.Point pinnedEnd,float first,float second,
            float minFlexDegrees,float maxFlexDegrees) {
        TwoBone reversed=twoBone(pinnedEnd,desiredRoot,second,first,
                1,minFlexDegrees,maxFlexDegrees);
        return reversed.end;
    }

    /**
     * FABRIK for chains with 2+ links. A fixed root, bounded iterations and
     * per-link length projection prevent drifting or accumulated stretching.
     * This generic helper is available to future animations with more bones;
     * the current arms/legs use the analytical 2-link solver above.
     */
    public static Chain fabrik(AvatarKinematics.Point root,
                               AvatarKinematics.Point requestedEnd,
                               float[] lengths,int maxIterations,float tolerance) {
        validPoint(root);validPoint(requestedEnd);
        if(lengths==null||lengths.length<2||lengths.length>32)
            throw new IllegalArgumentException("Chain needs 2..32 segments");
        if(maxIterations<1||maxIterations>100||!Float.isFinite(tolerance)
                ||tolerance<=0f)
            throw new IllegalArgumentException("Invalid FABRIK iteration settings");
        int n=lengths.length;
        double total=0;
        for(float value:lengths) {positive(value);total+=value;}
        double dx=requestedEnd.x-root.x,dy=requestedEnd.y-root.y;
        double distance=Math.hypot(dx,dy);
        double ux=distance<EPS?0:dx/distance;
        double uy=distance<EPS?1:dy/distance;
        AvatarKinematics.Point[] p=new AvatarKinematics.Point[n+1];
        p[0]=root;
        if(distance>=total) {
            for(int i=0;i<n;i++){
                p[i+1]=new AvatarKinematics.Point(
                        (float)(p[i].x+ux*lengths[i]),
                        (float)(p[i].y+uy*lengths[i]));
            }
            return new Chain(p,true,p[n].distance(requestedEnd));
        }
        // A slight alternating pole offset removes collinear deadlocks.
        for(int i=0;i<n;i++){
            double nx=-uy,ny=ux;
            double offset=(i%2==0?1:-1)*.23d*lengths[i];
            double sx=ux*lengths[i]*.973d+nx*offset;
            double sy=uy*lengths[i]*.973d+ny*offset;
            double inv=lengths[i]/Math.hypot(sx,sy);
            p[i+1]=new AvatarKinematics.Point(
                    (float)(p[i].x+sx*inv),(float)(p[i].y+sy*inv));
        }
        for(int iteration=0;iteration<maxIterations;iteration++){
            p[n]=requestedEnd;
            for(int i=n-1;i>=0;i--)
                p[i]=pointAtDistance(p[i+1],p[i],lengths[i],ux,uy);
            p[0]=root;
            for(int i=0;i<n;i++)
                p[i+1]=pointAtDistance(p[i],p[i+1],lengths[i],ux,uy);
            if(p[n].distance(requestedEnd)<=tolerance)break;
        }
        return new Chain(p,false,p[n].distance(requestedEnd));
    }

    private static AvatarKinematics.Point pointAtDistance(
            AvatarKinematics.Point base,AvatarKinematics.Point towards,
            float length,double fallbackX,double fallbackY) {
        double dx=towards.x-base.x,dy=towards.y-base.y;
        double raw=Math.hypot(dx,dy);
        double ux=raw<EPS?fallbackX:dx/raw;
        double uy=raw<EPS?fallbackY:dy/raw;
        return new AvatarKinematics.Point(
                (float)(base.x+ux*length),(float)(base.y+uy*length));
    }

    public static void requireFixedLength(AvatarKinematics.Point a,
                                          AvatarKinematics.Point b,float expected,
                                          float tolerance){
        validPoint(a);validPoint(b);positive(expected);
        if(!Float.isFinite(tolerance)||tolerance<=0)
            throw new IllegalArgumentException("Invalid tolerance");
        if(Math.abs(a.distance(b)-expected)>tolerance)
            throw new IllegalStateException("Bone changed length");
    }

    private static double distanceForFlex(float l1,float l2,double rad){
        return Math.sqrt(Math.max(0d,l1*l1+l2*l2+2d*l1*l2*Math.cos(rad)));
    }
    private static double clamp(double v,double lo,double hi){
        return Math.max(lo,Math.min(hi,v));
    }
    private static void validPoint(AvatarKinematics.Point p){
        if(p==null||!Float.isFinite(p.x)||!Float.isFinite(p.y))
            throw new IllegalArgumentException("Joint coordinates must be finite");
    }
    private static void positive(float v){
        if(!Float.isFinite(v)||v<=0f||v>10000f)
            throw new IllegalArgumentException("Bone length must be positive and finite");
    }
}
