#!/usr/bin/env python3
"""V9: multi-angle appearance must share the actual side-avatar detail code."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/'app/src/main/java/pe/asterionsystem/app'
rd=lambda p:(java/p).read_text(encoding='utf-8')
art=rd('AvatarArtDetails.java')
multi=rd('AvatarMultiViewRenderer.java')
side=rd('AvatarRenderer.java')
view=rd('NativeAvatarView.java')
for token in ('drawHeadFront(', 'drawHeadTop(', 'drawProjectedSneaker(',
              'drawProjectedHand(', 'drawCameraJeans(',
              'drawForearm(Canvas c,AvatarKinematics.Point elbow,',
              'c.drawRoundRect(', 'palette.skin', 'palette.hair',
              'palette.shoeDark'):
    assert token in art, token
for token in ('details.drawHeadFront(', 'details.drawHeadTop(',
              'details.drawProjectedSneaker(', 'details.drawProjectedHand(',
              'details.drawCameraJeans(', 'details.drawForearm(',
              'private void tapered(', 'bodySpec.thighWidth',
              'bodySpec.upperArmWidth', 'bodySpec.forearmWidth',
              'palette.legColor',
              'drawFront(c,p,squat||jacks)', 'drawHandsInset(',
              'drawPushupSideInset('):
    assert token in multi, token
assert 'details.drawForearm(canvas,p.elbow,p.wrist,palette)' in side
assert 'multiRenderer.draw(canvas,pose,viewMode,palette,dark,bodySpec)' in view
assert 'private final AvatarArtDetails details=new AvatarArtDetails();' in multi
assert 'new LinearGradient(' in multi and 'new Paint(Paint.ANTI_ALIAS_FLAG)' in multi
for forbidden in ('UnityPlayer', 'MediaPlayer', 'HttpURLConnection'):
    assert forbidden not in art+multi+side,forbidden
print('OK: identidad compartida en rostro, zapatillas, manos, denim, manga y articulación.')
