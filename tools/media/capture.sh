#!/bin/zsh
# usage: capture.sh <rig> <screen> — rig: ios26 | ios18 | android
# Captures one sample screen: stills from the open flow, a GIF from the action flow. Output under
# build/media/<rig>/<screen> (override with MEDIA_OUT). Device ids: edit the case below for your machines.
set -u
HERE=${0:A:h}
S=${MEDIA_OUT:-$HERE/../../build/media}
mkdir -p $S
rig=$1; screen=$2
case $rig in
  ios26) dev=7289CAAF-665F-4CD5-9E63-0FA0D9D7B28B; app=com.mohamedrejeb.Calf.debug;;
  ios18) dev=5356D66E-1D76-460F-B661-42BB476F5F54; app=com.mohamedrejeb.Calf.debug;;
  android) dev=emulator-5554; app=com.mohamedrejeb.calf.android;;
esac
out=$S/media/$rig/$screen; rm -rf $out; mkdir -p $out/stills
flow=${FLOWS:-$HERE/flows/$rig}/$screen
maestro --device $dev test -e APP_ID=$app --test-output-dir $out/open $flow/open.yaml > $out/open.log 2>&1; echo "open rc=$?"
find $out/open -name "*.png" -exec cp {} $out/stills/ \;
if [ -s $flow/action.yaml ]; then
  if [ $rig = android ]; then
    ~/Library/Android/sdk/platform-tools/adb -s $dev shell screenrecord --bit-rate 6000000 --time-limit 60 /sdcard/rec.mp4 & recpid=$!
  else
    xcrun simctl io $dev recordVideo --codec h264 --force $out/rec.mp4 > $out/rec.log 2>&1 & recpid=$!
  fi
  sleep 2
  maestro --device $dev test -e APP_ID=$app --test-output-dir $out/action $flow/action.yaml > $out/action.log 2>&1; echo "action rc=$?"
  sleep 1
  if [ $rig = android ]; then
    ~/Library/Android/sdk/platform-tools/adb -s $dev shell pkill -INT screenrecord; sleep 2; wait $recpid 2>/dev/null; ~/Library/Android/sdk/platform-tools/adb -s $dev pull /sdcard/rec.mp4 $out/rec.mp4 > /dev/null 2>&1
  else
    kill -INT $recpid; sleep 3
  fi
  find $out/action -name "*.png" -exec cp {} $out/stills/ \;
  [ -s $out/rec.mp4 ] && ffmpeg -v error -y -i $out/rec.mp4 -vf "fps=15,scale=360:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=4" $out/$screen.gif && echo "gif $(du -k $out/$screen.gif | cut -f1)K"
fi
for f in $out/stills/*.png; do [ -f "$f" ] && sips -Z 1080 "$f" --out "$f" >/dev/null 2>&1; done
ls $out/stills 2>/dev/null | tr '\n' ' '; echo
