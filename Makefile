.PHONY: test package

test:
	sh ./mvnw -B -ntp test

package:
	sh ./mvnw -B -ntp package
