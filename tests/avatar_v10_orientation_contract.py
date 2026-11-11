#!/usr/bin/env python3
"""V10: camera-space soles, toes, head and body axes must be explicit."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
s=r/'app/src/main/java/pe/asterionsystem/app'
read=lambda n:(s/n).read_text(encoding='utf8')
o=read('AvatarOrientationRules.java')
a=read('AvatarArtDetails.java')
m=read('AvatarMultiViewRenderer.java')
l=read('AvatarRenderer.java')
for v in ('PRONE_GROUND_FACING','STANDING','faceNormal','soleNormal',
          'toeAxis','headAxis','visibleSurface(','visibleShoeSurface(',
          'ShoeSurface.UPPER','topToeSign(','requireProneTopPose(',
          'sideFacingDegrees(','Float.isFinite','project('):
    assert v in o,v
for v in ('drawProneTopSneaker(', 'ShoeSurface.UPPER',
          'A sole cannot face the overhead camera','dy<=0f',
          'c.rotate(', 'Floor contact','No sole face'):
    assert v in a,v
assert 'AvatarOrientationRules.requireProneTopPose(p);' in m
assert 'AvatarOrientationRules.requireUprightFrontPose(p);' in m
assert 'details.drawProneTopSneaker(c,a,k,palette);' in m
assert 'details.drawProjectedSneaker(c,a,palette,true);' not in m
assert 'canvas.rotate(AvatarOrientationRules.sideFacingDegrees(' in l
t=(r/'app/src/test/java/pe/asterionsystem/app/AvatarOrientationRulesTest.java').read_text()
assert t.count('@Test')>=7
inst=(r/'app/src/androidTest/java/pe/asterionsystem/app/NativeAvatarRenderTest.java').read_text()
assert 'proneTopShoesFollowLegAxisAndNeverShowOutsole' in inst
print('OK: marco espacial, zapatilla cenital y orientación de cabeza verificados.')
