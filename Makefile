.PHONY: install debug release lint fmt
install: debug
debug:
	./gradlew assembleDebug --offline
	adb install -r app/build/outputs/apk/debug/app-debug.apk
	adb shell am start -n com.m57.hermescontrol/.MainActivity
release:
	./gradlew assembleRelease
lint:
	./gradlew ktlintCheck checkColorLiterals
fmt:
	./gradlew ktlintFormat
