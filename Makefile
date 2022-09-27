dev:
	mvn spring-boot:run

clean:
	mvn clean

codeGenerate:
	mvn exec:java -Dexec.mainClass="site.haihui.challenge.utils.MPGenerator"  -Dexec.cleanupDaemonThreads=false

release:
	./mvnw package -Dmaven.test.skip=true  --settings ./settings.xml
	docker build -t yy194131/challenge:$(tag) .
	docker push  yy194131/challenge:$(tag)

deploy: release
	ssh roselle bash start_challenge.sh $(tag)
