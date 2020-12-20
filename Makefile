$(eval name := $(shell ./bin/sbt -no-colors name|tail -1|awk '{print $$2}'))
$(eval version := $(shell ./bin/sbt -no-colors version|tail -1|awk '{print $$2}'))
THRESHOLD ?= 600
OUTPUT_LIMIT ?= 100

.PHONY: build
build:
	./bin/sbt assembly

.PHONY: clean
clean:
	rm -rf dist/*
	./bin/sbt clean

.PHONY: prepare
prepare:
	rm -rf dist/*
	mkdir -p dist

.PHONY: deploy_build
deploy_build: prepare
	cp target/scala-2.11/$(name)-assembly-$(version).jar dist/$(name)-assembly.jar

.PHONY: deploy_prebuild
deploy_prebuild: prepare
	gunzip -c prebuild/session_analysis-assembly-0.0.1.jar.gz -c > dist/$(name)-assembly.jar

.PHONY: run
run:
	docker-compose up -d spark-master
	docker-compose exec spark-master \
	/spark/bin/spark-submit \
	--master "local[4]" \
	--class data.challenge.session.Main \
	spark/applications/session_analysis-assembly.jar \
	--input-path input \
	--output-path /out/result \
	--output-limit $(OUTPUT_LIMIT) \
	--session-threshold $(THRESHOLD)
	docker-compose down