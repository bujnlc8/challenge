dev:
	mvn spring-boot:run

clean:
	mvn clean

codeGenerate:
	mvn exec:java -Dexec.mainClass="site.haihui.challenge.utils.MPGenerator"  -Dexec.cleanupDaemonThreads=false

release:
	./mvnw clean package -Dmaven.test.skip=true  --settings ./settings.xml
	docker build -t registry.cn-beijing.aliyuncs.com/roselle-1/challenge:$(tag) .
	docker push  registry.cn-beijing.aliyuncs.com/roselle-1/challenge:$(tag)

deploy: release
	ssh roselle bash challenge/start_challenge.sh $(tag)
