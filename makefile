POM_PATH = com.riccardo.shop/pom.xml

run-coverage:
	mvn clean verify -Pjacoco -f $(POM_PATH)
.PHONY: run-coverage

run-pit:
	mvn clean verify -Pmutation-testing -f $(POM_PATH)
.PHONY: run-pit

package:
	mvn clean package -D maven.test.skip=true -f $(POM_PATH)
.PHONY: package

run-app-mongo:
	make package
	docker compose -f docker-compose-mongo.yml up --build --wait
	java -jar com.riccardo.shop/target/*-jar-with-dependencies.jar
.PHONY: run-app-mongo

run-app-maria:
	make package
	docker compose -f docker-compose-maria.yml up --build --wait
	java -jar com.riccardo.shop/target/*-jar-with-dependencies.jar --db-type="maria" --maria-user="docker"
.PHONY: run-app-maria

stop-containers:
	docker compose -f docker-compose-mongo.yml -f docker-compose-maria.yml down -v
.PHONY: stop-containers
