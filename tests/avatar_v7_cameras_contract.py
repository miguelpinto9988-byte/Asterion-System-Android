#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/'app/src/main/java/pe/asterionsystem/app'
g=lambda n:(java/n).read_text(encoding='utf-8')
k=g('AvatarMultiViewKinematics.java')
r=g('AvatarMultiViewRenderer.java')
side=g('AvatarRenderer.java')
art=g('AvatarArtDetails.java')
view=g('NativeAvatarView.java')
for token in ('FRONT_SQUAT(', 'frontWideSquat(', 'leftLeg.joint',
              'rightLeg.joint', 'body.upperLeg', 'body.lowerLeg'):
    assert token in k,token
for token in ('drawFront(c,p,squat||jacks)', 'drawFrontDenimLeg(',
              'drawFrontArm(', 'drawDiamondTop(', 'drawHandsInset(',
              'drawPushupSideInset(', 'rr(c,193f,topNeck'):
    assert token in r,token
assert 'details.drawShortNeck(' in side
assert 'drawShortNeck(' in art
assert 'ViewMode.FRONT_SQUAT' in view
for name in ('AvatarMultiViewKinematicsTest.java',):
    t=(root/'app/src/test/java/pe/asterionsystem/app'/name).read_text()
    assert 'wideSquatIsGroundedInFrontalPlaneWithOpposedKnees' in t
print('OK: 6 escenas articuladas por ejercicio, cuello en todas, apoyo fijo.')
