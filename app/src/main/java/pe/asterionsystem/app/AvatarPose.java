package pe.asterionsystem.app;

/**
 * Immutable animation snapshot: logical joints, not the drawable pieces.
 * Independent from Android and suitable for testable rig calculations.
 */
public class AvatarPose {
    public final AvatarKinematics.Point ankle,farAnkle,knee,farKnee,hip,farHip;
    public final AvatarKinematics.Point shoulder,farShoulder,neck,head,elbow,wrist;
    public final AvatarKinematics.Point farElbow,farWrist;

    AvatarPose(AvatarKinematics.Point ankle,AvatarKinematics.Point farAnkle,
               AvatarKinematics.Point knee,AvatarKinematics.Point farKnee,
               AvatarKinematics.Point hip,AvatarKinematics.Point farHip,
               AvatarKinematics.Point shoulder,AvatarKinematics.Point farShoulder,
               AvatarKinematics.Point neck,AvatarKinematics.Point head,
               AvatarKinematics.Point elbow,AvatarKinematics.Point wrist,
               AvatarKinematics.Point farElbow,AvatarKinematics.Point farWrist) {
        this.ankle=ankle;this.farAnkle=farAnkle;
        this.knee=knee;this.farKnee=farKnee;
        this.hip=hip;this.farHip=farHip;
        this.shoulder=shoulder;this.farShoulder=farShoulder;
        this.neck=neck;this.head=head;this.elbow=elbow;this.wrist=wrist;
        this.farElbow=farElbow;this.farWrist=farWrist;
    }
}
